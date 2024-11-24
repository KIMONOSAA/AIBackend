package com.kimo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.kimo.api.client.ChartClient;
import com.kimo.mapper.AccuracyMapper;
import com.kimo.model.dto.GouZiAdditionalMessages;
import com.kimo.model.po.Accuracy;
import com.kimo.service.AccuracyService;
import com.kimo.utils.GenericUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

import static com.kimo.constant.RedisConstant.MAX_INSERT_REDIS;

/**
 * @author Mr.kimo
 */
@Service
@Slf4j
public class AccuracyServiceImpl extends ServiceImpl<AccuracyMapper, Accuracy>
    implements AccuracyService {

    @Autowired
    private AccuracyMapper accuracyMapper;

    @Autowired
    private ChartClient chartClient;




    @Override
    public List<Accuracy> getUserAccuracy() {
        return GenericUtils.getItemsBase(MAX_INSERT_REDIS, page -> accuracyMapper.getAccuracyListByPage(page * MAX_INSERT_REDIS, MAX_INSERT_REDIS));

    }
}




