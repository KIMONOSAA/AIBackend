package com.kimo.service.impl;


import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimo.common.ErrorCode;
import com.kimo.constant.CourseConstant;
import com.kimo.constant.RedisConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.feignclient.UserClient;
import com.kimo.mapper.*;

import com.kimo.model.dto.*;

import com.kimo.model.po.*;
import com.kimo.service.CourseBaseService;
import com.kimo.service.CoursePublishService;
import com.kimo.service.TeachplanService;
import com.kimo.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

import static com.kimo.constant.CaffeineConstant.*;

/**
 * @author Mr.kimo
 */
@Service
@Slf4j
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish>
    implements CoursePublishService {

    @Autowired
    private CourseBaseService courseBaseInfoService;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    private UserClient userClient;

    @Autowired
    private RedissonClient redissonClient;


    @Autowired
    private ServletUtils servletUtils;

    @Autowired
    private CourseLearnRecordMapper courseLearnRecordMapper;

    @Autowired
    private CourseBaseService courseBaseService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;


    @Autowired
    private ExecutorService executorService;


    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId,HttpServletRequest request,CourseLearnRecordDto courseLearnRecordDto) {
        //        //获取用户信息

        UserDto userDtoForRedisOrLock = courseBaseService.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
        ThrowUtils.throwIf(userDtoForRedisOrLock == null,ErrorCode.NOT_LOGIN_ERROR);
        Long userId = userDtoForRedisOrLock.getId();

        //课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);

        //判断是否收费是否会员
        String charge = courseBaseInfo.getCharge();
        ThrowUtils.throwIf(userDtoForRedisOrLock.getMember() == null,ErrorCode.OPERATION_ERROR);
        if(charge.equals("201001") && userDtoForRedisOrLock.getMember().equals("605003")){
            throw new BusinessException(ErrorCode.IS_NOT_MEMBER);
        }

        String courseBroweseId = CourseConstant.COURSE_BROWSES + userId + courseBaseInfo.getId();
//                //课程浏览加+1
        addCourseBrowses(courseBroweseId,userId,courseBaseInfo.getId());

//        课程计划信息
        List<TeachplanListDto> teachplanTreeRedis = courseBaseInfoService.findTeachplanTreeRedis(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTreeRedis);
        return coursePreviewDto;


    }



    private void addCourseBrowses(String courseBroweseId,Long userId,Long courseId) {

        try {
            Boolean member = redisTemplate.opsForSet().isMember(courseBroweseId, userId.toString());
            if(BooleanUtils.isFalse(member)){
                QueryWrapper<CourseBase> queryWrapper  = new QueryWrapper<>();
                queryWrapper.eq("id",courseId);
                CourseBase courseBase = courseBaseMapper.selectOne(queryWrapper);
                courseBase.setBrowses(courseBase.getBrowses() + 1);
                int i = courseBaseMapper.updateById(courseBase);
                if(i > 0){
                    redisTemplate.opsForSet().add(courseBroweseId,userId.toString());
                }
            }
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Override
    @Transactional
    public void commitAudit(Long courseId, HttpServletRequest request) {
        UserDto userDtoForRedisOrLock = courseBaseService.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
        ThrowUtils.throwIf(userDtoForRedisOrLock == null,ErrorCode.NOT_LOGIN_ERROR);
        Long userId = userDtoForRedisOrLock.getId();
        //约束校验
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        //当前审核状态为已提交不允许再次提交
        if("202003".equals(auditStatus)){
            throw new BusinessException(ErrorCode.COURSE_STATUS_AUDIT_ERROR);
        }

        if(!courseBase.getManagerId().equals(userDtoForRedisOrLock.getId())){
            throw new BusinessException(ErrorCode.COURSE_COMMIT_NOT_ERROR);
        }

        //课程图片是否填写
        if(StringUtils.isEmpty(courseBase.getPic())){
            throw new BusinessException(ErrorCode.UPLOAD_PICTURE_FOUND);
        }
        //查询课程计划信息
        List<TeachplanListDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if(teachplanTree.size() <= 0){
            throw new BusinessException(ErrorCode.TEACH_PLAN_ERROR);
        }
        //添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //课程基本信息加部分营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转为json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        //将课程营销信息json数据放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);
        //转json
        String teachplanTreeString = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeString);

        //设置预发布记录状态,已提交
        coursePublishPre.setStatus("202003");
        //教学机构id
        coursePublishPre.setManagerId(userId);
        coursePublishPre.setManager(userDtoForRedisOrLock.getUserName());
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreUpdate == null){
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Transactional
    @Override
    public void publish(HttpServletRequest request, Long courseId) {
        UserDto userDtoForRedisOrLock = courseBaseService.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
        ThrowUtils.throwIf(userDtoForRedisOrLock == null,ErrorCode.NOT_LOGIN_ERROR);
        String caffeineCourseIdKey = CAFFEINE_COURSE + courseId;
        String caffeineCourseMarketKey = CAFFEINE_COURSE_MARKET + courseId;
        //约束校验
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        ThrowUtils.throwIf(coursePublishPre == null, ErrorCode.COURSE_AUDIT_IS_NOT_ERROR);
        //本机构只允许提交本机构的课程
        if(!coursePublishPre.getManagerId().equals(userDtoForRedisOrLock.getId())){
            throw new BusinessException(ErrorCode.COURSE_COMMIT_NOT_ERROR);
        }


        //课程审核状态
        String auditStatus = coursePublishPre.getStatus();
        //审核通过方可发布
        if(!"202004".equals(auditStatus)){
            throw new BusinessException(ErrorCode.COURSE_AUDIT_STATUS_ERROR);
        }

        //保存课程发布信息
        saveCoursePublish(courseId);

        //删除redis数据库缓存
        deleteRedisCache(caffeineCourseIdKey,caffeineCourseMarketKey);

        //保存消息表
//        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);
    }

    private void deleteRedisCache(String caffeineCourseIdKey,String caffeineCourseMarketKey) {
        redisTemplate.delete(caffeineCourseIdKey);
        redisTemplate.delete(caffeineCourseMarketKey);
    }

    /**
     * @description 保存课程发布信息
     * @param courseId  课程id
     * @return void

     */
    private void saveCoursePublish(Long courseId){
        //整合课程发布信息
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        ThrowUtils.throwIf(coursePublishPre == null, ErrorCode.COURSE_PUBLIC_FOUND_ERROR);

        CoursePublish coursePublish = new CoursePublish();

        //拷贝到课程发布对象
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus("2003002");
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if(coursePublishUpdate == null){
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }
        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);

    }



    @Override
    public CoursePublish getCoursePublish(Long courseId){
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish ;
    }
}




