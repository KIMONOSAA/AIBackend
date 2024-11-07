package com.kimo.ucenter.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimo.common.PageRequest;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.exception.ThrowUtils;
import com.kimo.ucenter.mapper.PointMapper;
import com.kimo.ucenter.mapper.UserMemberMapper;
import com.kimo.ucenter.model.po.*;
import com.kimo.ucenter.service.PermissionsService;
import com.kimo.ucenter.service.RolesService;
import com.kimo.utils.*;
import com.kimo.common.ErrorCode;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.UserConstant;
import com.kimo.exception.BusinessException;

import com.kimo.ucenter.feignclient.CheckCodeClient;
import com.kimo.ucenter.mapper.UserMapper;
import com.kimo.ucenter.model.dto.*;
import com.kimo.ucenter.model.vo.AuthentianResponse;
import com.kimo.ucenter.model.vo.LoginUserVO;
import com.kimo.ucenter.model.vo.UserVO;

import com.kimo.ucenter.service.TokenService;
import com.kimo.ucenter.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.kimo.constant.CaffeineConstant.CAFFEINE_USER;

/**
 * 用户服务实现
 *
 * @author kimo

 * 

 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


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
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PointMapper pointMapper;

    @Autowired
    private UserMemberMapper userMemberMapper;

    @Autowired
    private RolesService rolesService;

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private ServletUtils  servletUtils;


    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "kimo";


//    @Override
//    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
//        return null;
//    }

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

        // 创建并保存用户角色
        Roles role = createAndSaveRole(user.getId(), "VIP");

        // 创建和保存角色的权限
        createAndSavePermission(role.getId(), "user_vip_all", "会员专享");

        return true;
    }

    // Utility methods
    private UserMember checkUserMemberExistence(String memberTypeId) {
        UserMember userMember = userMemberMapper.selectById(memberTypeId);
        ThrowUtils.throwIf(userMember == null, ErrorCode.SYSTEM_ERROR);
        return userMember;
    }

    private User checkUserExistence(String userId) {
        User user = userMapper.selectById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        return user;
    }

    private void updateUserMemberStatus(User user) {
        user.setMember("605001");
        int result = userMapper.updateById(user);
        ThrowUtils.throwIf(result <= 0, ErrorCode.SYSTEM_ERROR);
    }

    private void updateUserPoints(String userId, long points) {
        QueryWrapper<Point> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        Point point = pointMapper.selectOne(queryWrapper);
        point.setPoint(points);
        int result = pointMapper.updateById(point);
        ThrowUtils.throwIf(result <= 0, ErrorCode.SYSTEM_ERROR);
    }

    private Roles createAndSaveRole(Long userId, String roleName) {
        long roleId = IdWorkerUtils.getInstance().nextId();
        Roles role = new Roles();
        role.setId(roleId);
        role.setUserId(userId);
        role.setRolesName(roleName);
        boolean save = rolesService.save(role);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR);
        return role;
    }

    private void createAndSavePermission(long roleId, String code, String permissionName) {
        long permissionId = IdWorkerUtils.getInstance().nextId();
        Permissions permissions = new Permissions();
        permissions.setId(permissionId);
        permissions.setRoleId(roleId);
        permissions.setCode(code);
        permissions.setPermissionName(permissionName);
        boolean save = permissionsService.save(permissions);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR);
    }


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        final String header = request.getHeader("Authorization");

        final String jwt;
        final String userEmail;
        if(header == null || !header.startsWith("Bearer ")){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        jwt = header.substring(7);
        userEmail = jwtService.extractUsername(jwt);
        User currentUser = userMapper.selectById(userEmail);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        User loginUser = this.getLoginUser(request);
        return isAdmin(loginUser);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && Role.ADMIN.equals(user.getUserRole());
    }


    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
//        loginUserVO.setUserAvatar(user.getUserAvatar());
//        loginUserVO.setUserName(user.getUsername());
//        loginUserVO.setId(user.getId());
//        loginUserVO.setCreateTime(user.getCreateTime());
//        loginUserVO.setUpdateTime(user.getUpdateTime());
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
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
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq(id != null, "id", id);
//        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
//        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "user_role", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userName), "user_name", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }



    @Override
    public long registration(UserAddRequest request, MultipartFile file) throws IOException {

//        Blob avatarPhoyo = null;
//        try {
//            if(file != null && !file.isEmpty()){
//                byte[] bytes = file.getBytes();
//                avatarPhoyo = new SerialBlob(bytes);
//            }
//        } catch (Exception e) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件类型错误");
//        }
        String userName = request.getUserName();
        String userAccount = request.getUserAccount();
        String userPassword = request.getPassword();
        String userEmail = request.getEmail();
        String userCheckPassword = request.getConfirmPassword();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email",userEmail);
        User user = userMapper.selectOne(queryWrapper);
        if(user != null && user.getEmail().equals(userEmail)){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"你已经注册过");
        }
        if (StringUtils.isAnyBlank(userAccount, userPassword, userCheckPassword,userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || userCheckPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(userCheckPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userEmail.intern()){
            boolean isEmail = this.isEmail(userEmail);
            if(isEmail){
                User newUser = new User();
                newUser.setEmail(userEmail);
                assert file != null;
                newUser.setUserAvatar(file.getBytes());
                newUser.setUserName("user");
                newUser.setUserAccount(userAccount);
                newUser.setUserPassword(passwordEncoder.encode(userPassword));
                newUser.setUserName(userName);
                int insert = userMapper.insertUser(newUser);
                if (insert <= 0) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
                }
                return newUser.getId();
            }

        }
        return 0;
    }

    @Override
    public Boolean isPublishEvent(UserPublishEventRequest userEmailAndId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SqlConstants.CHART_ID, userEmailAndId.getId());
        queryWrapper.eq(SqlConstants.EMAIL_NAME,userEmailAndId.getEmail());
        User user = userMapper.selectOne(queryWrapper);
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user,userDto);
        if(user != null){
            checkCodeClient.getPublishEvent(userDto, redisVerificationUtil.generateVerification());
            return true;
        }
        return false;
    }

    @Override
    public String saveUserForUpdateEnabled(String s, boolean isValid) {
        if(isValid){
            long userId = Long.parseLong(s);
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(SqlConstants.CHART_ID,userId);
            User user = userMapper.selectOne(queryWrapper);
            user.setIsEnable(1);
            this.updateById(user);
            return CommonConstant.IS_TRUE;
        }else{
            return CommonConstant.IS_FALSE;
        }
    }

    @Override
    public Optional<AuthentianResponse> authentication(UserAuthenticationRequest request,HttpServletRequest httpServletRequest) {
        String header = servletUtils.getHeaderKey(httpServletRequest, SecurityConstants.AUTHORIZATION_HEADER);
        String userKeyForRedis = CAFFEINE_USER + header;
        User user = Optional.of(userMapper.findByEmail(request.getEmail())).orElseThrow(() -> new BusinessException(ErrorCode.OPERATION_ERROR,"没有这个用户"));
        if(user.getIsEnable() == 1){
            boolean isAuthenticated = passwordEncoder.matches(request.getPassword(),
                    user.getUserPassword());
            // 如果密码匹配成功
            if(isAuthenticated){
                // 通过 jwtTokenUtil 生成 JWT 令牌和刷新令牌
                String jwtToken = jwtService.generateToken(user.getId().toString(),user.getEmail(),null);
                String refreshToken = jwtService.generateRefreshToken(user.getId().toString(),user.getEmail(),null);
                revokeAllUserToken(user);
                saveUserToken(user, jwtToken);
                return Optional.of(AuthentianResponse.builder()
                        .accessToken(jwtToken)
                        .refershToken(refreshToken)
                        .build());
            }
            // 如果密码匹配失败
            return Optional.empty();

        }
        if(StringUtils.isNotBlank(header)){
            deleteRedisCache(userKeyForRedis);
        }
        return Optional.empty();
    }

    private void deleteRedisCache(String userIdForRedisKey) {
        redisTemplate.delete(userIdForRedisKey);
    }

    @Override
    public String userLogOut(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        String jwt;
        String userEmail;
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"没有token");
        }
        jwt = header.substring(7);
        userEmail = jwtService.extractUsername(jwt);
        if (userEmail != null) {
            User user = userMapper.selectById(userEmail);
//            User user = Optional.of(userMapper.findByEmail(userEmail)).orElseThrow(() -> new BusinessException(ErrorCode.OPERATION_ERROR,"没有这个用户"));
            revokeAllUserToken(user);
//            SecurityContextHolder.clearContext();
            return "登出成功";
        }
        return "登出失败";
    }

    //todo 实现签到功能 暂时只实现签到不实现月份签到统计等等
    @Override
    public Integer signUserCount(HttpServletRequest request) {
        return null;
    }

    @Override
    public Boolean settingUserIsVIP(HttpServletRequest request) {
        return null;
    }

    @Override
    public Boolean signUser(HttpServletRequest request,String date) {
        User user = this.getLoginUser(request);

        String cacheKey = getCacheKey(user.getId().toString(), date);

        QueryWrapper<Point> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",user.getId());
        Point point = pointMapper.selectOne(queryWrapper);
        DateTime dateTime = DateUtil.parse(date, "yyyy-MM-dd");
        int day = dateTime.dayOfMonth();
        Boolean result = redisTemplate.opsForValue().setBit(cacheKey, day - 1, true);
        if (Boolean.FALSE.equals(result)) {
            redisTemplate.expire(cacheKey, 30, TimeUnit.DAYS);
            if ("605001".equals(user.getMember())){
                point.setPoint(point.getPoint() + UserConstant.USER_VIP_POINT_SIGN);
            }else {
                point.setPoint(point.getPoint() + UserConstant.USER_POINT_SIGN);
            }
            pointMapper.updateById(point);
        }
        return result;
    }

    @Override
    public Long getPoints(HttpServletRequest request) {
        User user = this.getLoginUser(request);
        QueryWrapper<Point> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",user.getId());
        queryWrapper.eq("email",user.getEmail());
        Point point = pointMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(point == null,ErrorCode.NOT_FOUND_ERROR);

        return point.getPoint();
    }

    @Override
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
    public int insertUser(User user) {
        return userMapper.insertUser(user);
    }

    @Override
    public UserLocalDto findByEmail(String username) {
        UserLocalDto userLocalDto = new UserLocalDto();
        User byEmail = userMapper.findByEmail(username);
        BeanUtils.copyProperties(byEmail,userLocalDto);
        return userLocalDto;
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
//    private synchronized void increment(User user,long num){
//        QueryWrapper<Point> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("userId",user.getId());
//        Point point = pointMapper.selectOne(queryWrapper);
//        if(user.getIsPoint() == 0 && point == null) {
//            user.setIsPoint(1);
//            this.updateById(user);
//            Point newPoint = new Point();
//            newPoint.setEmail(user.getEmail());
//            newPoint.setUserId(user.getId());
//            newPoint.setPoint(num);
//            pointService.save(newPoint);
//        }
//    }


    public void saveUserToken(User saveUser, String jwtToken) {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(saveUser,userDto);
        tokenService.executeSaveUserToken(userDto,jwtToken);

    }
}
