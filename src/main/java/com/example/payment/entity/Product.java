package com.example.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author wxz
 * @date 12:00 2023/8/24
 */
@Data
@TableName("t_product")
public class Product extends BaseEntity
{
    /**
     * 商品名称
     */
    private String title;

    /**
     * 价格（分）
     */
    private Integer price;
}
