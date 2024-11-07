package com.kimo.controller;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimo.common.*;
import com.kimo.exception.BusinessException;
import com.kimo.mapper.AIRoleMapper;
import com.kimo.model.dto.aiRole.AIRoleAddRequest;
import com.kimo.model.dto.aiRole.AIRoleQueryRequest;
import com.kimo.model.dto.aiRole.AIRoleUpdateRequest;
import com.kimo.model.entity.AIRole;
import com.kimo.service.AIRoleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * AI设定角色(不测试前端还未实现)
 *  前端没实现
 * @author  kimo
 */
@RestController
@RequestMapping("/content")
@Slf4j
public class AIRoleController {


    @Autowired
    private AIRoleService aiRoleService;
    
    @Autowired
    private AIRoleMapper aiRoleMapper;


    // region 增删改查

    /**
     * 创建
     *
     * @param aiRoleAddRequest
     * @param
     * @return
     */
    @PostMapping("/add/ai/role")
    public BaseResponse<Long> addAiRole(@RequestBody AIRoleAddRequest aiRoleAddRequest, HttpServletRequest request) {

        if (aiRoleAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AIRole aiRole = new AIRole();
        BeanUtils.copyProperties(aiRoleAddRequest, aiRole);
        // 校验
        boolean isAddAiRole = aiRoleService.validAIRole(aiRole, request);
        if(isAddAiRole){
            int insert = aiRoleMapper.insert(aiRole);
            if(insert <= 0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        long newAIRoleId = aiRole.getId();
        return ResultUtils.success(newAIRoleId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @DeleteMapping("/delete/ai/role")
    public BaseResponse<Boolean> deleteAIRole(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        boolean isRole = aiRoleService.deleteAIRole(id,request);
        if(isRole){
            aiRoleMapper.deleteById(id);
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新
     *
     * @param aiRoleUpdateRequest
     * @param
     * @return
     */
    @PutMapping("/update/ai/role")
    public BaseResponse<Boolean> updateAIRole(@RequestBody AIRoleUpdateRequest aiRoleUpdateRequest, HttpServletRequest request) {
        if (aiRoleUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 参数校验
        return ResultUtils.success(aiRoleService.validUpdateAIRole(aiRoleUpdateRequest, request));
    }


    /**
     * AI问答标题列表
     *
     * @param aiRoleQueryRequest
     * @return
     */
    @GetMapping("/list/aiRole/ai/role")
    public BaseResponse<Page<AIRole>> listAiRoleByPage(@RequestBody AIRoleQueryRequest aiRoleQueryRequest,HttpServletRequest request) {
        long current = aiRoleQueryRequest.getCurrent();
        long size = aiRoleQueryRequest.getPageSize();
        Page<AIRole> aiRolesPage = aiRoleService.page(new Page<>(current, size),
                aiRoleService.getQueryWrapper(aiRoleQueryRequest,request));
        return ResultUtils.success(aiRolesPage);
    }


    /**
     * （管理员）对用户添加AI角色进行审核通过
     */
    @GetMapping("/check/ai/role")
    public BaseResponse<Boolean> checkAIRole(@RequestBody CheckRequest checkRequest) {
        if (checkRequest == null || checkRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = checkRequest.getId();
        AIRole aiRole = aiRoleMapper.findAIRoleById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));
        if (aiRole == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        aiRole.setAIRoleReview(1);
        aiRoleMapper.updateById(aiRole);
        return ResultUtils.success(true);
    }
    /**
     * （管理员）对用户添加AI角色进行审核拒绝
     */
    @GetMapping("/reject/ai/role")
    public BaseResponse<Boolean> rejectAIRole(@RequestBody CheckRequest checkRequest) {
        if (checkRequest == null || checkRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = checkRequest.getId();
        AIRole aiRole = aiRoleMapper.findAIRoleById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));
        if (aiRole == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        aiRole.setAIRoleReview(2);
        aiRoleMapper.updateById(aiRole);
        return ResultUtils.success(true);
    }

}
