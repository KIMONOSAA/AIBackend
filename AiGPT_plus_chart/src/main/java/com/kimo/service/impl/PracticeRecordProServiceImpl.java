package com.kimo.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.kimo.common.ErrorCode;
import com.kimo.common.PageRequest;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.exception.ThrowUtils;
import com.kimo.feignclient.UserClient;
import com.kimo.mapper.PracticeRecordProMapper;
import com.kimo.model.dto.chart.PracticeRecordRequest;
import com.kimo.model.dto.chart.UserDto;
import com.kimo.model.dto.po.Chart;
import com.kimo.model.dto.po.PracticeRecordPro;
import com.kimo.service.PracticeRecordProService;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Mr.kimo
 */
@Service
public class PracticeRecordProServiceImpl extends ServiceImpl<PracticeRecordProMapper, PracticeRecordPro>
    implements PracticeRecordProService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private ServletUtils servletUtils;

    @Override
    public Page<PracticeRecordPro> listMyChartByPage(HttpServletRequest request, PracticeRecordRequest pageRequest) {
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto user = userClient.GobalGetLoginUser(username);

        ThrowUtils.throwIf(user == null,ErrorCode.NOT_LOGIN_ERROR);
        Long userId = user.getId();
        long current = pageRequest.getCurrent();
        long size = pageRequest.getPageSize();
        pageRequest.setUserId(userId);
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        return this.page(new Page<>(current, size),
                this.getQueryWrapper(pageRequest));
    }

    private Wrapper<PracticeRecordPro> getQueryWrapper(PracticeRecordRequest pageRequest) {

        QueryWrapper<PracticeRecordPro> queryWrapper = new QueryWrapper<>();
        if (pageRequest == null) {
            return queryWrapper;
        }

        String subjects = pageRequest.getSubjects();
        String qualifications = pageRequest.getQualifications();
        Long userId1 = pageRequest.getUserId();
        String practiceStatus = pageRequest.getPracticeStatus();
        Integer userError = pageRequest.getUserError();
        Integer userRight = pageRequest.getUserRight();
        int current = pageRequest.getCurrent();
        int pageSize = pageRequest.getPageSize();
        String sortField = pageRequest.getSortField();
        String sortOrder = pageRequest.getSortOrder();

        queryWrapper.like(StringUtils.isNotBlank(subjects), SqlConstants.SUBJECTS, subjects);
        queryWrapper.like(StringUtils.isNotBlank(qualifications), SqlConstants.QUALIFICATION, qualifications);
        queryWrapper.like(StringUtils.isNotBlank(practiceStatus), SqlConstants.PRACTICESTATUS, practiceStatus);
//        queryWrapper.like(StringUtils.isNotBlank(userError),SqlConstants.USERERROR, userError);
//        queryWrapper.like(SqlConstants.USERRIGHT, userRight);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId1), SqlConstants.USER_ID, userId1);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




