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
@TableName(value ="orders")
@Data
public class Orders implements Serializable {
    /**
     * 订单号
     */
    @TableId
    private Long id;

    /**
     * 总价
     */
    private Long totalPrice;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 交易状态
     */
    private String status;

    /**
     * 用户id
     */
    private String userId;

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
        Orders other = (Orders) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTotalPrice() == null ? other.getTotalPrice() == null : this.getTotalPrice().equals(other.getTotalPrice()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getOrderType() == null ? other.getOrderType() == null : this.getOrderType().equals(other.getOrderType()))
            && (this.getOrderName() == null ? other.getOrderName() == null : this.getOrderName().equals(other.getOrderName()))
            && (this.getOrderDescrip() == null ? other.getOrderDescrip() == null : this.getOrderDescrip().equals(other.getOrderDescrip()))
            && (this.getOrderDetail() == null ? other.getOrderDetail() == null : this.getOrderDetail().equals(other.getOrderDetail()))
            && (this.getOutBusinessId() == null ? other.getOutBusinessId() == null : this.getOutBusinessId().equals(other.getOutBusinessId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTotalPrice() == null) ? 0 : getTotalPrice().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getOrderType() == null) ? 0 : getOrderType().hashCode());
        result = prime * result + ((getOrderName() == null) ? 0 : getOrderName().hashCode());
        result = prime * result + ((getOrderDescrip() == null) ? 0 : getOrderDescrip().hashCode());
        result = prime * result + ((getOrderDetail() == null) ? 0 : getOrderDetail().hashCode());
        result = prime * result + ((getOutBusinessId() == null) ? 0 : getOutBusinessId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", totalPrice=").append(totalPrice);
        sb.append(", createTime=").append(createTime);
        sb.append(", status=").append(status);
        sb.append(", userId=").append(userId);
        sb.append(", orderType=").append(orderType);
        sb.append(", orderName=").append(orderName);
        sb.append(", orderDescrip=").append(orderDescrip);
        sb.append(", orderDetail=").append(orderDetail);
        sb.append(", outBusinessId=").append(outBusinessId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}