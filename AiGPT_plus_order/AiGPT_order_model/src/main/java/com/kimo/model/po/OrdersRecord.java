package com.kimo.model.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;
/**
 * @author Mr.kimo
 */
@TableName(value ="orders_record")
@Data
public class OrdersRecord implements Serializable {
    /**
     * 支付记录号
     */
    @TableId
    private Long id;

    /**
     * 本系统支付交易号
     */
    private Long payNo;

    /**
     * 第三方支付交易流水号
     */
    private String outPayNo;

    /**
     * 第三方支付交易流水号
     */
    private String outPayChannel;

    private String orderName;

    /**
     * 订单号
     */
    private Long orderId;

    /**
     * 订单总价单位分
     */
    private Long totalPrice;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 支付状态
     */
    private String status;

    /**
     * 支付成功时间
     */
    private LocalDateTime paySuccessTime;

    /**
     * 用户id
     */
    private Long userId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        OrdersRecord other = (OrdersRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPayNo() == null ? other.getPayNo() == null : this.getPayNo().equals(other.getPayNo()))
            && (this.getOutPayNo() == null ? other.getOutPayNo() == null : this.getOutPayNo().equals(other.getOutPayNo()))
            && (this.getOutPayChannel() == null ? other.getOutPayChannel() == null : this.getOutPayChannel().equals(other.getOutPayChannel()))
            && (this.getOrderId() == null ? other.getOrderId() == null : this.getOrderId().equals(other.getOrderId()))
            && (this.getTotalPrice() == null ? other.getTotalPrice() == null : this.getTotalPrice().equals(other.getTotalPrice()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getPaySuccessTime() == null ? other.getPaySuccessTime() == null : this.getPaySuccessTime().equals(other.getPaySuccessTime()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPayNo() == null) ? 0 : getPayNo().hashCode());
        result = prime * result + ((getOutPayNo() == null) ? 0 : getOutPayNo().hashCode());
        result = prime * result + ((getOutPayChannel() == null) ? 0 : getOutPayChannel().hashCode());
        result = prime * result + ((getOrderId() == null) ? 0 : getOrderId().hashCode());
        result = prime * result + ((getTotalPrice() == null) ? 0 : getTotalPrice().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getPaySuccessTime() == null) ? 0 : getPaySuccessTime().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", payNo=").append(payNo);
        sb.append(", outPayNo=").append(outPayNo);
        sb.append(", outPayChannel=").append(outPayChannel);
        sb.append(", orderId=").append(orderId);
        sb.append(", totalPrice=").append(totalPrice);
        sb.append(", createTime=").append(createTime);
        sb.append(", status=").append(status);
        sb.append(", paySuccessTime=").append(paySuccessTime);
        sb.append(", userId=").append(userId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}