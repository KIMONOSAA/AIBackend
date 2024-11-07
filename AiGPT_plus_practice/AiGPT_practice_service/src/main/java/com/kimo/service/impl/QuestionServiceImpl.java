package com.kimo.service.impl;

import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.kimo.common.ErrorCode;
import com.kimo.config.RedisIdWorker;
import com.kimo.constant.ChartConstant;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.exception.ThrowUtils;
import com.kimo.feignclient.UserClient;
import com.kimo.mapper.AccuracyMapper;
import com.kimo.mapper.PracticeRecordMapper;
import com.kimo.mapper.QuestionMapper;
import com.kimo.model.dto.QuestionQueryRequest;
import com.kimo.model.dto.UserDto;
import com.kimo.model.po.Accuracy;
import com.kimo.model.po.PracticeRecord;
import com.kimo.model.po.Question;
import com.kimo.model.vo.QuestionDataVO;
import com.kimo.service.QuestionService;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

import java.util.*;

/**
 * @author Mr.kimo
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private ServletUtils servletUtils;


    @Autowired
    private PracticeRecordMapper practiceRecordMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private AccuracyMapper accuracyMapper;


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
     * <p>
     * 该方法从请求中获取当前用户信息，并根据传入的查询请求构建 QueryWrapper。
     * 如果没有提供查询请求，则返回一个空的 QueryWrapper。
     *
     * @param questionQueryRequest 查询请求对象，包含筛选条件和排序信息。
     * @param request              HTTP 请求对象，用于获取当前用户信息。
     * @return 构建好的 QueryWrapper 对象，包含查询条件。
     * @throws RuntimeException 如果用户未登录。
     */
    @Override
    @Transactional
    public Map<String, Wrapper<Question>> getQueryWrapper(QuestionQueryRequest questionQueryRequest, HttpServletRequest request) {
        // 获取当前用户
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        ThrowUtils.throwIf(userDto == null,ErrorCode.NOT_LOGIN_ERROR);
        String userDtoId = userDto.getId().toString();
        Long courseId = questionQueryRequest.getCourseId();
        Long teacherId = questionQueryRequest.getTeacherId();
        ThrowUtils.throwIf(userDtoId.isEmpty(),ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(teacherId == null || teacherId.toString().isEmpty(),ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(courseId == null || courseId.toString().isEmpty(),ErrorCode.NOT_LOGIN_ERROR);

        //创建答题记录
        String oldrecondId = questionQueryRequest.getRecordId().toString();
        QueryWrapper<PracticeRecord> queryWrapperPracticeRecord = new QueryWrapper<>();
        queryWrapperPracticeRecord.eq("user_id", userDtoId);
        queryWrapperPracticeRecord.eq("course_id", courseId);
        queryWrapperPracticeRecord.eq("teachplan_id", teacherId);
        queryWrapperPracticeRecord.eq("id", oldrecondId);
        PracticeRecord record = practiceRecordMapper.selectOne(queryWrapperPracticeRecord);
        long recordId = 0L;
        if(record == null){
            recordId = redisIdWorker.nextId("course");
            String practiceRecordId = courseId + ":" + teacherId + ":" + userDtoId + ":" + recordId;
            String key = "测试键值对";
            String value = "测试键值对";
//            ObjectMapper objectMapper = new ObjectMapper();
//            Map<String,String> map = new HashMap<>();
//            map.put("测试","测试");
//            String s = null;
//            try {
//                s = objectMapper.writeValueAsString(map);
//            } catch (JsonProcessingException e) {
//                throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
//            }
//            redisTemplate.opsForList().rightPush(String.valueOf(recordId),  s);
            redisTemplate.opsForHash().put(practiceRecordId,key,value);
    //                    redisTemplate.opsForValue().setIfAbsent(COURSE_USER_KEY + courseId, String.valueOf(courseId), 30, TimeUnit.MINUTES);
            //设置有效期
            redisTemplate.expire(practiceRecordId, Duration.ofMinutes(45));
            //将记录信息插入数据库
            PracticeRecord practiceRecord = new PracticeRecord();
            practiceRecord.setCourseId(courseId);
            practiceRecord.setTeachplanId(teacherId);
            practiceRecord.setId(recordId);
            practiceRecord.setUserId(userDto.getId());
            practiceRecord.setPracticeStatus("604001");
            practiceRecord.setSubjects(questionQueryRequest.getSubjects());
            practiceRecord.setQualifications(questionQueryRequest.getQualifications());
            practiceRecordMapper.insert(practiceRecord);
        }
        //是否有创建准确率表
        String lockKey = "lock:practice_record:" + username;
        //设置分布式锁，以防止用户使用多个设备同时创建记录
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(5));
        if (Boolean.TRUE.equals(lockAcquired)) {
           try {
               QueryWrapper<Accuracy> accuracyQueryWrapper = new QueryWrapper<>();
               accuracyQueryWrapper.eq("user_id", userDto.getId());
               Accuracy accuracy = accuracyMapper.selectOne(accuracyQueryWrapper);
               if(accuracy == null){
                   Accuracy accuracy1 = new Accuracy();
                   accuracy1.setUserId(userDto.getId());
                   accuracyMapper.insert(accuracy1);
               }
           }finally {
               redisTemplate.delete(lockKey);
           }
        }

        //记录id是否为空
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();

        Map<String,Wrapper<Question>> questionMap = new HashMap<>();
        String subjects = questionQueryRequest.getSubjects();
        queryWrapper.eq(CommonConstant.SUBJECTS,subjects);
        queryWrapper.last(SqlConstants.SORT);
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();


        // 拼接查询条件
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);


        questionMap.put(String.valueOf(recordId),queryWrapper);


        return questionMap;
    }

    private void addQuestion(QuestionDataVO questionDataVO) {
        Question question = new Question();
        BeanUtils.copyProperties(questionDataVO, question);
        this.save(question);
    }

}




