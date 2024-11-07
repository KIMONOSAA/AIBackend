package com.kimo.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.amqp.AccuracyChartProducer;
import com.kimo.amqp.PracticeChartProducer;
import com.kimo.common.ErrorCode;
import com.kimo.constant.SecurityConstants;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.feignclient.ChartClient;
import com.kimo.feignclient.UserClient;
import com.kimo.mapper.AccuracyMapper;
import com.kimo.mapper.PracticeAnswerMapper;
import com.kimo.mapper.PracticeRecordMapper;
import com.kimo.mapper.QuestionMapper;
import com.kimo.model.dto.*;
import com.kimo.model.po.*;
import com.kimo.service.PracticeAnswerService;
import com.kimo.service.PracticeRecordService;
import com.kimo.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;



/**
 * @author Mr.kimo
 */
@Service
@Slf4j
public class PracticeAnswerServiceImpl extends ServiceImpl<PracticeAnswerMapper, PracticeAnswer>
    implements PracticeAnswerService {

    @Autowired
    private ServletUtils servletUtils;

    @Autowired
    private UserClient userClient;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private PracticeAnswerMapper practiceAnswerMapper;

    @Autowired
    private PracticeRecordMapper practiceRecordMapper;

    @Autowired
    @Lazy
    private PracticeAnswerService currentProxy;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private AccuracyMapper accuracyMapper;

    @Autowired
    private ChartClient chartClient;

//    @Autowired
//    private ExecutorService executorService;

    @Autowired
    private AccuracyChartProducer accuracyChartProducer;

    @Autowired
    private PracticeChartProducer practiceChartProducer;


    @Override
    public Boolean setResultAnswer(AnswerAddResultRequest answerAddResultRequest, HttpServletRequest request) {
        // 获取当前用户
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        ThrowUtils.throwIf(userDto == null,ErrorCode.COURSE_USER_NOT_FOUND_ERROR);
        // 获取问题列表
        List<QuestionListRequest> questionList = answerAddResultRequest.getQuestion();
        ThrowUtils.throwIf(questionList == null, ErrorCode.NOT_FOUND_ERROR);
        Long practiceId = answerAddResultRequest.getPracticeId();
        Long courseId = answerAddResultRequest.getCourseId();
        Long teachplanId = answerAddResultRequest.getTeachplanId();
        if(practiceId == null || courseId == null || teachplanId == null || practiceId <= 0 || courseId <= 0 || teachplanId <= 0) {
            return Boolean.FALSE;
        }

        //批量处理题目
        Map<Long, Question> questionCache = questionMapper.selectBatchIds(
                        questionList.stream().map(QuestionListRequest::getId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Question::getId, question -> question));

        // 遍历问题列表并进行处理
        questionList.forEach(question -> currentProxy.createAnswerData(question,userDto,practiceId,courseId,teachplanId,questionCache));
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public void createAnswerData(QuestionListRequest question, UserDto userDto, Long practiceId, Long courseId,Long teachplanId,Map<Long, Question> questionCache) {
        Question questionData = questionCache.get(question.getId());
        PracticeAnswer practiceAnswer = new PracticeAnswer();


        // 更新答题记录
        PracticeRecord record = getPracticeRecord(userDto.getId(), practiceId, courseId);
        boolean isCorrect = question.getValue().equals(questionData.getSubjectsResult());
        updatePracticeRecord(record, isCorrect);


        practiceAnswer.setQuestionId(question.getId());
        practiceAnswer.setUserId(userDto.getId());
        practiceAnswer.setCourseId(courseId);
        practiceAnswer.setPracticeId(practiceId);
        practiceAnswer.setUserResult(question.getValue());
        practiceAnswer.setAnswerStatus(question.getValue().equals(questionData.getSubjectsResult()));
        practiceAnswerMapper.insert(practiceAnswer);

        String practiceRecordId = courseId + ":" + teachplanId + ":" + userDto.getId() + ":" + practiceId;
        Long expire = redisTemplate.getExpire(practiceRecordId);
        if (!isCorrect && (expire != null || expire != -1)) {
            Map<String, String> map = new HashMap<>();
            map.put("questionId", question.getId().toString());
            map.put("subjects", questionData.getSubjectsResult());
            map.put("subjectsTitle", questionData.getSubjectsTitle());
            map.put("subjectsResult", questionData.getSubjectsResult());
            map.put("optionA", questionData.getOptionA());
            map.put("optionB", questionData.getOptionB());
            map.put("optionC", questionData.getOptionC());
            map.put("optionD", questionData.getOptionD());
            map.put("userResult", question.getValue());
            map.put("answerStatus", String.valueOf(false));

            // 使用 ObjectMapper 将 Map 转换为 JSON 字符串
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String jsonString = objectMapper.writeValueAsString(map);
                // 将 JSON 字符串保存到 Redis 列表中
//                redisTemplate.opsForList().rightPush(practiceId.toString(), jsonString);
                redisTemplate.opsForHash().put(practiceRecordId,question.getId().toString(),jsonString);
            } catch (JsonProcessingException e) {
                e.printStackTrace(); // 处理序列化异常
                throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
            }
        }

    }
    private void updatePracticeRecord(PracticeRecord record, boolean isCorrect) {
        if (isCorrect) {
            record.setUserRight(record.getUserRight() + 1);
        } else {
            record.setUserError(record.getUserError() + 1);
        }
        practiceRecordMapper.updateById(record);
    }


    private PracticeRecord getPracticeRecord(Long userId, Long practiceId, Long courseId) {
        QueryWrapper<PracticeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("id", practiceId)
                .eq("course_id", courseId);
        return practiceRecordMapper.selectOne(queryWrapper);
    }

    @Override
    public BiResponse generateAnswer(AnswerAllResultRequest answerAddResultRequest, HttpServletRequest request) {

//        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
//        UserDto userDto = userClient.GobalGetLoginUser(username);
//        ThrowUtils.throwIf(userDto == null, ErrorCode.NOT_LOGIN_ERROR);
//        log.debug("UserDto: {}", userDto);
//        Long practiceId = answerAddResultRequest.getPracticeId();
//        ThrowUtils.throwIf(practiceId == null, ErrorCode.RECORD_NOT_FOUND);
//        Long expire = redisTemplate.getExpire(practiceId.toString());
//        ThrowUtils.throwIf(expire == null, ErrorCode.EXAM_TIMEOUT);
//        ThrowUtils.throwIf(expire == -1, ErrorCode.EXAM_NOT_FOUNT);
//        PracticeRecord record = practiceRecordMapper.selectById(practiceId);
//        ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR);
//        log.debug("PracticeRecord: {}", record);
//        QueryWrapper<Accuracy> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("user_id", userDto.getId());
//        Accuracy accuracy = accuracyMapper.selectOne(queryWrapper);
//        Integer userRight = record.getUserRight();
//        Integer userError = record.getUserError();
//        int total = userError + userRight;
//        ThrowUtils.throwIf(total == 0, ErrorCode.COURSE_NOT_FOUND);
//
//        double result = ((double) userRight / total) * 100;
//        record.setPracticePoint(userRight * 5.0);
//        updateAccuracyForSubject(accuracy, answerAddResultRequest.getSubjects(), result);
//        String allPracticeAnswers = getAllPracticeAnswers(userDto.getId(), practiceId, answerAddResultRequest.getCourseId(), record);
//        BiResponse response = new BiResponse();
//                    response.setGenChart(allPracticeAnswers);
//                    response.setGenResult(record.getPracticePoint());
//                    return response;
        //自定义线程池
        ExecutorService executorService = new ThreadPoolExecutor(
                24,48,60L,
                TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(150),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        try {
            // 获取当前用户
            CompletableFuture<UserDto> userDtoCompletableFuture = CompletableFuture.supplyAsync(() -> {
                String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
                UserDto userDto = userClient.GobalGetLoginUser(username);
                ThrowUtils.throwIf(userDto == null, ErrorCode.NOT_LOGIN_ERROR);
                log.debug("UserDto: {}", userDto);


                return userDto;
            }, executorService);

            Long practiceId = answerAddResultRequest.getPracticeId();
            Long courseIds = answerAddResultRequest.getCourseId();
            Long teachplanId = answerAddResultRequest.getTeachplanId();
            ThrowUtils.throwIf(practiceId == null || practiceId <= 0, ErrorCode.RECORD_NOT_FOUND);
            ThrowUtils.throwIf(courseIds == null || courseIds <= 0, ErrorCode.RECORD_NOT_FOUND);
            ThrowUtils.throwIf(teachplanId == null || teachplanId <= 0, ErrorCode.RECORD_NOT_FOUND);
            CompletableFuture<Long> practiceCompletableFuture = userDtoCompletableFuture.thenApplyAsync(userDto -> {
                // 检查有效期

                String practiceRecordId = courseIds + ":" + teachplanId + ":" + userDto.getId() + ":" + practiceId;
                Long expire = redisTemplate.getExpire(practiceRecordId);
                ThrowUtils.throwIf(expire == null, ErrorCode.EXAM_TIMEOUT);
                ThrowUtils.throwIf(expire == -1, ErrorCode.EXAM_NOT_FOUNT);
                return expire;
            }, executorService);

            // 异步获取用户答题记录
            CompletableFuture<PracticeRecord> recordFuture = CompletableFuture.supplyAsync(() -> {
                PracticeRecord record = practiceRecordMapper.selectById(practiceId);
                ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR);
                log.debug("PracticeRecord: {}", record);

                return record;
            }, executorService);

            // 异步获取用户准确率
            CompletableFuture<Accuracy> accuracyFuture = userDtoCompletableFuture.thenApplyAsync(userDto -> {
                QueryWrapper<Accuracy> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", userDto.getId());
                return accuracyMapper.selectOne(queryWrapper);
            }, executorService);

            // 计算准确率和分数，并返回IntermediateResult
            CompletableFuture<BiResponse> responseFuture = userDtoCompletableFuture.thenCombine(recordFuture, (userDto, record) -> {
                Integer userRight = record.getUserRight();
                Integer userError = record.getUserError();
                int total = userError + userRight;
                ThrowUtils.throwIf(total == 0, ErrorCode.COURSE_NOT_FOUND);

                double result = ((double) userRight / total) * 100;
                record.setPracticePoint(userRight * 5.0);

                return new IntermediateResult(userDto, record, result);
            }).thenCombine(accuracyFuture, (intermediateResult, accuracy) -> {
                updateAccuracyForSubject(accuracy, answerAddResultRequest.getSubjects(), intermediateResult.getResult());
                log.debug("Accuracy: {}", accuracy);
                log.debug("IntermediateResult: {}", intermediateResult);
                return intermediateResult;
            }).thenCompose(intermediateResult -> {
                // 调用AI评估接口
                return CompletableFuture.supplyAsync(() ->
                                getAllPracticeAnswers(intermediateResult.getUserDto().getId(), practiceId, answerAddResultRequest.getCourseId(),answerAddResultRequest.getTeachplanId(), intermediateResult.getRecord()),
                        executorService
                ).thenApply(practiceAnswer -> {
                    BiResponse response = new BiResponse();
                    response.setGenChart(practiceAnswer);
                    response.setGenResult(intermediateResult.getRecord().getPracticePoint());
                    return response;

                });

            }).exceptionally(ex -> {
                // 处理异常
                System.out.println(ex.getMessage());
                log.error("Exception occurred", ex);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            });

            return responseFuture.join(); // 等待完成并返回结果
        } finally {
            executorService.shutdown(); // 确保关闭线程池
        }
    }


    private void updateAccuracyForSubject(Accuracy accuracy, String subject, double result) {
        String accuracyList = null;
        try {
            accuracyList = updateAccuracyList(accuracy.getAccuracy(), subject, result);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
        }
        accuracy.setAccuracy(accuracyList.toString());
        accuracyMapper.updateById(accuracy);
        accuracyChartProducer.sendMessage(accuracy);
    }


    private String updateAccuracyList(String accuracyJson, String subject, double result) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, String>> list;

        // 反序列化 JSON
        if (!StringUtils.isEmpty(accuracyJson)) {
            list = objectMapper.readValue(accuracyJson, new TypeReference<List<Map<String, String>>>() {});
        } else {
            list = new ArrayList<>();
        }

        list.stream()
                .filter(item -> item.containsKey(subject))
                .findFirst()
                .ifPresentOrElse(
                        item -> {
                            String existingValue = item.get(subject);
                            // 检查值是否为空或为非数字字符串
                            if (StringUtils.isEmpty(existingValue)) {
                                item.put(subject, String.valueOf(result));
                            } else {
                                try {
                                    double updatedValue = (Double.parseDouble(existingValue) + result) / 2;
                                    item.put(subject, String.valueOf(updatedValue));
                                } catch (NumberFormatException e) {
                                    // 处理非数字字符串的情况，将其替换为新计算的结果
                                    item.put(subject, String.valueOf(result));
                                }
                            }
                        },
                        () -> {
                            // 若不存在该科目的记录，新增一条记录
                            Map<String, String> newItem = new HashMap<>();
                            newItem.put(subject, String.valueOf(result));
                            list.add(newItem);
                        }
                );

        // 将 list 转换为 JSON 字符串
        String jsonOutput = objectMapper.writeValueAsString(list);
        System.out.println("Updated accuracy list in JSON format: " + jsonOutput);

        return jsonOutput;
    }



//    private String getPracticeAnswer(Long userId, Long practiceId, Long courseId, PracticeRecord record) {
//        Map<Object, Object> entries = redisTemplate.opsForHash().entries(practiceId.toString());
//        ChartDataRequest chartDataRequest = new ChartDataRequest();
//
//        if (entries.isEmpty()) {
//            List<PracticeAnswerDto> practiceAnswerDtos = practiceAnswerMapper.selectQuestion(practiceId);
//            chartDataRequest.setChartData(practiceAnswerDtos.toString());
//        } else {
//            chartDataRequest.setChartData(entries.toString());
//        }
//
//        String result = chartClient.genChartData(chartDataRequest);
//        record.setAiresult(result);
//        practiceRecordMapper.updateById(record);
//        return result;
//    }

    private String getAllPracticeAnswers(Long userId, Long practiceId, Long courseId,Long teachplanId, PracticeRecord record) {

        String practiceRecordId = courseId + ":" + teachplanId + ":" + userId + ":" + practiceId;
        List<Map<String, String>> practiceAnswers = new ArrayList<>();
        ChartDataRequest chartDataRequest = new ChartDataRequest();

        Map<Object, Object> entries = redisTemplate.opsForHash().entries(practiceRecordId);

        // 反序列化 JSON 字符串为 Map
        ObjectMapper objectMapper = new ObjectMapper();
        //            for (String json : jsonList) {
//                try {
//                    Map<String, String> practiceAnswer = objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
//                    practiceAnswers.add(practiceAnswer);
//                } catch (JsonProcessingException e) {
//                    e.printStackTrace();
//                }
//            }
        // 遍历 Redis 中的题目数据并反序列化为 Map
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String jsonString = (String) entry.getValue();
            if(entry.getKey().toString().equals("测试键值对")){
                continue;
            }
            try {
                // 反序列化 JSON 字符串为 Map<String, String>
                Map<String, String> questionData = objectMapper.readValue(jsonString, new TypeReference<Map<String, String>>() {});
                practiceAnswers.add(questionData);
            } catch (JsonProcessingException e) {
                e.printStackTrace(); // 处理反序列化异常
                throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
            }
        }

        PracticeRecordDto records = new PracticeRecordDto();
        BeanUtils.copyProperties(record,records);



        chartDataRequest.setChartData(practiceAnswers.toString());
        ChartDataForRecord chartDataForRecord = new ChartDataForRecord();
        chartDataForRecord.setPracticeRecord(records);
        chartDataForRecord.setChartDataRequest(chartDataRequest);
        practiceChartProducer.sendMessage(chartDataForRecord);
        practiceRecordMapper.updateById(record);
        return practiceAnswers.toString();
    }



}




