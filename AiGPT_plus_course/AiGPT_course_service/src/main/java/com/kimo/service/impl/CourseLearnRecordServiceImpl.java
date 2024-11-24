package com.kimo.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.api.client.UserClient;
import com.kimo.api.dto.UserDto;
import com.kimo.common.ErrorCode;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;

import com.kimo.mapper.CourseLearnRecordMapper;
import com.kimo.model.dto.CourseLearnRecordTimeDto;
import com.kimo.model.dto.CourseTeachPlanRecord;
import com.kimo.model.dto.QueryCourseParamsDto;

import com.kimo.model.po.CourseBase;
import com.kimo.model.po.CourseLearnRecord;
import com.kimo.service.CourseBaseService;
import com.kimo.service.CourseLearnRecordService;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mr.kimo
 */
@Service
public class CourseLearnRecordServiceImpl extends ServiceImpl<CourseLearnRecordMapper, CourseLearnRecord>
    implements CourseLearnRecordService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private ServletUtils servletUtils;

    @Autowired
    private CourseLearnRecordMapper courseLearnRecordMapper;

    @Autowired
    private CourseBaseService courseBaseService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public Boolean updateCourseRecord(CourseLearnRecordTimeDto courseLearnRecordTimeDto, HttpServletRequest request) {
        ThrowUtils.throwIf(courseLearnRecordTimeDto.getCourseId() == null,ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(courseLearnRecordTimeDto.getCourseName() == null,ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(courseLearnRecordTimeDto.getCourseTeachPlanRecord() == null,ErrorCode.NOT_FOUND_ERROR);
        UserDto userDtoForRedisOrLock = courseBaseService.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
        ThrowUtils.throwIf(userDtoForRedisOrLock == null, ErrorCode.NOT_LOGIN_ERROR);

        QueryWrapper<CourseLearnRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseLearnRecordTimeDto.getCourseId());
        queryWrapper.eq("user_id", userDtoForRedisOrLock.getId());
        CourseLearnRecord courseLearnRecord = courseLearnRecordMapper.selectOne(queryWrapper);

        if(courseLearnRecord == null) {
            CourseLearnRecord courseLearnRecord1 = new CourseLearnRecord();
            courseLearnRecord1.setUserId(userDtoForRedisOrLock.getId());
            courseLearnRecord1.setUserName(userDtoForRedisOrLock.getUserName());
            courseLearnRecord1.setCourseName(courseLearnRecordTimeDto.getCourseName());
            courseLearnRecord1.setCourseId(courseLearnRecordTimeDto.getCourseId());
            CourseTeachPlanRecord courseTeachPlanRecord = courseLearnRecordTimeDto.getCourseTeachPlanRecord();
            String timelength = courseTeachPlanRecord.getTimelength();
            String totalTime = courseTeachPlanRecord.getTotalTime();
            String timeFormat = convertSecondsToTimeFormat(timelength);
            String totalFormat = convertSecondsToTimeFormat(totalTime);
            courseTeachPlanRecord.setTimelength(timeFormat);
            courseTeachPlanRecord.setTotalTime(totalFormat);
            ArrayList<CourseTeachPlanRecord> courseTeachPlanRecords = new ArrayList<>();

            courseTeachPlanRecords.add(courseTeachPlanRecord);
            String courseTeachPlanRecordJson = null;
            try {
                courseTeachPlanRecordJson = objectMapper.writeValueAsString(courseTeachPlanRecords);
            } catch (JsonProcessingException e) {
                throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
            }

            courseLearnRecord1.setCourseTeachplan(courseTeachPlanRecordJson);
            courseLearnRecord1.setCreatetime(new Date());
            courseLearnRecord1.setUpdatetime(new Date());
            int insert = courseLearnRecordMapper.insert(courseLearnRecord1);

            return insert == 1;

        }else {
            String courseTeachplan = courseLearnRecord.getCourseTeachplan();
            List<CourseTeachPlanRecord> list;

            try {
                // 反序列化 JSON
                list = Optional.ofNullable(courseTeachplan)
                        .filter(str -> !str.isEmpty())
                        .map(str -> {
                            try {
                                return objectMapper.readValue(str, new TypeReference<List<CourseTeachPlanRecord>>() {});
                            } catch (JsonProcessingException e) {
                                throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
                            }
                        })
                        .orElseGet(ArrayList::new);

                // 将列表转换为以 ID 为键的 Map，过滤掉 id 为 null 的记录
                Map<Integer, CourseTeachPlanRecord> recordMap = list.stream()
                        .filter(record -> record.getId() != null) // 过滤掉 id 为 null 的记录
                        .collect(Collectors.toMap(CourseTeachPlanRecord::getId, record -> record));

                int idToFind = courseLearnRecordTimeDto.getCourseTeachPlanRecord().getId();
                CourseTeachPlanRecord courseTeachPlanRecord = recordMap.get(idToFind);

                if (courseTeachPlanRecord == null) {

                    // 如果未找到，添加到列表
                    list.add(courseLearnRecordTimeDto.getCourseTeachPlanRecord());
                } else {
                    // 更新已有记录
                    courseTeachPlanRecord.setDescription(courseLearnRecordTimeDto.getCourseTeachPlanRecord().getDescription());
                    courseTeachPlanRecord.setLabel(courseLearnRecordTimeDto.getCourseTeachPlanRecord().getLabel());
                    courseTeachPlanRecord.setPname(courseLearnRecordTimeDto.getCourseTeachPlanRecord().getPname());
                    courseTeachPlanRecord.setTimelength(convertSecondsToTimeFormat(courseLearnRecordTimeDto.getCourseTeachPlanRecord().getTimelength()));
                    courseTeachPlanRecord.setTotalTime(convertSecondsToTimeFormat(courseLearnRecordTimeDto.getCourseTeachPlanRecord().getTotalTime()));
                }

                // 序列化为 JSON
                String jsonOutput = objectMapper.writeValueAsString(list);
                courseLearnRecord.setCourseTeachplan(jsonOutput);
            } catch (JsonProcessingException e) {
                throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
            }

        }

        int i = courseLearnRecordMapper.updateById(courseLearnRecord);

        return i == 1;
    }

    private String convertSecondsToTimeFormat(String secondsStr) {
        try {
            // 将输入字符串转换为 double
            double seconds = Double.parseDouble(secondsStr);

            // 计算小时、分钟和秒
            int totalSeconds = (int) seconds;
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int remainingSeconds = totalSeconds % 60;

            // 保留小数部分（如果需要）
            double fractionalSeconds = seconds - totalSeconds;

            // 如果要显示秒的小数部分（如毫秒）：
            // String formattedSeconds = String.format("%02d.%03d", remainingSeconds, (int) (fractionalSeconds * 1000));

            // 格式设置为时间字符串
            return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
        } catch (NumberFormatException e) {

            System.err.println("Invalid input, please provide a valid number: " + secondsStr);
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION,"时间格式异常");
        }
    }

    @Override
    public Wrapper<CourseLearnRecord> getQueryWrapper(QueryCourseParamsDto queryCourseParams, HttpServletRequest request) {

        UserDto userDtoForRedisOrLock = courseBaseService.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
        ThrowUtils.throwIf(userDtoForRedisOrLock == null, ErrorCode.NOT_LOGIN_ERROR);
        // Use QueryWrapper instead of LambdaQueryWrapper to support dynamic field names
        QueryWrapper<CourseLearnRecord> queryWrapper = new QueryWrapper<>();
        // 构建查询条件，根据课程名称查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParams.getCourseName()), "course_name", queryCourseParams.getCourseName());

        queryWrapper.eq(SqlConstants.USER_ID, userDtoForRedisOrLock.getId());

        // 获取排序字段和排序顺序
        String sortField = queryCourseParams.getSortField();
        String sortOrder = queryCourseParams.getSortOrder();

        // 拼接查询条件，判断排序顺序并使用相应的方法
        if (SqlUtils.validSortField(sortField)) {
            if (CommonConstant.SORT_ORDER_ASC.equals(sortOrder)) {
                queryWrapper.orderByAsc(sortOrder);
            } else {
                queryWrapper.orderByDesc(sortOrder);
            }
        }
        return queryWrapper;
    }
}




