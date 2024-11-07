package com.kimo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimo.annotation.PermissionMethod;
import com.kimo.common.*;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.RedisConstant;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.ucenter.mapper.UserMapper;
import com.kimo.ucenter.model.vo.AuthentianResponse;
import com.kimo.ucenter.model.dto.*;
import com.kimo.ucenter.model.dto.UserDto;
import com.kimo.ucenter.service.PointService;
import com.kimo.ucenter.service.UserService;
import com.kimo.ucenter.model.po.User;

import java.io.IOException;
import java.util.Optional;


import com.kimo.utils.RedisVerificationUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户接口
 *
 * @author Mr.kimo
 *
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;


    @Autowired
    private RedisVerificationUtil redisVerificationUtil;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PointService pointService;


    @PostMapping("/insert")
    public int insertUser(User user){

        return userService.insertUser(user);
    }


    @PostMapping("/findemail")
    public UserLocalDto findByEmail(String username){
        return  userService.findByEmail(username);
    }
    /**
     * 注册
     * @param multipartFile
     * @param confirmPassword
     * @param userPassword
     * @param userAccount
     * @param email
     * @return 用户id
     *
     */
    @PostMapping("/register")
    @Transactional
    public BaseResponse<Long> register(@RequestParam("file") MultipartFile multipartFile, @RequestParam("confirmPassword") String confirmPassword,
                                       @RequestParam("userPassword") String userPassword, @RequestParam("userAccount") String userAccount, @RequestParam("email") String email) throws IOException {
        UserAddRequest userAddRequest = new UserAddRequest();
        userAddRequest.setUserAccount(userAccount);
        userAddRequest.setUserName(userAccount);
        userAddRequest.setEmail(email);
        userAddRequest.setPassword(userPassword);
        userAddRequest.setConfirmPassword(confirmPassword);
        long userId = userService.registration(userAddRequest,multipartFile);
        ThrowUtils.throwIf(userId == 0,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(userId);
    }


    /**
     * 发送验证码
     * @param userPublishEventRequest
     * @return trye或false
     */
    @PostMapping("/publish")
    public BaseResponse<Boolean> getPublishEvent(@RequestBody UserPublishEventRequest userPublishEventRequest, HttpServletRequest request){
        ThrowUtils.throwIf(userPublishEventRequest.getId() <= 0,ErrorCode.OPERATION_ERROR);
        ThrowUtils.throwIf("".equals(userPublishEventRequest.getEmail()),ErrorCode.OPERATION_ERROR);
        ThrowUtils.throwIf(userPublishEventRequest.getEmail() == null,ErrorCode.OPERATION_ERROR);
        if(StringUtils.isBlank(userPublishEventRequest.getEmail())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请输入电子邮箱");
        }
        return ResultUtils.success(userService.isPublishEvent(userPublishEventRequest));
    }

    /**
     * 验证电子邮箱
     * @param userEmail
     * @return true或false
     */
    @PostMapping("/verificationEmail")
    public BaseResponse<String> checkVerificationEmail(@RequestBody UserEmailVerificationRequest userEmail){
        boolean isValid = redisVerificationUtil.verifyCode(userEmail.userId(),userEmail.code(),userEmail.email(), RedisConstant.KEY_UTIL);
        String isEnable = userService.saveUserForUpdateEnabled(userEmail.userId(),isValid);
        return isEnable.equals(CommonConstant.IS_TRUE) ? ResultUtils.success(isEnable) : ResultUtils.error(ErrorCode.EMAIL_ERROR,"验证失败");
    }

//    @RequestMapping("/r/r1")
//    public String r1(){
//        return "访问r1资源";
//    }

    /**
     * 登录
     * @param request
     * @return AuthentianResponse
     */
    @PostMapping("/authentication")
    public BaseResponse<AuthentianResponse> getAuthentication(@RequestBody UserAuthenticationRequest request,HttpServletRequest httpServletRequest){
        Optional<AuthentianResponse> optionalResponse = userService.authentication(request,httpServletRequest);
        return optionalResponse.map(ResultUtils::success).orElseGet(() -> ResultUtils.error(ErrorCode.OPERATION_ERROR, "验证失败"));
    }








    //todo 暂未实现
    @GetMapping("/signCount")
    public BaseResponse<Integer> getSignUserCount(HttpServletRequest request){
        Integer isSign = userService.signUserCount(request);
        return ResultUtils.success(isSign);
    }



    //todo 暂未实现
    @PostMapping("/vip")
    public BaseResponse<Boolean> settingUserIsVIP(HttpServletRequest request){
        Boolean isVIP = userService.settingUserIsVIP(request);

        return ResultUtils.success(isVIP);
    }


    /**
     * 签到
     * @param request
     * @param date
     * @return trye或false
     */
    @PostMapping("/sign")
    public BaseResponse<Boolean> userSign(HttpServletRequest request, String date){
        Boolean isSign = userService.signUser(request,date);
        return ResultUtils.success(isSign);
    }

    /**
     * 获取积分
     * @param request
     * @return
     */
    @GetMapping("/get/points")
    public BaseResponse<Long> getPoints(HttpServletRequest request){
        Long points = userService.getPoints(request);
        return ResultUtils.success(points);
    }

    /**
     * 登出
     * @param request
     * @return
     */
    @GetMapping("/logout")
    public BaseResponse<String> logout(HttpServletRequest request){
        String isLogout = userService.userLogOut(request);
        return ResultUtils.success(isLogout);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<User> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(user);
    }

    /**
     *
     *
     * @param request
     * @return
     */
    @PostMapping("/gobalget/login")
    public UserDto GobalGetLoginUser(@RequestParam String request) {
        return userService.GobalGetLoginUser(request);
    }


    /**
     * 创建用户
     * 前端未实现
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @PermissionMethod(permission = "user_add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 12345678
//        String defaultPassword = "12345678";
//        String encryptPassword = DigestUtils.md5DigestAsHex((UserServiceImpl.SALT + defaultPassword).getBytes());
        String encode = passwordEncoder.encode(userAddRequest.getPassword());
        user.setUserPassword(encode);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     * 前端未实现
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @PermissionMethod(permission = "user_delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     * 前端未实现
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @PermissionMethod(permission = "user_update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user1 = userMapper.selectById(userUpdateRequest.getId());
        ThrowUtils.throwIf(user1 == null,ErrorCode.USER_IS_NOT);
        BeanUtils.copyProperties(userUpdateRequest, user1);
        boolean result = userService.updateById(user1);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     * 前端未实现
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @PermissionMethod(permission = "user_read")
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 分页获取用户列表（仅管理员）
     * 前端未实现
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @PermissionMethod(permission = "user_read")
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }




    /**
     * 是否为管理员
     */
    @PostMapping("/isAdmin")
    public Boolean isAdmin(HttpServletRequest request){
        return userService.isAdmin(request);
    }


    /**
     * 操作积分数
     */
    @PostMapping("/update/point")
    public Boolean updatePoint(@RequestParam  Long userId,@RequestParam Long point){
        return pointService.updatePoint(userId,point);
    }


}
