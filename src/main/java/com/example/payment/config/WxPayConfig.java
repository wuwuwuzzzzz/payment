package com.example.payment.config;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.ScheduledUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.Data;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

/**
 * @author wxz
 * @date 10:20 2023/8/25
 */
@Configuration
@PropertySource("classpath:wxpay.properties")
@ConfigurationProperties(prefix = "wxpay")
@Data
public class WxPayConfig
{
    /**
     * 商户号
     */
    private String mchId;

    /**
     * 商户API证书序列号
     */
    private String mchSerialNo;

    /**
     * 商户私钥文件
     */
    private String privateKeyPath;

    /**
     * APIv3密钥
     */
    private String apiV3Key;

    /**
     * APPID
     */
    private String appid;

    /**
     * 微信服务器地址
     */
    private String domain;

    /**
     * 接收结果通知地址
     */
    private String notifyDomain;

    /**
     * 获取商户的私钥文件
     *
     * @param filePath 文件路径
     * @return java.security.PrivateKey
     * @author wxz
     * @date 10:53 2023/8/25
     */
    public PrivateKey getPrivateKey(String filePath)
    {
        try
        {
            return PemUtil.loadPrivateKey(new FileInputStream(filePath));
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException("私钥文件不存在", e);
        }
    }

    /**
     * 获取签名验证器
     *
     * @return com.wechat.pay.contrib.apache.httpclient.auth.ScheduledUpdateCertificatesVerifier
     * @author wxz
     * @date 11:10 2023/8/25
     */
    @Bean
    public ScheduledUpdateCertificatesVerifier getVerifier()
    {
        // 获取商户私钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        // 私钥签名对象
        PrivateKeySigner privateKeySigner = new PrivateKeySigner(mchSerialNo, privateKey);
        // 身份认证对象
        WechatPay2Credentials wechatPay2Credentials = new WechatPay2Credentials(mchId, privateKeySigner);
        // 从证书管理器中获取verifier
        return new ScheduledUpdateCertificatesVerifier(wechatPay2Credentials, apiV3Key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 获取HTTP请求对象
     *
     * @return org.apache.http.impl.client.CloseableHttpClient
     * @author wxz
     * @date 11:15 2023/8/25
     */
    @Bean
    public CloseableHttpClient getWxPayClient(ScheduledUpdateCertificatesVerifier verifier)
    {
        return WechatPayHttpClientBuilder.create()
                                         .withMerchant(mchId, mchSerialNo, getPrivateKey(privateKeyPath))
                                         .withValidator(new WechatPay2Validator(verifier)).build();
    }

}
