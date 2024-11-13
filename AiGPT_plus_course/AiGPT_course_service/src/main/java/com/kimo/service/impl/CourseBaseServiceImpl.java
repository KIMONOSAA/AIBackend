package com.kimo.service.impl;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.kimo.common.ErrorCode;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.feignclient.UserClient;
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


    @Override
    public Wrapper<CourseBase> getQueryWrapper(Long id,QueryCourseParamsDto queryCourseParams, HttpServletRequest request) {

        // Use QueryWrapper instead of LambdaQueryWrapper to support dynamic field names
        QueryWrapper<CourseBase> queryWrapper = new QueryWrapper<>();
        // 构建查询条件，根据课程名称查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParams.getCourseName()), "name", queryCourseParams.getCourseName());
        // 构建查询条件，根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParams.getAuditStatus()), "audit_status", queryCourseParams.getAuditStatus());
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParams.getPublishStatus()), "status", queryCourseParams.getPublishStatus());
        // 根据课程发布状态查询（根据需要进行修改）
        queryWrapper.eq(SqlConstants.COURSE_USERID, id);

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

    @Override
    public Wrapper<CourseBase> getQueryWrapperList(CoursePublishListDto coursePublishListDto, HttpServletRequest request) {
        QueryWrapper<CourseBase> queryWrapper = new QueryWrapper<>();

//        queryWrapper.lambda().like(StringUtils.isNotEmpty(coursePublishListDto.getName()), CourseBase::getName, coursePublishListDto.getName());

        queryWrapper.lambda().eq(StringUtils.isNotEmpty(coursePublishListDto.getTags()), CourseBase::getTags, coursePublishListDto.getTags());

//        queryWrapper.lambda().eq(StringUtils.isNotEmpty(coursePublishListDto.getBrowses().toString()), CourseBase::getBrowses, coursePublishListDto.getBrowses());

//        queryWrapper.lambda().eq(StringUtils.isNotEmpty(coursePublishListDto.getId().toString()),CourseBase::getId, coursePublishListDto.getId());

        queryWrapper.eq("status", "203002");

//        queryWrapper.lambda().eq(StringUtils.isNotEmpty(coursePublishListDto.getGrade()),CourseBase::getGrade, coursePublishListDto.getGrade());

        // 获取排序字段和排序顺序
        String sortField = coursePublishListDto.getSortField();
        String sortOrder = coursePublishListDto.getSortOrder();

        // 拼接查询条件，判断排序顺序并使用相应的方法
        if (SqlUtils.validSortField(sortField)) {
            if (CommonConstant.SORT_ORDER_ASC.equals(sortOrder)) {
                queryWrapper.orderByAsc(sortField);
            } else {
                queryWrapper.orderByDesc(sortField);
            }
        }
        return queryWrapper;
    }

    @Override
    public UserDto getUserDtoForRedisOrLock(HttpServletRequest request,String type){
        UserDto userDto = new UserDto();
        Object user = servletUtils.getHeaderRedisForUser(request, type);

        if (user == null) {
            String headerKey = servletUtils.getHeaderKey(request, type);
            String redis_user_key =  CAFFEINE_USER + headerKey;
            if (StringUtils.isNotBlank(headerKey)) {
                RLock lock = redissonClient.getLock(CAFFEINE_USER_LOCKED + headerKey);
                boolean isLocked = false;
                try {
                    // 尝试获取锁，等待10秒，锁超时1秒自动释放
                    isLocked = lock.tryLock(10, 1, TimeUnit.SECONDS);
                    if (isLocked) {
                        String username = servletUtils.getHeader(request, type);
                        userDto = userClient.GobalGetLoginUser(username);
                        // 如果成功获取锁，进行数据库查询和缓存更新
                        if (userDto == null) {
                            // 将空值写入redis 防止缓存穿透
                            redisTemplate.opsForValue().set(redis_user_key, "", 1L, TimeUnit.HOURS);
                            throw new BusinessException(ErrorCode.USER_IS_NOT); // 返回 null 表示没有找到课程
                        }
                        syncToRedis(redis_user_key,userDto);
                    }
                } catch (InterruptedException e) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }finally {
                    if (isLocked){
                        lock.unlock();
                    }
                }
            }

        }else {

            BeanUtils.copyProperties(user,userDto);

        }
        return userDto;
    }

    public void getUserDtoForRedis(UserDto userDto,HttpServletRequest request,String type){
        Object user = servletUtils.getHeaderRedisForUser(request, type);
        if (user == null) {
            String headerKey = servletUtils.getHeaderKey(request, type);
            if (StringUtils.isNotBlank(headerKey)) {
                String username = servletUtils.getHeader(request, type);
                userDto = userClient.GobalGetLoginUser(username);
                // 如果成功获取锁，进行数据库查询和缓存更新
                if (userDto == null) {
                    // 将空值写入redis 防止缓存穿透
                    redisTemplate.opsForValue().set(headerKey, "", 1L, TimeUnit.HOURS);
                    throw new BusinessException(ErrorCode.USER_IS_NOT); // 返回 null 表示没有找到课程
                }
                syncToCache(headerKey,userDto);

            }
        }else {
            BeanUtils.copyProperties(user,userDto);
        }
    }

    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase( AddCourseDto addCourseDto,HttpServletRequest request) {
        UserDto userDtoForRedisOrLock = this.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
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


    //根据课程id查询课程基本信息，包括基本信息和营销信息
    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
//        CourseBase courseBase = courseBaseMapper.selectById(courseId);
//        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
//        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
//        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
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

//        long statTime = System.currentTimeMillis();
//        log.info("当前线程ID：" + Thread.currentThread().getId() + "," + "当前线程名：" + Thread.currentThread().getName() + "当前开始时间:" + statTime + "ms");

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

//        long endTime = System.currentTimeMillis();
//        log.info("当前线程ID：" + Thread.currentThread().getId() + "," + "当前线程名：" + Thread.currentThread().getName() + "当前结束时间:" + endTime + "ms");
//        log.info("当前线程ID：" + Thread.currentThread().getId() + "," + "当前线程名：" + Thread.currentThread().getName() + "花费时间" + (endTime - statTime)  + "ms");
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
//        return data.toString();
    }

    private byte[] serializeForCompress(Object data) {
        try {
            String JsonData = objectMapper.writeValueAsString(data);
            return compress(JsonData);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_EXCEPTION);
        }
//        return data.toString();
    }

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
    public CourseBaseInfoDto updateCourseBase(EditCourseDto editCourseDto,HttpServletRequest request) {
        UserDto userDtoForRedisOrLock = this.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
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
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
//        CourseBase courseBase = courseBaseMapper.selectById(courseId);
//        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
//        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
//        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        String caffeineCourseIdKey = CAFFEINE_COURSE + courseId;

        // 从 Caffeine 或 Redis 获取缓存数据
        CourseBase courseBaseCache = (CourseBase) getFromCaffeineOrRedis(caffeineCourseIdKey, CourseBase.class);

        if (courseBaseCache != null) {
//            BeanUtils.copyProperties(courseBaseCache, courseBaseInfoDto);
            return courseBaseCache;
        }


//        RLock lock = redissonClient.getLock(CAFFEINE_COURSE_MARKET_LOCKED + courseId);
//        boolean isLocked = false;
//        try {
//            // 尝试获取锁，等待10秒，锁超时1秒自动释放
//            isLocked = lock.tryLock(10, 1, TimeUnit.SECONDS);
//            if (isLocked) {
//                // 如果成功获取锁，进行数据库查询和缓存更新
//                return fetchAndUpdateCourseDataOrRedis(courseId, courseBaseInfoDto, caffeineCourseIdKey);
//
//            }
//        } catch (InterruptedException e) {
////            Thread.currentThread().interrupt(); // 重置中断状态
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//        }finally {
//            if (isLocked){
//                lock.unlock();
//            }
//        }
        return null;
    }

    @Override
    public CourseBase getCourseBaseInfoN(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);

        return courseBase;
    }
    @Override
    public Boolean deletedTeachplanOrMedia(Long teachId,Long courseId, HttpServletRequest request) {
        UserDto userDtoForRedisOrLock = this.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
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
        teachplanMediaQueryWrapper.eq("user_id", userId);
        teachplanMediaQueryWrapper.eq("teachplan_id",teachId);
        TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(teachplanMediaQueryWrapper);
        int isTeachplanMedia = teachplanMediaMapper.deleteById(teachplanMedia.getId());

        int isTeachplan = teachplanMapper.deleteById(teachId);
        return isTeachplan == 1 && isTeachplanMedia == 1;
    }



    @Override
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
    public Boolean deletedCourseBase(Long courseId, HttpServletRequest request) {
        UserDto userDtoForRedisOrLock = this.getUserDtoForRedisOrLock(request, SecurityConstants.AUTHORIZATION_HEADER);
        ThrowUtils.throwIf(userDtoForRedisOrLock == null,ErrorCode.NOT_LOGIN_ERROR);
        Long userId = userDtoForRedisOrLock.getId();
        CourseBase courseBase = courseBaseMapper.selectById(userId);
        ThrowUtils.throwIf(courseBase == null,ErrorCode.COURSE_NOT_FOUND);
        courseBaseMapper.deleteById(courseId);
        return true;
    }

    private List<TeachplanListDto> getFromRedis(String redisTeachplan) {
        List<TeachplanListDto> data = null;
        // 如果 Caffeine 没有，查 Redis
        String redisData = redisTemplate.opsForValue().get(redisTeachplan);
        if(StringUtils.isNotBlank(redisData)){
            data = redisSerialize(redisData);
            return data;
        }
        // 判断命中的是否是空值
        if (redisData != null) {
            // 返回一个错误信息
            return null;
        }
        return data;
    }

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


    private List<TeachplanListDto> redisSerialize(String redisData) {
        try {
            return objectMapper.readValue(redisData, new TypeReference<List<TeachplanListDto>>() {});
        } catch (Exception e) {
            // 处理异常，如日志记录或抛出运行时异常
            e.printStackTrace();
            return null;
        }
    }

    private CourseBase fetchAndUpdateCourseDataOrRedis(Long courseId, CourseBaseInfoDto courseBaseInfoDto, String caffeineCourseIdKey) {
//        long statTime = System.currentTimeMillis();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            // 将空值写入redis 防止缓存穿透
            redisTemplate.opsForValue().set(caffeineCourseIdKey, "", 1L, TimeUnit.HOURS);
            return null; // 返回 null 表示没有找到课程
        }

        // 更新缓存
        syncToCache(caffeineCourseIdKey, courseBase);

        return courseBase;
//        long endTime = System.currentTimeMillis();
//        log.info("当前线程ID：" + Thread.currentThread().getId() + "," + "当前线程名：" + Thread.currentThread().getName() + "花费时间" + (endTime - statTime)  + "ms");
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




