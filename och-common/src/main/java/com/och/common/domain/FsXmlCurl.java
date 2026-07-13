package com.och.common.domain;

import lombok.Data;

import java.util.Map;

/**
 * FreeSWITCH XML Curl 请求参数封装
 * <p>FreeSWITCH通过HTTP GET/POST向XML Curl服务器发送配置请求，
 * 携带section、tag_name、key_name、key_value等参数，该类用于封装这些请求参数。</p>
 *
 * @author danmo
 * @date 2023-09-13
 */
@Data
public class FsXmlCurl {

    /** 请求的配置段：directory、dialplan、configuration等 */
    private String section;

    /** 标签名称（configuration段时使用） */
    private String tagName;

    /** 键名称 */
    private String keyName;

    /** 键值 */
    private String keyValue;

    /** FreeSWITCH主机名 */
    private String hostname;

    /** 所有请求参数 */
    private Map<String, String> metadata;
}
