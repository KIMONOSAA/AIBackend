package com.kimo.service.impl;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.kimo.api.client.PermissionsClient;
import com.kimo.api.client.UserClient;
import com.kimo.api.dto.Permissions;
import com.kimo.api.dto.UserDto;
import com.kimo.common.ErrorCode;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;

import com.kimo.mapper.CourseBaseMapper;
import com.kimo.mapper.CourseMarketMapper;
import com.kimo.mapper.TeachplanMapper;
import com.kimo.mapper.TeachplanMediaMapper;

import com.kimo.model.dto.*;
import com.kimo.model.po.CourseBase;
import com.kimo.model.po.CourseMarket;
import com.kimo.model.po.Teachplan;
import com.kimo.model.po.TeachplanMedia;
import com.kimo.service.CourseBaseService;
import com.kimo.service.TeachplanMediaService;
import com.kimo.service.TeachplanService;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.kimo.constant.CaffeineConstant.*;
import static com.kimo.constant.RedisConstant.MAX_INSERT_REDIS;


/**
 * @author Mr.kimo
 */
@Service
@Slf4j
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase>
    implements CourseBaseService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    Cache<String, Object> commonCaffeine;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private UserClient userClient;


    @Autowired
    private ServletUtils servletUtils;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Autowired
    private RedisTemplate<String, byte[]> redisByteTemplate;

    // 初始化 ObjectMapper 为静态成员，避免多次实例化
    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private PermissionsClient permissionsClient;



    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 18:32
     * @return: com.baomidou.mybatisplus.core.conditions.Wrapper<com.kimo.model.po.CourseBase>
     * @Param: [java.lang.Long, com.kimo.model.dto.QueryCourseParamsDto, jakarta.servlet.http.HttpServletRequest]
     * @Description: 分页获取当前用户的课程信息
     */
    public Wrapper<CourseBase> getQueryWrapper(Long id, QueryCourseParamsDto queryCourseParams, HttpServletRequest request) {
        // 创建 QueryWrapper
        QueryWrapper<CourseBase> queryWrapper = new QueryWrapper<>();

        // 添加查询条件
        addConditionIfNotEmpty(queryWrapper, "name", queryCourseParams.getCourseName(), "like");
        addConditionIfNotEmpty(queryWrapper, "audit_status", queryCourseParams.getAuditStatus(), "eq");
        addConditionIfNotEmpty(queryWrapper, "status", queryCourseParams.getPublishStatus(), "eq");
        queryWrapper.eq(SqlConstants.COURSE_USERID, id); // 根据课程用户ID查询

        // 排序处理
        handleSort(queryWrapper, queryCourseParams.getSortField(), queryCourseParams.getSortOrder());

        return queryWrapper;
    }

    /**
     * 判断字段是否为空，若不为空则添加对应的查询条件。
     */
    private void addConditionIfNotEmpty(QueryWrapper<CourseBase> queryWrapper, String column, String value, String operator) {
        if (StringUtils.isNotEmpty(value)) {
            if ("like".equals(operator)) {
                queryWrapper.like(column, value);
            } else if ("eq".equals(operator)) {
                queryWrapper.eq(column, value);
            }
        }
    }

    /**
     * 处理排序逻辑
     */
    private void handleSort(QueryWrapper<CourseBase> queryWrapper, String sortField, String sortOrder) {
        if (SqlUtils.validSortField(sortField)) {
            if (CommonConstant.SORT_ORDER_ASC.equals(sortOrder)) {
                queryWrapper.orderByAsc(sortField); // 按升序排序
            } else if (CommonConstant.SORT_ORDER_DESC.equals(sortOrder)) {
                queryWrapper.orderByDesc(sortField); // 按降序排序
            }
        } else {
            // 若排序字段无效，可以根据需求抛出异常或使用默认排序
            throw new BusinessException(ErrorCode.INVALID_SORT_FIELD, "无效的字段：" + sortField);
        }
    }



    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 18:33
     * @return: com.baomidou.mybatisplus.core.conditions.Wrapper<com.kimo.model.po.CourseBase>
     * @Param: [com.kimo.model.dto.CoursePublishListDto, jakarta.servlet.http.HttpServletRequest]
     * @Description: 分页获取所有课程信息
     */
    public Wrapper<CourseBase> getQueryWrapperList(CoursePublishListDto coursePublishListDto, HttpServletRequest request) {
        QueryWrapper<CourseBase> queryWrapper = new QueryWrapper<>();
        UserDto userDtoForRedisOrLock = this.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);

//        String code = getRoleForPermission(userDtoForRedisOrLock);
//
//        ensuperAdminOrAdmin(code,"900001");
        ThrowUtils.throwIf(userDtoForRedisOrLock == null,ErrorCode.NOT_FOUND_ERROR);

        queryWrapper.lambda().eq(StringUtils.isNotEmpty(coursePublishListDto.getTags()), CourseBase::getTags, coursePublishListDto.getTags());

        queryWrapper.eq("status", "203002");

        // 获取排序字段和排序顺序
        String sortField = coursePublishListDto.getSortField();
        String sortOrder = coursePublishListDto.getSortOrder();

        handleSort(queryWrapper, sortField, sortOrder);
        return queryWrapper;
    }



    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 18:36
     * @return: com.kimo.model.dto.UserDto
     * @Param: [jakarta.servlet.http.HttpServletRequest, java.lang.String]
     * @Description: redis获取用户
     */
    public UserDto getUserDtoForRedisOrLock(HttpServletRequest request, String type) {
        UserDto userDto = new UserDto();
        // 尝试从Redis中获取用户数据
        Object user = servletUtils.getHeaderRedisForUser(request, type);

        if (user == null) {
            String headerKey = servletUtils.getHeaderKey(request, type);
            String redisUserKey = CAFFEINE_USER + headerKey;

            if (StringUtils.isNotBlank(headerKey)) {
                // 尝试获取分布式锁，避免缓存穿透
                if (tryLockAndFetchData(headerKey, redisUserKey, request, type, userDto)) {
                    return userDto;
                }
            }
        } else {
            // 从Redis中成功获取，复制属性
            BeanUtils.copyProperties(user, userDto);
        }

        return userDto;
    }

    /**
     * 尝试获取分布式锁并查询数据库更新Redis缓存
     */
    private boolean tryLockAndFetchData(String headerKey, String redisUserKey, HttpServletRequest request, String type, UserDto userDto) {
        RLock lock = redissonClient.getLock(CAFFEINE_USER_LOCKED + headerKey);
        boolean isLocked = false;
        try {
            // 尝试获取锁，等待10秒，锁超时1秒自动释放
            isLocked = lock.tryLock(10, 1, TimeUnit.SECONDS);
            if (isLocked) {
                String username = servletUtils.getHeader(request, type);
                userDto = userClient.GobalGetLoginUser(username);

                // 用户数据为空，防止缓存穿透
                if (userDto == null) {
                    redisTemplate.opsForValue().set(redisUserKey, "", 1L, TimeUnit.HOURS);
                    throw new BusinessException(ErrorCode.USER_IS_NOT); // 返回 null 表示没有找到用户
                }
                syncToRedis(redisUserKey, userDto); // 更新到 Redis
                return true;
            }
        } catch (InterruptedException e) {
            // 异常处理，添加日志
            log.error("尝试锁定时出错: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "锁定中断");
        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }
        return false;
    }



    @Override
    @Transactional
    /**
     * @Author: Mr.kimo
     * @Date: 18:36
     * @return: com.kimo.model.dto.CourseBaseInfoDto
     * @Param: [com.kimo.model.dto.AddCourseDto, jakarta.servlet.http.HttpServletRequest]
     * @Description: 创建课程
     */
    public CourseBaseInfoDto createCourseBase(AddCourseDto addCourseDto, HttpServletRequest request) {
        UserDto userDtoForRedisOrLock = this.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = getRoleForPermission(userDtoForRedisOrLock);

        ensuperAdminOrAdmin(code,"900001");
//        UserDto userDto = userClient.GobalGetLoginUser(username);
        //合法性校验
        ThrowUtils.throwIf(StringUtils.isBlank(addCourseDto.getName()), ErrorCode.COURSE_NAME_NOT_FOUND_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(addCourseDto.getGrade()), ErrorCode.COURSE_GRADE_NOT_FOUND_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(addCourseDto.getTeachmode()),ErrorCode.COURSE_TEACH_MODE_NOT_FOUND_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(addCourseDto.getUsers()), ErrorCode.COURSE_USER_NOT_FOUND_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(addCourseDto.getCharge()), ErrorCode.COURSE_CHARGE_NOT_FOUND_ERROR);
        //新增对象
        CourseBase courseBaseNew = new CourseBase();
        //将填写的课程信息赋值给新增对象
        BeanUtils.copyProperties(addCourseDto,courseBaseNew);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());
        courseBaseNew.setManagerId(userDtoForRedisOrLock.getId());
        courseBaseNew.setManager(userDtoForRedisOrLock.getUserName());

        courseBaseNew.setAuditStatus("202002");
        courseBaseNew.setStatus("203001");
        int insert = courseBaseMapper.insert(courseBaseNew);
        ThrowUtils.throwIf(insert <= 0,ErrorCode.ADD_COURSE_INFO_ERROR);
        //向课程营销表保存课程营销信息
        //课程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        Long courseId = courseBaseNew.getId();
        BeanUtils.copyProperties(addCourseDto,courseMarketNew);
        courseMarketNew.setId(courseId);
        int i = saveCourseMarket(courseMarketNew);
        ThrowUtils.throwIf(i <= 0,ErrorCode.SAVE_MARKET_ERROR);
        //查询课程基本信息及营销信息并返回
        return getCourseBaseInfo(courseId);
    }

    @Override
    public String getRoleForPermission(UserDto userDtoForRedisOrLock) {
        Long roleId = userDtoForRedisOrLock.getId();
        Permissions userPermissions = permissionsClient.getUserPermissions(String.valueOf(roleId));
        String code = userPermissions.getCode();
        return code;
    }

    @Override
    public void ensuperAdminOrAdmin(String code,String expected) {
        ArrayList<String> permissionList = new ArrayList<>();
        try {
            permissionList = objectMapper.readValue(code,new TypeReference<ArrayList<String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,e.getMessage());
        }
        boolean admin = permissionList.contains(expected);
        boolean superAdmin = permissionList.contains("114514");
        if(!admin && !superAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR,"不是管理员或超级管理员");
        }
    }


    //根据课程id查询课程基本信息，包括基本信息和营销信息
    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 18:37
     * @return: com.kimo.model.dto.CourseBaseInfoDto
     * @Param: [java.lang.Long]
     * @Description: 从caffeine和redis获取课程信息和营销信息
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        String caffeineCourseIdKey = CAFFEINE_COURSE + courseId;
        String caffeineCourseMarketKey = CAFFEINE_COURSE_MARKET + courseId;

        // 从 Caffeine 或 Redis 获取缓存数据
        CourseBase courseBaseCache = (CourseBase) getFromCaffeineOrRedis(caffeineCourseIdKey, CourseBase.class);
        CourseMarket courseMarketCache = (CourseMarket) getFromCaffeineOrRedis(caffeineCourseMarketKey, CourseMarket.class);

        if (courseBaseCache != null) {
            BeanUtils.copyProperties(courseBaseCache, courseBaseInfoDto);
        }
        if (courseMarketCache != null) {
            BeanUtils.copyProperties(courseMarketCache, courseBaseInfoDto);
        }

        if (courseBaseCache == null || courseMarketCache == null){
            RLock lock = redissonClient.getLock(CAFFEINE_COURSE_MARKET_LOCKED + courseId);
            boolean isLocked = false;
            try {
                // 尝试获取锁，等待10秒，锁超时1秒自动释放
                isLocked = lock.tryLock(10, 1, TimeUnit.SECONDS);
                if (isLocked) {
                    // 如果成功获取锁，进行数据库查询和缓存更新
                    fetchAndUpdateCourseData(courseId, courseBaseInfoDto, caffeineCourseIdKey, caffeineCourseMarketKey);
                }
            } catch (InterruptedException e) {
//            Thread.currentThread().interrupt(); // 重置中断状态
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }finally {
                 if (isLocked){
                     lock.unlock();
                 }
            }
        }

        return courseBaseInfoDto; // 返回最终结果
    }

    private void fetchAndUpdateCourseData(Long courseId, CourseBaseInfoDto courseBaseInfoDto, String caffeineCourseIdKey, String caffeineCourseMarketKey) {

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            // 将空值写入redis 防止缓存穿透
            redisTemplate.opsForValue().set(caffeineCourseIdKey, "", 1L, TimeUnit.HOURS);
            return; // 返回 null 表示没有找到课程
        }
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket != null) {
            // 将空值写入redis防止缓存穿透
            redisTemplate.opsForValue().set(caffeineCourseMarketKey, "", 1L, TimeUnit.HOURS);
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        // 更新缓存
        syncToCache(caffeineCourseIdKey, courseBase);
        syncToCache(caffeineCourseMarketKey, courseMarket);
    }

    // 从 Caffeine 或 Redis 获取数据，支持不同类型
    private <T> Object getFromCaffeineOrRedis(String cacheKey, Class<T> clazz) {
        Object data = commonCaffeine.getIfPresent(cacheKey);
        if (data == null) {
            // 如果 Caffeine 没有，查 Redis
            String redisData = redisTemplate.opsForValue().get(cacheKey);
            if(StringUtils.isNotBlank(redisData)){
                data = deserialize(redisData, clazz);
                commonCaffeine.put(cacheKey, data); // 同步到 Caffeine 缓存
                return data;
            }
            // 判断命中的是否是空值
            if (redisData != null) {
                // 返回一个错误信息
                return null;
            }

        }
        return data;
    }

    // 同步数据到 Redis 和 Caffeine
    private void syncToCache(String cacheKey, Object data) {
        if (data != null) {
            String serializedData = serialize(data);
            redisTemplate.opsForValue().set(cacheKey, serializedData, 1, TimeUnit.HOURS); // 同步到 Redis 缓存
            commonCaffeine.put(cacheKey, data); // 同步到 Caffeine 缓存
        }
    }


    private void syncToCacheForCompress(String cacheKey, Object data) {
        if (data != null) {
            byte[] compressedData = serializeForCompress(data);
            redisByteTemplate.opsForValue().set(cacheKey, compressedData, 1, TimeUnit.HOURS); // 同步到 Redis 缓存
            commonCaffeine.put(cacheKey, data); // 同步到 Caffeine 缓存
        }
    }

    private void syncToRedis(String cacheKey, Object data) {
        if (data != null) {
            String serializedData = serialize(data);
            redisTemplate.opsForValue().set(cacheKey, serializedData, 1, TimeUnit.HOURS); // 同步到 Redis 缓存
        }
    }

    // 序列化方法
    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);

        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
        }
    }


    // 序列化方法
    private byte[] serializeForCompress(Object data) {
        try {
            String JsonData = objectMapper.writeValueAsString(data);
            return compress(JsonData);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
        }
    }

    /**
     * @Author: Mr.kimo
     * @Date: 18:39
     * @return:
     * @Param:
     * @Description: 压缩
     */
    private byte[] compress(String data) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
        }
        return byteArrayOutputStream.toByteArray();
    }


    // 泛型序列化方法
    private <T> T deserialize(String redisData, Class<T> clazz) {
        try {
            return objectMapper.readValue(redisData, clazz);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
        }
    }

    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 18:39
     * @return: com.kimo.model.dto.CourseBaseInfoDto
     * @Param: [com.kimo.model.dto.EditCourseDto, jakarta.servlet.http.HttpServletRequest]
     * @Description: 修改课程信息
     */
    public CourseBaseInfoDto updateCourseBase(EditCourseDto editCourseDto, HttpServletRequest request) {
        UserDto userDtoForRedisOrLock = this.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = getRoleForPermission(userDtoForRedisOrLock);

        ensuperAdminOrAdmin(code,"900003");

        ThrowUtils.throwIf(userDtoForRedisOrLock == null,ErrorCode.NOT_LOGIN_ERROR);
        Long userId = userDtoForRedisOrLock.getId();
        Long courseId = editCourseDto.getId();
        String caffeineCourseIdKey = CAFFEINE_COURSE + courseId;
        String caffeineCourseMarketKey = CAFFEINE_COURSE_MARKET + courseId;
        QueryWrapper<CourseBase> queryWrapper = new QueryWrapper<>();
        if(courseId != null){
            queryWrapper.eq("id", courseId);
        }
        CourseBase courseBase = courseBaseMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(courseBase == null,ErrorCode.COURSE_NOT_FOUND);

        if(!courseBase.getManagerId().equals(userId)){
            throw  new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        //封装数据
        BeanUtils.copyProperties(editCourseDto,courseBase);
        courseBase.setCreateDate(LocalDateTime.now());

        int i = courseBaseMapper.updateById(courseBase);

        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        saveCourseMarket(courseMarket);
        //删除redis数据库缓存
        deleteRedisCache(caffeineCourseIdKey,caffeineCourseMarketKey);

        return this.getCourseBaseInfo(courseId);
    }

    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 18:40
     * @return: java.lang.Boolean
     * @Param: []
     * @Description: 获取热门课程
     */
    public Boolean getHotCoursesBase() {
        int courseCount = courseBaseMapper.getCourseCount();
        if(courseCount == 0){
            return false;
        }
        int step = MAX_INSERT_REDIS;
        int total = step % courseCount == 0 ? step / courseCount : step / courseCount + 1;
        for (int i = 0; i < total; i++) {
            List<CourseBase> courseListByPage = courseBaseMapper.getCourseListByPage(i * step, step);
            handleSaveRedis(courseListByPage);
        }
        return true;
    }

    @Override
    public CourseBase getCourseBaseInfoByRedis(Long courseId) {
        String caffeineCourseIdKey = CAFFEINE_COURSE + courseId;

        // 从 Caffeine 或 Redis 获取缓存数据
        CourseBase courseBaseCache = (CourseBase) getFromCaffeineOrRedis(caffeineCourseIdKey, CourseBase.class);

        if (courseBaseCache != null) {
            return courseBaseCache;
        }

        return null;
    }

    @Override
    public CourseBase getCourseBaseInfoN(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);

        return courseBase;
    }


    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 18:41
     * @return: java.lang.Boolean
     * @Param: [java.lang.Long, java.lang.Long, jakarta.servlet.http.HttpServletRequest]
     * @Description: 解除章节和media的关系
     */
    public Boolean deletedTeachplanOrMedia(Long teachId,Long courseId, HttpServletRequest request) {
        UserDto userDtoForRedisOrLock = this.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
        String code = getRoleForPermission(userDtoForRedisOrLock);

        ensuperAdminOrAdmin(code,"900004");
        ThrowUtils.throwIf(userDtoForRedisOrLock == null,ErrorCode.NOT_LOGIN_ERROR);
        Long userId = userDtoForRedisOrLock.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase==null){
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        if(!Objects.equals(courseBase.getManagerId(), userId)){
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        Teachplan teachplan = teachplanMapper.selectById(teachId);
        if(!Objects.equals(teachplan.getCourseId(), courseId)){
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        QueryWrapper<TeachplanMedia> teachplanMediaQueryWrapper = new QueryWrapper<>();
        teachplanMediaQueryWrapper.eq("course_id", courseId);
        teachplanMediaQueryWrapper.eq("teachplan_id",teachId);
        TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(teachplanMediaQueryWrapper);
        int isTeachplanMedia = teachplanMediaMapper.deleteById(teachplanMedia.getId());

        int isTeachplan = teachplanMapper.deleteById(teachId);
        String redis_teachplan = REDIS_COURSE_TEACHPLAN + courseId;
        Boolean delete = redisTemplate.delete(redis_teachplan);
        ThrowUtils.throwIf(!delete,ErrorCode.OPERATION_ERROR,"缓存删除错误");
        return isTeachplan == 1 && isTeachplanMedia == 1;
    }



    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 18:42
     * @return: java.util.List<com.kimo.model.dto.TeachplanListDto>
     * @Param: [java.lang.Long]
     * @Description: 从redis获取课程计划
     */
    public List<TeachplanListDto> findTeachplanTreeRedis(Long courseId) {
        String redis_teachplan = REDIS_COURSE_TEACHPLAN + courseId;
        List<TeachplanListDto> teachplanListDtos =  getFromRedisByte(redis_teachplan);
        if (teachplanListDtos != null){
            return teachplanListDtos;
        }
        RLock lock = redissonClient.getLock(CAFFEINE_COURSE_MARKET_LOCKED + courseId);
        boolean isLocked = false;
        try {
            // 尝试获取锁，等待10秒，锁超时1秒自动释放
            isLocked = lock.tryLock(10, 1, TimeUnit.SECONDS);
            if (isLocked) {
                // 如果成功获取锁，进行数据库查询和缓存更新
                List<TeachplanListDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
                if (teachplanTree == null) {
                    // 将空值写入redis 防止缓存穿透
                    redisTemplate.opsForValue().set(redis_teachplan, "", 1L, TimeUnit.HOURS);
                    return null;
                }

                // 更新缓存
                syncToCacheForCompress(redis_teachplan, teachplanTree);
                return teachplanTree;
            }
        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt(); // 重置中断状态
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }finally {
            if (isLocked){
                lock.unlock();
            }
        }
        return List.of();
    }


    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 18:42
     * @return: java.lang.Boolean
     * @Param: [java.lang.Long, jakarta.servlet.http.HttpServletRequest]
     * @Description: 删除课程
     */
    public Boolean deletedCourseBase(Long courseId, HttpServletRequest request) {

        UserDto userDtoForRedisOrLock = this.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = getRoleForPermission(userDtoForRedisOrLock);

        ensuperAdminOrAdmin(code,"900004");
        ThrowUtils.throwIf(userDtoForRedisOrLock == null,ErrorCode.NOT_LOGIN_ERROR);
        Long userId = userDtoForRedisOrLock.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        ThrowUtils.throwIf(courseBase == null,ErrorCode.COURSE_NOT_FOUND);

        courseBaseMapper.deleteById(courseId);


        return true;
    }

    @Override
    public Wrapper<CourseBase> getQueryWrapperListForMember(CoursePublishListDto coursePublishListDto, HttpServletRequest request) {
        QueryWrapper<CourseBase> queryWrapper = new QueryWrapper<>();
        UserDto userDtoForRedisOrLock = this.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = getRoleForPermission(userDtoForRedisOrLock);

        ensuperAdminOrAdmin(code,"900001");
        ThrowUtils.throwIf(userDtoForRedisOrLock == null,ErrorCode.NOT_FOUND_ERROR);

        queryWrapper.lambda().eq(StringUtils.isNotEmpty(coursePublishListDto.getTags()), CourseBase::getTags, coursePublishListDto.getTags());

//        queryWrapper.eq("status", "203002");

        // 获取排序字段和排序顺序
        String sortField = coursePublishListDto.getSortField();
        String sortOrder = coursePublishListDto.getSortOrder();

        handleSort(queryWrapper, sortField, sortOrder);
        return queryWrapper;
    }


    /**
     * @Author: Mr.kimo
     * @Date: 18:43
     * @return:
     * @Param:
     * @Description: 从redis获取章节列表
     */
    private List<TeachplanListDto> getFromRedisByte(String redisTeachplan) {
        List<TeachplanListDto> data = null;
        // 查 Redis
        byte[] redisData = redisByteTemplate.opsForValue().get(redisTeachplan);
        // 判断命中的是否是空值
        if (redisData != null) {
            byte[] decompressedData = null;
            try {
                decompressedData = decompressForRedis(redisData);

            } catch (IOException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            String jsonData = new String(decompressedData, StandardCharsets.UTF_8);
            data = redisSerialize(jsonData);
            return data;
        }
        return data;
    }

    /**
     * @Author: Mr.kimo
     * @Date: 18:43
     * @return:
     * @Param:
     * @Description: 解压缩
     */
    private byte[] decompressForRedis(byte[] redisData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(redisData);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPInputStream gis = new GZIPInputStream(bais)){
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0){
                baos.write(buffer, 0, len);
            }
        }
        return baos.toByteArray();
    }


    /**
     * @Author: Mr.kimo
     * @Date: 18:44
     * @return:
     * @Param:
     * @Description: 章节的序列化
     */
    private List<TeachplanListDto> redisSerialize(String redisData) {
        try {
            return objectMapper.readValue(redisData, new TypeReference<List<TeachplanListDto>>() {});
        } catch (Exception e) {
            // 处理异常，如日志记录或抛出运行时异常
            e.printStackTrace();
            return null;
        }
    }


    private void handleSaveRedis(List<CourseBase> courseListByPage) {
        String caffeineCourseIdKey = null;
        for (CourseBase course : courseListByPage){
            caffeineCourseIdKey = CAFFEINE_COURSE + course.getId();
//            String s = objectMapper.writeValueAsString(course);
            redisTemplate.opsForValue().set(caffeineCourseIdKey, serialize(course), 1, TimeUnit.HOURS);
        }
    }

    private void deleteRedisCache(String caffeineCourseIdKey,String caffeineCourseMarketKey) {
        // 将键的有效期设置为0（已失效）
        redisTemplate.delete(caffeineCourseIdKey);
        redisTemplate.delete(caffeineCourseMarketKey);
        commonCaffeine.invalidate(caffeineCourseIdKey);
        commonCaffeine.invalidate(caffeineCourseMarketKey);
    }

    //保存课程营销信息
    private int saveCourseMarket(CourseMarket courseMarketNew){
        //收费规则
        String charge = courseMarketNew.getCharge();
        if(StringUtils.isBlank(charge)){
            throw new RuntimeException("收费规则没有选择");
        }
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarketNew.getMemberPrice() == null || courseMarketNew.getMemberPrice().floatValue()<=0){
                throw new BusinessException(ErrorCode.COURSE_PRICE_NOT_FOUND_ERROR);
            }
        }
        //根据id从课程营销表查询
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarketNew.getId());
        if(courseMarketObj == null){
            return courseMarketMapper.insert(courseMarketNew);
        }else{
            BeanUtils.copyProperties(courseMarketNew,courseMarketObj);
            courseMarketObj.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }


}




