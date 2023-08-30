package com.example.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author wxz
 * @date 11:58 2023/8/24
 */
@Data
@TableName("t_order_info")
public class OrderInfo extends BaseEntity
{
    /**
     * 商品名称
     */
    private String title;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 支付类型
     */
    private String paymentType;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 商品id
     */
    private Long productId;

    /**
     * 支付类型
     */
    private Integer totalFee;

    /**
     * 订单二维码连接
     */
    private String codeUrl;

    /**
     * 订单状态
     */
    private String orderStatus;
}
