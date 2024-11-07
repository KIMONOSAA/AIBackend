package com.kimo.model.dto;

import lombok.Data;

import java.util.Date;
/**
 * @author Mr.kimo
 */
@Data
public class AddOrderDto {
    /**
     * 总价
     */
    private Long totalPrice;


    /**
     * 订单类型
     */
    private String orderType;

    /**
     * 订单名称
     */
    private String orderName;

    /**
     * 订单描述
     */
    private String orderDescrip;

    /**
     * 订单明细json
     */
    private String orderDetail;

    /**
     * 外部系统业务id
     */
    private String outBusinessId;
}
