package com.example.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.payment.entity.PaymentInfo;
import com.example.payment.enums.PayType;
import com.example.payment.mapper.PaymentInfoMapper;
import com.example.payment.service.PaymentInfoService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wxz
 * @date 12:06 2023/8/24
 */
@Service
@Slf4j
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService
{
    /**
     * 记录支付日志
     *
     * @param plainText 解密报文
     * @author wxz
     * @date 09:43 2023/8/29
     */
    @Override
    public void createPaymentInfo(String plainText)
    {
        log.info("记录支付日志");

        Gson gson = new Gson();
        HashMap plainTextMap = gson.fromJson(plainText, HashMap.class);

        // 订单号
        String orderNo = (String) plainTextMap.get("out_trade_no");
        // 业务编号
        String transactionId = (String) plainTextMap.get("transaction_id");
        // 支付类型
        String tradeType = (String) plainTextMap.get("trade_type");
        // 交易状态
        String tradeState = (String) plainTextMap.get("trade_state");
        // 用户实际支付金额
        Map<String, Object> amount = (Map) plainTextMap.get("amount");
        int payerTotal = ((Double)amount.get("payer_total")).intValue();

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderNo(orderNo);
        paymentInfo.setPaymentType(PayType.WXPAY.getType());
        paymentInfo.setTransactionId(transactionId);
        paymentInfo.setTradeType(tradeType);
        paymentInfo.setTradeState(tradeState);
        paymentInfo.setPayerTotal(payerTotal);
        paymentInfo.setContent(plainText);

        baseMapper.insert(paymentInfo);
    }
}
