package com.kimo.ucenter.service;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.common.PageRequest;
import com.kimo.ucenter.model.po.UserMember;

/**
* @author chen
* @description 针对表【user_member(会员表)】的数据库操作Service
* @createDate 2024-10-26 21:56:03
*/
public interface UserMemberService extends IService<UserMember> {

    Wrapper<UserMember> getQueryWrapper(PageRequest pageRequest);
}
