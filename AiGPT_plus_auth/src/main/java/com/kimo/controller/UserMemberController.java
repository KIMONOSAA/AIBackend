package com.kimo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimo.common.BaseResponse;
import com.kimo.common.ErrorCode;
import com.kimo.common.PageRequest;
import com.kimo.common.ResultUtils;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.ucenter.model.po.UserMember;
import com.kimo.ucenter.service.UserMemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@Slf4j
/**
 * @Author kimo
 * @Description  当前模块没有写完整
 * @Date
 * @Param
 * @param null
 * @return
 * @return null
 **/
public class UserMemberController {

    @Autowired
    private UserMemberService userMemberService;

    /**
     * 分页获取会员类型列表
     * @param
     * @param request
     * @return
     */
    @PostMapping("/list/page/member")
    public BaseResponse<Page<UserMember>> listUserMemberByPage(@RequestBody PageRequest pageRequest,
                                                           HttpServletRequest request) {
        if (pageRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = pageRequest.getCurrent();
        long size = pageRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<UserMember> userPage = userMemberService.page(new Page<>(current, size),
                userMemberService.getQueryWrapper(pageRequest));
        return ResultUtils.success(userPage);
    }

}
