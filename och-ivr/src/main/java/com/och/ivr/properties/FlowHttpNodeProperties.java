package com.och.ivr.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FlowHttpNodeProperties extends FlowNodeProperties{

    /**
     * 请求名称
     */
    private String name;

    /**
     * 请求地址
     */
    private String url;

    /**
     * 请求方式 1-GET 2-POST 3-PUT 4-DELETE
     */
    private Integer method;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 请求头
     */
    private List<HttpHeader> headers;

    /**
     * 请求结果字段
     */
    private String result;

    @Data
    public static class HttpHeader{
        private String name;
        private String value;
    }
}
