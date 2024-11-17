package com.kimo.ucenter.service;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.common.PageRequest;
import com.kimo.ucenter.model.po.UserMember;

/**
 * @Author: Mr.kimo
 * @Date: 10:47
 * @return:
 * @Param:
 * @Description:
 */
public interface UserMemberService extends IService<UserMember> {

    Wrapper<UserMember> getQueryWrapper(PageRequest pageRequest);
}
