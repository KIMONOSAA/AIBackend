package com.kimo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.model.po.Accuracy;

import java.util.List;
/**
 * @author Mr.kimo
 */
public interface AccuracyService extends IService<Accuracy> {

    List<Accuracy> getUserAccuracy();
}
