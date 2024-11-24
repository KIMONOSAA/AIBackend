package com.kimo.ucenter.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.api.dto.UserDto;
import com.kimo.common.DeleteRequest;
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


    public UserDto getUserFromCacheOrDb(HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

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

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest,HttpServletRequest request);

    long registration(UserAddRequest request, MultipartFile file) throws IOException;

    Long isPublishEvent(UserPublishEventRequest userEmailAndId);

    String saveUserForUpdateEnabled(String s, boolean isValid,String email);

    Optional<AuthentianResponse> authentication(UserAuthenticationRequest request, HttpServletRequest httpServletRequest);

    String userLogOut(HttpServletRequest request);


    Boolean signUser(HttpServletRequest request,String date);

    Long getPoints(HttpServletRequest request);

    UserDto GobalGetLoginUser(String request);

    List<UserListDto> listUserForRolesByPage(PageRequest pageRequest, HttpServletRequest request);

    Long createUser(UserAddRequest userAddRequest, HttpServletRequest request);

    Boolean removeUser(DeleteRequest deleteRequest, HttpServletRequest request);

    Boolean updateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request);

    User getUserById(long id, HttpServletRequest request);
}
