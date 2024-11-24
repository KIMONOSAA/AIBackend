package com.kimo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimo.annotation.PermissionMethod;
import com.kimo.api.dto.UserDto;
import com.kimo.common.*;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.RedisConstant;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.ucenter.mapper.UserMapper;
import com.kimo.ucenter.model.dto.*;
import com.kimo.ucenter.model.vo.AuthentianResponse;


import com.kimo.ucenter.service.PointService;
import com.kimo.ucenter.service.UserService;
import com.kimo.ucenter.model.po.User;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import com.kimo.utils.RedisVerificationUtil;
import com.kimo.utils.ServletUtils;
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


@RestController
@RequestMapping("/auth")
@Slf4j
/**
 * @Author kimo
 * @Description  用户模块
 * @Date
 * @Param
 * @param null
 * @return
 * @return null
 **/
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

    @Autowired
    private ServletUtils servletUtils;


//    @PostMapping("/findemail")
//    public UserLocalDto findByEmail(String username){
//        return  userService.findByEmail(username);
//    }

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
                                       @RequestParam("userPassword")  String userPassword, @RequestParam("qualification") String qualification, @RequestParam("userAccount") String userAccount, @RequestParam("grade") String grade, @RequestParam("email") String email) throws IOException {
        UserAddRequest userAddRequest = new UserAddRequest();
        userAddRequest.setUserAccount(userAccount);
        userAddRequest.setUserName(userAccount);
        userAddRequest.setEmail(email);
        userAddRequest.setPassword(userPassword);
        userAddRequest.setConfirmPassword(confirmPassword);
        userAddRequest.setQualification(qualification);
        userAddRequest.setGrade(grade);
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
    public BaseResponse<Long> getPublishEvent(@RequestBody UserPublishEventRequest userPublishEventRequest, HttpServletRequest request){
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
        String isEnable = userService.saveUserForUpdateEnabled(userEmail.userId(),isValid,userEmail.email());
        return isEnable.equals(CommonConstant.IS_TRUE) ? ResultUtils.success(isEnable) : ResultUtils.error(ErrorCode.EMAIL_ERROR,"验证失败");
    }


    /**
     * 登录
     * @param request
     * @return AuthentianResponse
     */
    @PostMapping("/authentication")
    public BaseResponse<AuthentianResponse> getAuthentication(@RequestBody UserAuthenticationRequest request, HttpServletRequest httpServletRequest){
        Optional<AuthentianResponse> optionalResponse = userService.authentication(request,httpServletRequest);
        return optionalResponse.map(ResultUtils::success).orElseGet(() -> ResultUtils.error(ErrorCode.OPERATION_ERROR, "验证失败"));
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
    public BaseResponse<UserLocalDto> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        UserLocalDto userLocalDto = new UserLocalDto();
        BeanUtils.copyProperties(user,userLocalDto);
        return ResultUtils.success(userLocalDto);
    }

    /**
     * @Author kimo
     * @Description 根据请求头获取JWT用户信息 //todo (改为AOP全局加ConCourrentHashMap（ThreadLocal）)
     * @Date
     * @Param
     * @param request
     * @return com.kimo.ucenter.model.dto.UserDto
     **/
    @PostMapping("/gobalget/login")
    public UserDto GobalGetLoginUser(@RequestParam String request) {
        return userService.GobalGetLoginUser(request);
    }



    @PostMapping("/add")
    /**
     * @Author kimo
     * @Description  创建用户只允许管理员
     * @Date
     * @Param
     * @param userAddRequest
     * @param request
     * @return
     * @return com.kimo.common.BaseResponse<java.lang.Long>
     **/
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        Long userId = userService.createUser(userAddRequest,request);
        return ResultUtils.success(userId);
    }



    @PostMapping("/delete")
    /**
     * @Author kimo
     * @Description  删除用户只允许管理员
     * @Date
     * @Param
     * @param deleteRequest
     * @param request
     * @return
     * @return com.kimo.common.BaseResponse<java.lang.Boolean>
     **/
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        Boolean IsS = userService.removeUser(deleteRequest,request);
        return ResultUtils.success(IsS);
    }


    @PostMapping("/update")
    /**
     * @Author kimo
     * @Description  修改用户只允许管理员
     * @Date
     * @Param
     * @param userUpdateRequest
     * @param request
     * @return
     * @return com.kimo.common.BaseResponse<java.lang.Boolean>
     **/
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        Boolean IsS = userService.updateUser(userUpdateRequest,request);
        return ResultUtils.success(true);
    }


    @GetMapping("/get")
    /**
     * @Author kimo
     * @Description  管理员、获取用户
     * @Date
     * @Param
     * @param id
     * @param request
     * @return
     * @return com.kimo.common.BaseResponse<com.kimo.ucenter.model.po.User>
     **/
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        User user = userService.getUserById(id,request);
        return ResultUtils.success(user);
    }


    @PostMapping("/list/page")
    /**
     * @Author kimo
     * @Description  分页获取用户只允许管理员
     * @Date
     * @Param
     * @param userQueryRequest
     * @param request
     * @return
     * @return com.kimo.common.BaseResponse<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.kimo.ucenter.model.po.User>>
     **/

    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest,request));
        return ResultUtils.success(userPage);
    }





    @PostMapping("/isAdmin")
    /**
     * @Author kimo
     * @Description  判断是否管理员
     * @Date
     * @Param
     * @param request
     * @return
     * @return java.lang.Boolean
     **/
    public Boolean isAdmin(HttpServletRequest request){
        return userService.isAdmin(request);
    }


    /**
     * 操作积分数
     */
    @PostMapping("/update/point")
    /**
     * @Author kimo
     * @Description  更新积分数值
     * @Date
     * @Param
     * @param userId
     * @param point
     * @return
     * @return java.lang.Boolean
     **/
    public Boolean updatePoint(@RequestParam Long userId,@RequestParam Long point){
        return pointService.updatePoint(userId,point);
    }

    /**
     * @Author: Mr.kimo
     * @Date: 19:47
     * @return:
     * @Param:
     * @Description: 分页获取用户以及角色
     */
    @PostMapping("/list/user/roles/page")
    public BaseResponse<List<UserListDto>> listUserForRolesByPage(@RequestBody PageRequest pageRequest,
                                                           HttpServletRequest request) {
        List<UserListDto> userPage = userService.listUserForRolesByPage(pageRequest,request);
        return ResultUtils.success(userPage);
    }



    @PostMapping("/add/point")
    /**
     * @Author kimo
     * @Description  创建积分
     * @Date
     * @Param
     * @param userId
     * @return
     * @return java.lang.Boolean
     **/
    public Boolean addPoint(@RequestParam  Long userId){
        return pointService.addPoint(userId);
    }


}
