package com.kimo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimo.common.ErrorCode;
import com.kimo.constant.ContentConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.constant.UserConstant;
import com.kimo.exception.ThrowUtils;
import com.kimo.feignclient.ChartClient;
import com.kimo.feignclient.UserClient;
import com.kimo.mapper.AnswerMapper;
import com.kimo.mapper.QuestionMapper;
import com.kimo.model.dto.answer.AnswerAddResultRequest;
import com.kimo.model.dto.answer.ChartDataRequest;
import com.kimo.model.dto.answer.QuestionListRequest;
import com.kimo.model.dto.answer.WrongInformation;
import com.kimo.model.dto.user.UserDto;
import com.kimo.model.entity.Answer;
import com.kimo.model.entity.Question;
import com.kimo.model.vo.BiResponse;
import com.kimo.service.AnswerService;
import com.kimo.service.QuestionService;
import com.kimo.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.kimo.constant.ScoresConstant.USER_EVALUATION;

/**
 * @author Mr.kimo
 */
@Service
@Slf4j
public class AnswerServiceImpl extends ServiceImpl<AnswerMapper, Answer>
    implements AnswerService{

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ChartClient chartClient;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ServletUtils servletUtils;


    /**
     * 生成用户的答题结果并调用 AI 模块进行评估。
     *
     * 此方法处理用户提交的答题结果，包括计算正确答案数量和生成评估报告。
     *
     * @param answerAddResultRequest 包含用户答题结果的请求对象，包括问题列表。
     * @param request HTTP 请求对象，用于获取当前用户的信息。
     * @return BiResponse 返回生成的评估结果，包含 AI 模块的响应信息。
     *
     * @throws Exception 如果在处理过程中发生错误，例如调用 AI 模块失败。
     */
    @Override
    public BiResponse generateAnswer(AnswerAddResultRequest answerAddResultRequest, HttpServletRequest request) throws Exception {
        // 获取问题列表
        List<QuestionListRequest> questionList = answerAddResultRequest.getQuestion();

        // 获取当前用户
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);

        // 初始化用户答题记录
        Answer answer = new Answer();
        answer.setUserId(userDto.getId());
        answer.setUserWrong(0L);
        answer.setUserRight(0L);

        // 初始化返回对象和错误信息列表
        BiResponse biResponse = new BiResponse();
        WrongInformationList wrongInformationList = new WrongInformationList();

        // 遍历问题列表并进行处理
        questionList.forEach(question -> processAnswer(question, answer, wrongInformationList));

        // 计算答题结果的百分比
        long totalQuestions = questionList.size();
        long correctQuestions = answer.getUserRight();
        int percentage = (int) ((correctQuestions * 100) / totalQuestions);
        answer.setUserResult(percentage);

        // 保存用户答题记录
        this.saveOrUpdate(answer);

        // 构造AI输入
        StringBuilder userInput = new StringBuilder();
        userInput.append(USER_EVALUATION).append("\n");
        wrongInformationList.getWrongInformations().forEach(wrongInformation -> generateGuestions(wrongInformation,userInput));

        ChartDataRequest chartDataRequest = new ChartDataRequest();
        chartDataRequest.setChartData(userInput.toString());

        // 调用AI模块生成评估结果
        String result = chartClient.genChartData(chartDataRequest);
        biResponse.setGenResult(result);

        return biResponse;
    }


    /**
     * 生成包含错误题目信息的字符串，并将其添加到用户输入中。
     *
     * 该方法处理一道错误题目，格式化题目标题、选项、正确答案和学生的错误答案，并将结果追加到给定的 StringBuilder 对象中。
     *
     * @param wrongInformation 包含错误题目信息的对象，提供题目标题、选项、正确答案和学生的错误答案。
     * @param userInput 用于构建最终输入的 StringBuilder 对象，包含评估生成所需的信息。
     */
    private void generateGuestions(WrongInformation wrongInformation,StringBuilder userInput){
        userInput.append(wrongInformation.getSubjectsTitle()).append(ContentConstant.LINE_FEED);
        userInput.append(wrongInformation.getOptionA()).append(wrongInformation.getOptionB()).append(wrongInformation.getOptionC()).append(wrongInformation.getOptionD()).append(ContentConstant.LINE_FEED);
        userInput.append(ContentConstant.TURE_FEEDBACK).append(wrongInformation.getSubjectsResult()).append(ContentConstant.LINE_FEED);
        userInput.append(ContentConstant.FALSE_FEEDBACK).append(wrongInformation.getSubjectsWrong()).append(ContentConstant.LINE_FEED);
        userInput.append(ContentConstant.LINE_FEED);

    }


    /**
     * 处理用户对问题的回答，检查答案的正确性并更新答题记录。
     *
     * 该方法根据用户的回答记录正确和错误的题目，并将错误题目信息保存到错误信息列表中。
     *
     * @param questionRequest 用户提交的问题请求，包含问题 ID 和用户的答案。
     * @param answer 用户的答题记录，用于记录答对和答错的题目数量。
     * @param wrongInformationList 错误信息列表，用于保存用户答错的题目信息。
     */
    private synchronized void processAnswer(QuestionListRequest questionRequest, Answer answer, WrongInformationList wrongInformationList) {
        // 检查问题请求是否为空
        ThrowUtils.throwIf(questionRequest == null, ErrorCode.NOT_FOUND_ERROR);

        // 根据问题ID查找问题
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SqlConstants.CHART_ID, questionRequest.getId());
        Question question = questionMapper.selectOne(queryWrapper);

        // 检查问题是否存在
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);

        // 设置答题记录中的科目信息
        answer.setUserSubjects(question.getSubjects());

        // 判断用户答案是否正确
        if (questionRequest.getValue().equals(question.getSubjectsResult())) {
            // 答对了，增加答对题数
            answer.setUserRight(answer.getUserRight() + 1);
        } else {
            // 答错了，增加答错题数
            answer.setUserWrong(answer.getUserWrong() + 1);

            // 保存错题信息
            WrongInformation wrongInformation = new WrongInformation();
            BeanUtils.copyProperties(question, wrongInformation);
            wrongInformation.setSubjectsWrong(questionRequest.getValue());
            wrongInformationList.getWrongInformations().add(wrongInformation);
        }
    }

}




