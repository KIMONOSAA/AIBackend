package com.kimo.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimo.common.ErrorCode;
import com.kimo.exception.BusinessException;
import com.kimo.mapper.CourseMarketMapper;
import com.kimo.model.po.CourseBase;
import com.kimo.model.po.CourseMarket;
import com.kimo.service.CourseMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.kimo.constant.CaffeineConstant.CAFFEINE_COURSE;
import static com.kimo.constant.CaffeineConstant.CAFFEINE_COURSE_MARKET;
import static com.kimo.constant.RedisConstant.MAX_INSERT_REDIS;

/**
 * @author Mr.kimo
 */
@Service
public class CourseMarketServiceImpl extends ServiceImpl<CourseMarketMapper, CourseMarket>
    implements CourseMarketService {

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    // 初始化 ObjectMapper 为静态成员，避免多次实例化
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Boolean getHotCoursesBaseMarket() {
        int courseCount = courseMarketMapper.getCourseMarketCount();
        if(courseCount == 0){
            return false;
        }
        int step = MAX_INSERT_REDIS;
        int total = step % courseCount == 0 ? step / courseCount : step / courseCount + 1;
        for (int i = 0; i < total; i++) {
            List<CourseMarket> courseListByPage = courseMarketMapper.getCourseMarketListByPage(i * step, step);
            handleSaveRedis(courseListByPage);
        }
        return true;
    }

    private void handleSaveRedis(List<CourseMarket> courseListByPage) {
        String caffeineCourseIdKey = null;
        for (CourseMarket course : courseListByPage){
            caffeineCourseIdKey = CAFFEINE_COURSE_MARKET + course.getId();
//            String s = objectMapper.writeValueAsString(course);
            redisTemplate.opsForValue().set(caffeineCourseIdKey, serialize(course), 1, TimeUnit.HOURS);
        }
    }

    // 序列化方法
    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
        }
//        return data.toString();
    }
}




