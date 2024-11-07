package com.kimo.model.dto;

import com.kimo.model.po.OrdersRecord;
import lombok.Data;
/**
 * @author Mr.kimo
 */
@Data
public class PayRecordDto extends OrdersRecord {
    //二维码
    private String qrcode;
}
