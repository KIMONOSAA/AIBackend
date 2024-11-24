package com.kimo.service.impl;

import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimo.api.client.UserClient;
import com.kimo.api.dto.UserDto;
import com.kimo.common.ErrorCode;
import com.kimo.constant.ChartConstant;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.exception.ThrowUtils;
import com.kimo.mapper.QuestionMapper;
import com.kimo.model.dto.question.QuestionQueryRequest;

import com.kimo.model.entity.Question;
import com.kimo.model.vo.QuestionDataVO;
import com.kimo.service.QuestionService;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Mr.kimo
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{
    
    @Autowired
    private UserClient userClient;

    @Autowired
    private ServletUtils servletUtils;


    /**
     * 从上传的 Excel 文件中提取数据并添加到数据库中。
     *
     * 该方法接受一个 MultipartFile 类型的文件，检查其格式和大小，
     * 解析 Excel 文件内容并将每个问题数据添加到系统中。
     *
     * @param multipartFile 上传的 Excel 文件，必须是 .xls 或 .xlsx 格式，且大小不超过 1MB。
     * @throws RuntimeException 如果文件格式错误或文件大小超过限制。
     */
    @Override
    @Transactional
    public void extractData(MultipartFile multipartFile) {
        //1.检查文件是否是xls，并且文件是否超过1MB
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024L * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1MB");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffix = Arrays.asList(ChartConstant.SPECIFICATION_XLSX, ChartConstant.SPECIFICATION_XLS);
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
        try {
            List<QuestionDataVO> questionDataVOList = EasyExcel.read(multipartFile.getInputStream())
                    .head(QuestionDataVO.class)
                    .sheet()
                    .doReadSync();
            questionDataVOList.forEach(this::addQuestion);
        } catch (IOException e) {
            log.error("表格处理错误");
            e.printStackTrace();
        }
    }


    /**
     * 根据用户请求生成一个查询包装器，用于查询问题数据。
     *
     * 该方法从请求中获取当前用户信息，并根据传入的查询请求构建 QueryWrapper。
     * 如果没有提供查询请求，则返回一个空的 QueryWrapper。
     *
     * @param questionQueryRequest 查询请求对象，包含筛选条件和排序信息。
     * @param request HTTP 请求对象，用于获取当前用户信息。
     * @return 构建好的 QueryWrapper 对象，包含查询条件。
     * @throws RuntimeException 如果用户未登录。
     */
    @Override
    public Wrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest, HttpServletRequest request) {
        // 获取当前用户
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        ThrowUtils.throwIf(userDto == null,ErrorCode.NOT_LOGIN_ERROR);

        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        queryWrapper.eq(CommonConstant.SUBJECTS,questionQueryRequest.getSubjects());
        queryWrapper.last(SqlConstants.SORT);
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    private void addQuestion(QuestionDataVO questionDataVO) {
        Question question = new Question();
        BeanUtils.copyProperties(questionDataVO, question);
        this.save(question);
    }
    
    
}




