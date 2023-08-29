package com.example.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.payment.entity.OrderInfo;
import com.example.payment.entity.RefundInfo;
import com.example.payment.mapper.RefundInfoMapper;
import com.example.payment.service.OrderInfoService;
import com.example.payment.service.RefundInfoService;
import com.example.payment.util.OrderNoUtils;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wxz
 * @date 12:07 2023/8/24
 */
@Slf4j
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService
{
    @Resource
    private OrderInfoService orderInfoService;

    /**
     * 退款
     *
     * @param orderNo 订单编号
     * @param reason  退款原因
     * @author wxz
     * @date 14:29 2023/8/29
     */
    @Override
    public RefundInfo createRefundInfoByOrderNo(String orderNo, String reason)
    {
        // 根据订单号获取订单信息
        OrderInfo orderInfo = orderInfoService.getOrderByOrderNo(orderNo);

        // 根据订单号生成退款订单
        RefundInfo refundInfo = new RefundInfo();
        // 订单编号
        refundInfo.setOrderNo(orderNo);
        // 退款编号
        refundInfo.setRefundNo(OrderNoUtils.getRefundNo());
        // 原订单金额
        refundInfo.setTotalFee(orderInfo.getTotalFee());
        // 退款金额
        refundInfo.setRefund(orderInfo.getTotalFee());
        // 退款原因
        refundInfo.setReason(reason);

        // 保存退款订单
        baseMapper.insert(refundInfo);

        return refundInfo;
    }

    /**
     * 更新退款订单
     *
     * @param content 退款结果通知参数
     * @author wxz
     * @date 14:42 2023/8/29
     */
    @Override
    public void updateRefund(String content)
    {
        // 将JSON字符串转换为Map
        Gson gson = new Gson();
        Map<String, String> resultMap = gson.fromJson(content, HashMap.class);

        // 根据退款编号修改退款单
        QueryWrapper<RefundInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("refund_no", resultMap.get("out_refund_no"));

        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setRefundId(resultMap.get("refund_id"));

        // 查询退款和申请退款中的返回参数
        if (resultMap.get("status") != null)
        {
            refundInfo.setRefundStatus(resultMap.get("status"));
            refundInfo.setContentReturn(content);
        }
        // 退款回调中的回调参数
        if (resultMap.get("refund_status") != null)
        {
            refundInfo.setRefundStatus(resultMap.get("refund_status"));
            refundInfo.setContentNotify(content);
        }

        baseMapper.update(refundInfo, wrapper);
    }
}
