package com.example.payment.vo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wxz
 * @date 11:22 2023/8/24
 */
@Data
public class R {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private Map<String, Object> data = new HashMap<>();

    /**
     * 响应成功
     *
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 11:27 2023/8/24
     */
    public static R ok() {
        R r = new R();
        r.setCode(0);
        r.setMsg("success");
        return r;
    }

    /**
     * 响应失败
     *
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 11:28 2023/8/24
     */
    public static R error() {
        R r = new R();
        r.setCode(1);
        r.setMsg("error");
        return r;
    }

    /**
     * 返回数据
     *
     * @return com.example.payment.vo.R
     * @author wxz
     * @date 11:30 2023/8/24
     */
    public R data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
