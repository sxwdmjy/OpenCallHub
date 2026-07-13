package com.och.common.exception;

/**
 * 解析异常
 * <p>在解析 XML、JSON 或其他数据格式时抛出，通常出现在 CDR 解析、
 * FreeSWITCH XML Curl 配置解析等场景。</p>
 */
public class ParserException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * 构造解析异常
     *
     * @param message 异常描述信息
     */
    public ParserException(String message) {
        super(message);
    }

    /**
     * 构造解析异常（含根因）
     *
     * @param message 异常描述信息
     * @param cause   根因异常
     */
    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
