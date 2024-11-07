package com.kimo.service.impl;


import com.kimo.model.dto.answer.WrongInformation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
/**
 * @author Mr.kimo
 */
@Data
public class WrongInformationList {
    private List<WrongInformation> wrongInformations = new ArrayList<>();  // 初始化为一个空列表
}
