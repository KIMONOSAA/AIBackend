package com.kimo.ucenter.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimo.api.client.CheckCodeClient;
import com.kimo.api.dto.UserDto;
import com.kimo.common.*;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.exception.ThrowUtils;
import com.kimo.ucenter.mapper.PointMapper;
import com.kimo.ucenter.mapper.UserEnbleMapper;
import com.kimo.ucenter.mapper.UserMemberMapper;
import com.kimo.ucenter.model.dto.*;
import com.kimo.ucenter.model.po.*;

import com.kimo.utils.*;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.UserConstant;
import com.kimo.exception.BusinessException;

import com.kimo.ucenter.mapper.UserMapper;

import com.kimo.ucenter.model.vo.AuthentianResponse;
import com.kimo.ucenter.model.vo.UserVO;
import com.kimo.ucenter.service.TokenService;
import com.kimo.ucenter.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import static com.kimo.constans.AuthRedisConstant.REGISTER_USER_LOCK;
import static com.kimo.constans.PointConstant.VIP_MEMBER_CODE;
import static com.kimo.constans.UserConstant.AUTHORIZATION;
import static com.kimo.constans.UserConstant.BEARER;
import static com.kimo.constant.CaffeineConstant.CAFFEINE_USER;
import static com.kimo.constant.SecurityConstants.LOGIN_TYPE;
import static com.kimo.utils.CommonUtils.checkIfNull;


@Service
@Slf4j
/**
 * @Author: Mr.kimo
 * @Date: 9:26
 * @return:
 * @Param:
 * @Description: 用户服务
 */
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Autowired
    private UserEnbleMapper userEnbleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisVerificationUtil redisVerificationUtil;

    @Autowired
    private CheckCodeClient checkCodeClient;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenService tokenService;


    @Autowired
    private PointMapper pointMapper;

    @Autowired
    private UserMemberMapper userMemberMapper;

    @Autowired
    private ServletUtils  servletUtils;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;




    /**
     * @Author: Mr.kimo
     * @Date: 9:26
     * @Description: 用户服务 - 提供用户相关的服务方法，例如更新用户状态、积分、角色和权限等。
     */
    @Override
    @Transactional
    public boolean saveUserStatusSuccess(String memberTypeId, String memberType, String userId) {
        // 检查用户成员是否存在
        UserMember userMember = checkUserMemberExistence(memberTypeId);

        // 检查用户状态
        User user = checkUserExistence(userId);

        // 更新用户成员状态
        updateUserMemberStatus(user);

        // 用户的更新点
        updateUserPoints(user.getId().toString(), 1000L);



//        // 创建并保存用户角色
//        Roles role = createAndSaveRole(user.getId(), "VIP");
//
//        // 创建和保存角色的权限
//        createAndSavePermission(role.getId(), "user_vip_all", "会员专享");

        return true;
    }

    /**
     * @Author: Mr.kimo
     * @Date: 9:26
     * @Param: memberTypeId - 用户成员类型的唯一标识
     * @return: UserMember - 返回找到的用户成员对象
     * @Description: 检查用户成员是否存在于数据库中，如果不存在则抛出系统错误异常。
     */
    private UserMember checkUserMemberExistence(String memberTypeId) {
        UserMember userMember = userMemberMapper.selectById(memberTypeId);
        return checkIfNull(userMember, ErrorCode.SYSTEM_ERROR);
    }

    /**
     * @Author: Mr.kimo
     * @Date: 9:26
     * @Param: userId - 用户的唯一标识
     * @return: User - 返回找到的用户对象
     * @Description: 检查用户是否存在于数据库中，如果不存在则抛出未登录错误异常。
     */
    private User checkUserExistence(String userId) {
        User user = userMapper.selectById(userId);
        return checkIfNull(user, ErrorCode.NOT_LOGIN_ERROR);
    }

    /**
     * @Author: Mr.kimo
     * @Date: 9:26
     * @Param: user - 需要更新的用户对象
     * @Description: 更新用户的成员状态，将成员类型设为 "VIP"。
     *               如果更新失败，则抛出系统错误异常。
     */
    private void updateUserMemberStatus(User user) {
        user.setMember(VIP_MEMBER_CODE);
        int result = userMapper.updateById(user);
        ThrowUtils.throwIf(result <= 0, ErrorCode.SYSTEM_ERROR);
    }

    /**
     * @Author: Mr.kimo
     * @Date: 9:26
     * @Param: userId - 用户的唯一标识
     * @Param: points - 需要更新的积分值
     * @Description: 更新用户的积分信息。如果用户没有积分记录，将创建新的记录。
     *               更新失败时抛出系统错误异常。
     */
    private void updateUserPoints(String userId, long points) {
        QueryWrapper<Point> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        Point point = Optional.ofNullable(pointMapper.selectOne(queryWrapper))
                .orElseGet(() -> createNewPointRecord(userId));
        point.setPoint(points);
        int result = pointMapper.updateById(point);
        ThrowUtils.throwIf(result <= 0, ErrorCode.SYSTEM_ERROR);
    }

    /**
     * @Author: Mr.kimo
     * @Date: 9:37
     * @return:
     * @Param:
     * @Description: 没有就创建积分表数据
     */
    private Point createNewPointRecord(String userId) {
        Point newPoint = new Point();
        newPoint.setUserId(Long.parseLong(userId));
        newPoint.setPoint(100L);  // 设置初始积分值为 100
        newPoint.setCreateTime(LocalDateTime.now());
        newPoint.setUpdateTime(LocalDateTime.now());
        int insertResult = pointMapper.insert(newPoint);
        ThrowUtils.throwIf(insertResult <= 0, ErrorCode.SYSTEM_ERROR);  // 如果插入失败，抛出系统错误
        return newPoint;  // 返回新创建的积分记录
    }

//    /**
//     * @Author: Mr.kimo
//     * @Date: 9:26
//     * @Param: userId - 用户的唯一标识
//     * @Param: roleName - 需要创建的角色名称
//     * @return: Roles - 返回创建并保存的角色对象
//     * @Description: 为指定用户创建新的角色信息。如果保存失败，则抛出系统错误异常。
//     */
//    private Roles createAndSaveRole(Long userId, String roleName) {
//        long roleId = IdWorkerUtils.getInstance().nextId();
//        Roles role = new Roles();
//        role.setId(roleId);
//        role.setUserId(userId);
//        role.setRolesName(roleName);
//        boolean save = rolesService.save(role);
//        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR);
//        return role;
//    }

//    /**
//     * @Author: Mr.kimo
//     * @Date: 9:26
//     * @Param: roleId - 角色的唯一标识
//     * @Param: code - 权限代码
//     * @Param: permissionName - 权限名称
//     * @Description: 为指定角色创建新的权限信息。如果保存失败，则抛出系统错误异常。
//     */
//    private void createAndSavePermission(long roleId, String code, String permissionName) {
//        long permissionId = IdWorkerUtils.getInstance().nextId();
//        Permissions permissions = new Permissions();
//        permissions.setId(permissionId);
//        permissions.setRoleId(roleId);
//        permissions.setCode(code);
//        permissions.setPermissionName(permissionName);
//        boolean save = permissionsService.save(permissions);
//        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR);
//    }


    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 9:39
     * @return: com.kimo.ucenter.model.po.User
     * @Param: [jakarta.servlet.http.HttpServletRequest]
     * @Description: 获取用户信息
     */
    public User getLoginUser(HttpServletRequest request) {
        // 1. 获取请求头中的 Authorization 字段，检查是否携带 Bearer token
        final String header = request.getHeader(AUTHORIZATION);

        if (header == null || !header.startsWith(BEARER)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "令牌缺失或无效");
        }

        // 2. 解析 JWT Token，获取用户邮箱
        final String jwt = header.substring(7); // 去除 "Bearer " 前缀
        final String userEmail = jwtService.extractUsername(jwt);

        if (userEmail == null || userEmail.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "JWT 令牌无效：缺少用户名。");
        }

        // 3. 根据用户邮箱查询用户信息
        User currentUser = userMapper.selectById(userEmail);

        // 4. 检查用户是否存在以及是否有效
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未找到用户或用户无效");
        }

        // 返回当前用户
        return currentUser;
    }

    /**
     * 判断用户是否为管理员
     *
     * @param request HTTP 请求对象
     * @return true 如果是管理员，false 否则
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = this.getLoginUser(request);

        // 判断当前用户是否为管理员
        return isAdmin(loginUser);
    }

    /**
     * 判断用户是否为管理员
     *
     * @param user 用户对象
     * @return true 如果是管理员，false 否则
     */
    @Override
    public boolean isAdmin(User user) {
        // 用户为管理员的判断，确保用户非空且角色为管理员
        return user != null && Role.ADMIN.equals(user.getUserRole());
    }


    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }



    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 9:44
     * @return: com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.kimo.ucenter.model.po.User>
     * @Param: [com.kimo.ucenter.model.dto.UserQueryRequest]
     * @Description: 分页列出所有用户信息（只允许管理员）
     */
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest,HttpServletRequest request) {
        // 检查请求参数是否为空
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"2000002");
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        // 初始化QueryWrapper
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 使用Optional避免空指针异常
        Optional.ofNullable(userRole)
                .filter(StringUtils::isNotBlank)
                .ifPresent(role -> queryWrapper.eq("user_role", role));

        Optional.ofNullable(userName)
                .filter(StringUtils::isNotBlank)
                .ifPresent(name -> queryWrapper.like("user_name", name));

        // 排序字段和顺序的处理
        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            queryWrapper.orderBy(SqlUtils.validSortField(sortField), isAsc, sortField);
        }

        return queryWrapper;
    }




    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 10:10
     * @return: long
     * @Param: [com.kimo.ucenter.model.dto.UserAddRequest, org.springframework.web.multipart.MultipartFile]
     * @Description: 注册用户，从一开始的同步锁优化到现在的使用Redission分布式锁进行注册
     */
    public long registration(UserAddRequest request, MultipartFile file) throws IOException {
        // 校验参数
        validateRegistration(request);

        String userEmail = request.getEmail();
        byte[] avatarBytes = processAvatarFile(file);

        // 校验邮箱格式
        if (StringUtils.isBlank(userEmail) || !this.isEmail(userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式无效");
        }

        // 查询邮箱是否已注册
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", userEmail);
        Long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已经注册");
        }

        // 使用 Redis 锁来防止并发注册
        RLock lock = redissonClient.getLock(REGISTER_USER_LOCK + userEmail);
        int maxRetries = 3;  // 最大重试次数
        int retryCount = 0;
        boolean isLocked = false;

        while (retryCount < maxRetries) {
            try {
                isLocked = lock.tryLock(10, (long) Math.pow(2, retryCount), TimeUnit.SECONDS); // 指数退避策略
                if (isLocked) {
                    // 执行注册操作
                    User newUser = new User();
                    newUser.setEmail(userEmail);
                    newUser.setUserAvatar(avatarBytes);
                    newUser.setUserAccount(request.getUserAccount());
                    newUser.setUserPassword(passwordEncoder.encode(request.getPassword()));
                    newUser.setUserName(request.getUserName());
                    newUser.setGrade(request.getGrade());
                    newUser.setQualification(request.getQualification());
                    newUser.setIsEnable(1);

                    int insertResult = userMapper.insert(newUser);
                    if (insertResult <= 0) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
                    }
                    return newUser.getId(); // 返回新用户的 ID
                } else {
                    retryCount++;
                    // 可选：记录重试日志
                    log.warn("获取锁失败，正在重试，第 {} 次", retryCount);
                    Thread.sleep(1000L * (long) Math.pow(2, retryCount));  // 延迟一段时间再试
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统异常，请稍后重试");
            } catch (Exception e) {
                log.error("注册失败，锁操作异常", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误，请稍后重试");
            }
        }

        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后再试");
    }


    private byte[] processAvatarFile(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            return file.getBytes();
        }
        return null;  // 如果没有文件，则返回 null
    }


    private void validateRegistration(UserAddRequest request) {
        if (StringUtils.isAnyBlank(request.getUserAccount(), request.getPassword(), request.getConfirmPassword(), request.getEmail())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (request.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (request.getPassword().length() < 8 || request.getConfirmPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }
    }


    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 10:11
     * @return: java.lang.Long
     * @Param: [com.kimo.ucenter.model.dto.UserPublishEventRequest]
     * @Description: 是否有这个用户有就发布验证码
     */
    public Long isPublishEvent(UserPublishEventRequest userEmailAndId) {
        // 校验邮箱格式
        String email = userEmailAndId.getEmail();
        if (!isEmail(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式无效");
        }

        // 如果缓存中没有，再查询数据库（避免频繁查询数据库）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SqlConstants.EMAIL_NAME, email);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已存在");
        }

        long key = IdWorkerUtils.getInstance().nextId();

        // 发布验证码到邮箱
        checkCodeClient.getPublishEvent(key, redisVerificationUtil.generateVerification(), email);

        return key;
    }


    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 10:20
     * @return: java.lang.String
     * @Param: [java.lang.String, boolean, java.lang.String]
     * @Description: 是否验证成功如果isValid为True就添加验证表状态
     */
    public String saveUserForUpdateEnabled(String status, boolean isValid, String email) {
        if (isValid) {
            // Check if the record already exists
            QueryWrapper<UserEnble> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email", email);
            UserEnble existingUserEnble = userEnbleMapper.selectOne(queryWrapper);
            if (existingUserEnble != null) {
                return CommonConstant.IS_TRUE;
            }

            // Insert a new UserEnble record
            UserEnble userEnble = new UserEnble();
            userEnble.setIsEnable(1);
            userEnble.setEmail(email);
            userEnbleMapper.insert(userEnble);
            return CommonConstant.IS_TRUE;
        } else {
            return CommonConstant.IS_FALSE;
        }
    }


    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 10:24
     * @return: java.util.Optional<com.kimo.ucenter.model.vo.AuthentianResponse>
     * @Param: [com.kimo.ucenter.model.dto.UserAuthenticationRequest, jakarta.servlet.http.HttpServletRequest]
     * @Description: 登录验证，删除缓存采用的是缓存更新策略（先操作数据库再删缓存）
     */
    public Optional<AuthentianResponse> authentication(UserAuthenticationRequest request, HttpServletRequest httpServletRequest) {
        String header = servletUtils.getHeaderKey(httpServletRequest, SecurityConstants.AUTHORIZATION_HEADER);
        String userKeyForRedis = CAFFEINE_USER + header;

        // 查找用户并进行空值检查
        User user = userMapper.findByEmail(request.getEmail());
        if (user == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "没有这个用户");
        }

        // 如果用户未启用，则直接返回
        if (user.getIsEnable() != 1) {
            deleteRedisCache(userKeyForRedis);  // 删除缓存
            return Optional.empty();  // 返回认证失败
        }

        // 密码验证
        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), user.getUserPassword());
        if (isAuthenticated) {
            // 生成 JWT 和刷新令牌
            String jwtToken = jwtService.generateToken(user.getId().toString(), user.getEmail(), null);
            String refreshToken = jwtService.generateRefreshToken(user.getId().toString(), user.getEmail(), null);

            // 注销用户之前的所有 token
            revokeAllUserToken(user);

            // 保存新的 token
            saveUserToken(user, jwtToken);

            return Optional.of(AuthentianResponse.builder()
                    .accessToken(jwtToken)
                    .refershToken(refreshToken)
                    .build());
        }

        // 密码不匹配
        return Optional.empty();
    }

    private void deleteRedisCache(String userIdForRedisKey) {
        redisTemplate.delete(userIdForRedisKey);
    }

    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 10:35
     * @return: java.lang.String
     * @Param: [jakarta.servlet.http.HttpServletRequest]
     * @Description: 用户登出功能先从redis获取
     */
    public String userLogOut(HttpServletRequest request) {
        // 从 Redis 获取用户信息
        UserDto userDto = getUserFromCacheOrDb(request);

        // 撤销用户的所有 token
        revokeAllUserToken(userDto);

        return "登出成功";
    }

    @Override
    public UserDto getUserFromCacheOrDb(HttpServletRequest request) {
        // 从 Redis 缓存中获取用户信息
        Object userFromCache = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        // 如果缓存中没有找到，查询数据库
        if (userFromCache == null) {
            String userEmail = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
            if (userEmail == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "无法提取用户信息");
            }

            // 查询数据库中的用户
            User userFromDb = userMapper.selectById(userEmail);
            if (userFromDb == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "没有这个用户");
            }

            // 将查询到的数据库用户信息转到 DTO
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(userFromDb, userDto);
            return userDto;
        }

        // 如果缓存中找到了用户信息，直接转换为 UserDto
        return (UserDto) userFromCache;
    }




    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 10:37
     * @return: java.lang.Boolean
     * @Param: [jakarta.servlet.http.HttpServletRequest, java.lang.String]
     * @Description: 签到功能从缓存获取用户数据其实这个方法添加不添加还是会受到更新积分到数据库的性能影响
     */
    public Boolean signUser(HttpServletRequest request, String date) {
        // 从 Redis 获取用户信息
        UserDto userDto = getUserFromCacheOrDb(request);

        String cacheKey = getCacheKey(userDto.getId().toString(), date);

        // 获取当天日期
        DateTime dateTime = DateUtil.parse(date, "yyyy-MM-dd");
        int day = dateTime.dayOfMonth();

        // 设置签到标记
        Boolean result = redisTemplate.opsForValue().setBit(cacheKey, day - 1, true);
        String s = redisTemplate.opsForValue().get("gateway:user:1854934298709209090");
        log.info(s+"==================================");
        // 如果设置签到标记失败
        if (Boolean.FALSE.equals(result)) {
            // 设置过期时间，确保缓存不永久存在
            redisTemplate.expire(cacheKey, 30, TimeUnit.DAYS);

            // 增加用户积分
            updateUserPoints(userDto);
        }

        return result;
    }

    /**
     * 更新用户积分
     * @param userDto 用户信息
     */
    private void updateUserPoints(UserDto userDto) {
        QueryWrapper<Point> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userDto.getId());
        Point point = pointMapper.selectOne(queryWrapper);

        // 如果没有找到用户积分数据，可能需要创建新的积分记录
        if (point == null) {
            point = new Point();
            point.setUserId(userDto.getId());
            point.setPoint(0L);  // 初始积分
            pointMapper.insert(point);
        }

        // 判断是否是 VIP 用户
        if ("605001".equals(userDto.getMember())) {
            point.setPoint(point.getPoint() + UserConstant.USER_VIP_POINT_SIGN);
        } else {
            point.setPoint(point.getPoint() + UserConstant.USER_POINT_SIGN);
        }

        // 使用乐观锁更新积分，避免并发问题
        pointMapper.updateById(point);
    }



    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 10:42
     * @return: java.lang.Long
     * @Param: [jakarta.servlet.http.HttpServletRequest]
     * @Description: 获取用户积分
     */
    public Long getPoints(HttpServletRequest request) {
        // 获取当前登录用户
        User user = this.getLoginUser(request);

        // 使用 QueryWrapper 通过 user_id 查询积分数据，避免使用不必要的条件
        QueryWrapper<Point> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        Point point = pointMapper.selectOne(queryWrapper);

        // 如果没有找到对应的积分记录，抛出异常
        ThrowUtils.throwIf(point == null, ErrorCode.NOT_FOUND_ERROR, "未找到用户 ID 的用户点数： " + user.getId());

        return point.getPoint();
    }


    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 10:45
     * @return: com.kimo.ucenter.model.dto.UserDto
     * @Param: [java.lang.String]
     * @Description:
     */
    public UserDto GobalGetLoginUser(String request) {
        // 先判断是否已登录

        User currentUser = userMapper.selectById(request);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(currentUser,userDto);
        return userDto;
    }

    @Override
    public PageResponse<UserListDto> listUserForRolesByPage(PageRequest pageRequest, HttpServletRequest request) {
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);


        long total = userMapper.getTotalCountForRolesNode();

        servletUtils.ensuperAdminOrAdmin(code,"3000002");
        int pageSize = pageRequest.getPageSize();
        int current = pageRequest.getCurrent();
        int offset = (current - 1) * pageSize;
        List<UserListDto> userForRolesNodeById = userMapper.getUserForRolesNodeById(pageSize, offset);

        // 封装并返回分页结果
        PageResponse<UserListDto> response = new PageResponse<>();
        response.setTotal(total);
        response.setList(userForRolesNodeById);

        return response;

    }

    @Override
    public Long createUser(UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        String encode = passwordEncoder.encode(userAddRequest.getPassword());
        user.setUserPassword(encode);
        user.setIsEnable(1);
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"3000001");
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return 1L;
    }

    @Override
    public Boolean removeUser(DeleteRequest deleteRequest, HttpServletRequest request) {
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"3000004");
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return this.removeById(deleteRequest.getId());
    }

    @Override
    public Boolean updateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"3000003");
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user1 = userMapper.selectById(userUpdateRequest.getId());
        ThrowUtils.throwIf(user1 == null,ErrorCode.USER_IS_NOT);
        BeanUtils.copyProperties(userUpdateRequest, user1);
        boolean result = this.updateById(user1);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public User getUserById(long id, HttpServletRequest request) {

        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserDto headerRedisForUser = servletUtils.getHeaderRedisForUser(request, SecurityConstants.AUTHORIZATION_HEADER);

        String code = servletUtils.getRoleForPermission(headerRedisForUser);

        servletUtils.ensuperAdminOrAdmin(code,"3000002");
        User user = this.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return user;
    }

    @Override
    public BaseResponse<Boolean> deleteUserHeaderForRedis(HttpServletRequest request) {
        String beaner = servletUtils.getHeaderKey(request, SecurityConstants.AUTHORIZATION_HEADER);
        String userKeyForRedis = CAFFEINE_USER + beaner;
        Boolean delete = redisTemplate.delete(userKeyForRedis);
        return ResultUtils.success(delete);
    }


    public boolean isSignIn(String userNo, String date) {
        // 获取缓存key
        String cacheKey = getCacheKey(userNo, date);
        // 获取日期
        DateTime dateTime = DateUtil.parse(date, "yyyy-MM-dd");
        int day = dateTime.dayOfMonth();
        return redisTemplate.opsForValue().getBit(cacheKey, day - 1);
    }


    /**
     * 获取缓存key
     */
    private static String getCacheKey(String userNo, String date) {
        DateTime dateTime = DateUtil.parse(date, "yyyy-MM");
        return String.format("USER_SIGN_IN:%s:%s", userNo, dateTime.year() + "" + dateTime.monthBaseOne());
    }

    public  boolean isEmail(String email) {
        if (email == null || email.isEmpty() || email.length() > 256) {
            return false;
        }
        Pattern pattern = Pattern.compile("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
        return pattern.matcher(email).matches();
    }

    public void revokeAllUserToken(User user) {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user,userDto);
        tokenService.executeRevokeAllUserToken(userDto);
    }

    public void revokeAllUserToken(UserDto user) {

        tokenService.executeRevokeAllUserToken(user);
    }



    public void saveUserToken(User saveUser, String jwtToken) {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(saveUser,userDto);
        tokenService.executeSaveUserToken(userDto,jwtToken);

    }
}
