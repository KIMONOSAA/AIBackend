package com.kimo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kimo.model.po.OrdersDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Mr.kimo
 */
public interface OrdersDetailMapper extends BaseMapper<OrdersDetail> {
    int insertBatch(@Param("orderDetails") List<OrdersDetail> orderDetails);
}




