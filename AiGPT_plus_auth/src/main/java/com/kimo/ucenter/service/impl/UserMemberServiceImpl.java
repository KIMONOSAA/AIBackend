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


@Service
/**
 * @Author: Mr.kimo
 * @Date: 9:26
 * @return:
 * @Param:
 * @Description:
 */
public class UserMemberServiceImpl extends ServiceImpl<UserMemberMapper, UserMember>
    implements UserMemberService {


    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 9:25
     * @return: com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.kimo.ucenter.model.po.UserMember>
     * @Param: [com.kimo.common.PageRequest]
     * @Description: 获取已添加数据库的会员类型数据返回前端
     */
    public QueryWrapper<UserMember> getQueryWrapper(PageRequest pageRequest) {
        QueryWrapper<UserMember> queryWrapper = new QueryWrapper<>();
        String sortField = pageRequest.getSortField();
        String sortOrder = pageRequest.getSortOrder();
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




