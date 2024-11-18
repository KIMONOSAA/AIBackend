package com.kimo.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.common.ErrorCode;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.feignclient.UserClient;
import com.kimo.mapper.CourseLearnRecordMapper;
import com.kimo.model.dto.CourseLearnRecordTimeDto;
import com.kimo.model.dto.CourseTeachPlanRecord;
import com.kimo.model.dto.QueryCourseParamsDto;
import com.kimo.model.dto.UserDto;
import com.kimo.model.po.CourseBase;
import com.kimo.model.po.CourseLearnRecord;
import com.kimo.service.CourseLearnRecordService;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public Boolean updateCourseRecord(CourseLearnRecordTimeDto courseLearnRecordTimeDto, HttpServletRequest request) {
        ThrowUtils.throwIf(courseLearnRecordTimeDto.getCourseId() == null,ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(courseLearnRecordTimeDto.getCourseName() == null,ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(courseLearnRecordTimeDto.getCourseTeachPlanRecord() == null,ErrorCode.NOT_FOUND_ERROR);
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        ThrowUtils.throwIf(userDto == null, ErrorCode.NOT_LOGIN_ERROR);

        QueryWrapper<CourseLearnRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseLearnRecordTimeDto.getCourseId());
        queryWrapper.eq("user_id", userDto.getId());
        CourseLearnRecord courseLearnRecord = courseLearnRecordMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(courseLearnRecord == null, ErrorCode.NOT_FOUND_ERROR);

        String courseTeachplan = courseLearnRecord.getCourseTeachplan();
        ObjectMapper objectMapper = new ObjectMapper();
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
                courseTeachPlanRecord.setTimelength(courseLearnRecordTimeDto.getCourseTeachPlanRecord().getTimelength());
                courseTeachPlanRecord.setTotalTime(courseLearnRecordTimeDto.getCourseTeachPlanRecord().getTotalTime());
            }

            // 序列化为 JSON
            String jsonOutput = objectMapper.writeValueAsString(list);
            courseLearnRecord.setCourseTeachplan(jsonOutput);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
        }

        int i = courseLearnRecordMapper.updateById(courseLearnRecord);

        return i == 1;
    }

    @Override
    public Wrapper<CourseLearnRecord> getQueryWrapper(long id, QueryCourseParamsDto queryCourseParams, HttpServletRequest request) {
        // Use QueryWrapper instead of LambdaQueryWrapper to support dynamic field names
        QueryWrapper<CourseLearnRecord> queryWrapper = new QueryWrapper<>();
        // 构建查询条件，根据课程名称查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParams.getCourseName()), "course_name", queryCourseParams.getCourseName());

        queryWrapper.eq(SqlConstants.USER_ID, id);

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




