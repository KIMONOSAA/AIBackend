package com.kimo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimo.common.ErrorCode;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.ContentConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.feignclient.UserClient;
import com.kimo.mapper.AIRoleMapper;
import com.kimo.model.dto.aiRole.AIRoleQueryRequest;
import com.kimo.model.dto.aiRole.AIRoleUpdateRequest;
import com.kimo.model.dto.user.UserDto;
import com.kimo.model.entity.AIRole;
import com.kimo.service.AIRoleService;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.kimo.model.dto.user.Role.USERVIP;

/**
 * @author Mr.kimo
 */
@Service
@Slf4j
public class AIRoleServiceImpl extends ServiceImpl<AIRoleMapper, AIRole>
    implements AIRoleService {

    @Autowired
    private AIRoleMapper aiRoleMapper;

    @Autowired
    UserClient userClient;

    @Autowired
    ServletUtils servletUtils;

    /**
     * 验证给定的 AIRole 对象的合法性，根据用户的角色和角色状态进行相应处理。
     *
     * @param aIRole 要验证的 AIRole 对象，包含角色信息和参数。
     * @param request HTTP 请求对象，用于获取用户身份信息。
     * @return boolean 返回是否验证成功。
     *         - 如果角色是私有且满足条件，返回 true，审核状态设置为成功（0）。
     *         - 如果角色是公有，返回 true，审核状态设置为待审核（1）。
     *         - 如果不满足条件，返回 false。
     *
     * @throws BusinessException 如果用户未登录或参数错误。
     */
    @Override
    public boolean validAIRole(AIRole aIRole, HttpServletRequest request) {
        //获取用户对象
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        //特判
        ThrowUtils.throwIf(userDto == null,ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(aIRole == null,ErrorCode.PARAMS_ERROR);

        aIRole.setUserId(userDto.getId());
        //是否私有，私有就直接插入数据库
        if(aIRole.getStatus() == 0){
//            String role = userDto.getUserRole().name();
//            if(!role.equals(USERVIP) && aIRole.getAIMax_Tokens() > 1024){
//                throw new BusinessException(ErrorCode.PARAMS_ERROR,"你的参数格式错误");
//            }
            //私有就放，直接审核成功
            aIRole.setAIRoleReview(0);
            return true;
        }else if(aIRole.getStatus() == 1){
            //公有就直接待审核，通过管理员同意
            aIRole.setAIRoleReview(1);
            return true;
        }
        return false;
    }

    /**
     * 删除指定 ID 的 AIRole 对象。
     *
     * 该方法首先验证用户是否已登录，然后检查要删除的角色是否存在，并确定用户是否有权限进行删除操作。
     *
     * @param id 要删除的 AIRole 对象的 ID。
     * @param request HTTP 请求对象，用于获取用户身份信息。
     * @return boolean 如果用户有权限删除，返回 true。
     *         - 如果角色不存在，抛出 NOT_FOUND_ERROR。
     *         - 如果用户没有删除权限，抛出 NOT_LOGIN_ERROR。
     *
     * @throws BusinessException 如果用户未登录或没有权限进行删除操作。
     */
    @Override
    public boolean deleteAIRole(long id, HttpServletRequest request) {

        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        if(userDto == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"请先登录");
        }
        log.info("id是：" + id);
        QueryWrapper<AIRole> aiRoleQueryWrapper = new QueryWrapper<>();
        aiRoleQueryWrapper.eq(SqlConstants.CHART_ID,id);
        AIRole aiRole = this.getOne(aiRoleQueryWrapper);
        if (aiRole == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if ((!aiRole.getUserId().equals(userDto.getId()) && (aiRole.getStatus() == 1)) || !USERVIP.equals(userDto.getUserRole()) ) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        } else{
            return true;
        }
    }

    /**
     * 获取查询包装类
     *
     * @param aiRoleQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<AIRole> getQueryWrapper(AIRoleQueryRequest aiRoleQueryRequest,HttpServletRequest request) {
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        ThrowUtils.throwIf(userDto == null,ErrorCode.NOT_LOGIN_ERROR);
        QueryWrapper<AIRole> queryWrapper = new QueryWrapper<>();
        if (aiRoleQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = aiRoleQueryRequest.getSearchText();
        String sortField = aiRoleQueryRequest.getSortField();
        String sortOrder = aiRoleQueryRequest.getSortOrder();
        Long id = aiRoleQueryRequest.getId();
        String aiRole = aiRoleQueryRequest.getAIRole();
        String description = aiRoleQueryRequest.getAIDescription();
        Long userId = userDto.getId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like(ContentConstant.AIROLE_NAME, searchText).or().like(ContentConstant.AIDESCRIPTION_NAME, searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(aiRole), ContentConstant.AIROLE_NAME, aiRole);
        queryWrapper.like(StringUtils.isNotBlank(description), ContentConstant.AIDESCRIPTION_NAME, description);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), SqlConstants.CHART_ID, id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), SqlConstants.USERID_NAME, userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     * 验证并更新指定 ID 的 AIRole 对象。
     *
     * 该方法首先检查用户是否已登录，然后验证用户是否有权限更新指定的 AIRole 对象。
     * 如果验证通过，方法将根据请求中提供的新值更新角色的属性，并设置审核状态。
     *
     * @param aiRoleUpdateRequest 包含更新信息的请求对象，包括角色名、描述、状态和最大令牌数等。
     * @param request HTTP 请求对象，用于获取用户身份信息。
     * @return boolean 如果更新成功，返回 true；否则返回 false。
     *         - 如果用户未登录，抛出 NOT_LOGIN_ERROR。
     *         - 如果角色不存在，抛出 NOT_FOUND_ERROR。
     *         - 如果用户没有权限更新角色，抛出 NOT_FOUND_ERROR。
     *
     * @throws BusinessException 如果用户未登录或没有权限更新角色。
     */
    @Override
    @Transactional
    public boolean validUpdateAIRole(AIRoleUpdateRequest aiRoleUpdateRequest, HttpServletRequest request) {
        String aIRole = aiRoleUpdateRequest.getAIRole();
        String aIDescription = aiRoleUpdateRequest.getAIDescription();
        Integer status = aiRoleUpdateRequest.getStatus();
        Integer aIMax_Tokens = aiRoleUpdateRequest.getAIMax_Tokens();
        Long oldId = aiRoleUpdateRequest.getId();
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        ThrowUtils.throwIf(userDto == null,ErrorCode.NOT_LOGIN_ERROR);
        AIRole oldRole = aiRoleMapper.findAIRoleById(aiRoleUpdateRequest.getId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));
        ThrowUtils.throwIf(oldRole == null,ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!Objects.equals(oldRole.getUserId(), userDto.getId()),ErrorCode.NOT_FOUND_ERROR);

        if(StringUtils.isNotBlank(aIRole)){
            oldRole.setAIRole(aIRole);
        }
        if(StringUtils.isNotBlank(aIDescription)){
            oldRole.setAIDescription(aIDescription);
        }
        if(status == 0 || status == 1){
            oldRole.setStatus(status);
        }
        if(aIMax_Tokens > 0){
            oldRole.setAIMax_Tokens(aIMax_Tokens);
        }
        oldRole.setAIRoleReview(1);
        int id = aiRoleMapper.updateById(oldRole);
        log.info("id" + id + oldId);
        return id > 0;
    }
}




