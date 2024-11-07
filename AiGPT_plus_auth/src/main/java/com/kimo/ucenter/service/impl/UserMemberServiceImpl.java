package com.kimo.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.kimo.common.PageRequest;
import com.kimo.constant.CommonConstant;
import com.kimo.ucenter.mapper.UserMemberMapper;
import com.kimo.ucenter.model.po.UserMember;
import com.kimo.ucenter.service.UserMemberService;
import com.kimo.utils.SqlUtils;
import org.springframework.stereotype.Service;

/**
* @author chen
* @description 针对表【user_member(会员表)】的数据库操作Service实现
* @createDate 2024-10-26 21:56:03
*/
@Service
public class UserMemberServiceImpl extends ServiceImpl<UserMemberMapper, UserMember>
    implements UserMemberService {


    @Override
    public QueryWrapper<UserMember> getQueryWrapper(PageRequest pageRequest) {
        QueryWrapper<UserMember> queryWrapper = new QueryWrapper<>();
        String sortField = pageRequest.getSortField();
        String sortOrder = pageRequest.getSortOrder();
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




