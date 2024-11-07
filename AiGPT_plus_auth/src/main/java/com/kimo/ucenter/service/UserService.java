package com.kimo.ucenter.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.common.PageRequest;
import com.kimo.ucenter.model.dto.*;
import com.kimo.ucenter.model.po.User;
import com.kimo.ucenter.model.po.UserMember;
import com.kimo.ucenter.model.vo.AuthentianResponse;
import com.kimo.ucenter.model.vo.LoginUserVO;
import com.kimo.ucenter.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务
 *
 * @author kimo

 * 

 */
public interface UserService extends IService<User> {

    /**
     * 保存用户会员状态
     * @param userId
     * @return
     */
    public boolean saveUserStatusSuccess(String memberId,String memberType,String userId);

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
//    long userRegister(String userAccount, String userPassword, String checkPassword);

//    /**
//     * 用户登录
//     *
//     * @param userAccount  用户账户
//     * @param userPassword 用户密码
//     * @param request
//     * @return 脱敏后的用户信息
//     */
//    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

//    /**
//     * 用户登录（微信开放平台）
//     *
//     * @param wxOAuth2UserInfo 从微信获取的用户信息
//     * @param request
//     * @return 脱敏后的用户信息
//     */
//    LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

//    /**
//     * 用户注销
//     *
//     * @param request
//     * @return
//     */
//    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

//    QueryWrapper<UserMember> getQueryWrapper(PageRequest pageRequest);

    long registration(UserAddRequest request, MultipartFile file) throws IOException;

    Boolean isPublishEvent(UserPublishEventRequest userEmailAndId);

    String saveUserForUpdateEnabled(String s, boolean isValid);

    Optional<AuthentianResponse> authentication(UserAuthenticationRequest request,HttpServletRequest httpServletRequest);

    String userLogOut(HttpServletRequest request);

    Integer signUserCount(HttpServletRequest request);

    Boolean settingUserIsVIP(HttpServletRequest request);

    Boolean signUser(HttpServletRequest request,String date);

    Long getPoints(HttpServletRequest request);

    UserDto GobalGetLoginUser(String request);

    int insertUser(User user);

    UserLocalDto findByEmail(String username);
}
