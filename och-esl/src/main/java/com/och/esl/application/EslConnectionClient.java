package com.och.esl.application;

import java.util.Map;
import java.util.function.Consumer;

/**
 * ESL 连接客户端端口。
 * <p>
 * 连接管理器只依赖该端口，不直接依赖 FreeSWITCH ESL SDK。
 * 真实环境可以实现本接口完成 TCP 连接、认证、事件订阅和事件回调；本地开发使用日志适配器即可。
 * </p>
 *
 * @author danmo
 */
public interface EslConnectionClient {

    /**
     * 建立到 FreeSWITCH 的 ESL 连接。
     *
     * @param request       连接请求
     * @param eventConsumer 原始事件回调，真实客户端收到事件后将 headers 推给该回调
     * @return 连接结果
     */
    ConnectionResult connect(ConnectionRequest request, Consumer<Map<String, String>> eventConsumer);

    /**
     * 断开指定 FreeSWITCH 节点连接。
     *
     * @param freeswitchId FreeSWITCH 节点标识
     */
    void disconnect(String freeswitchId);

    /**
     * 连接请求。
     */
    record ConnectionRequest(String freeswitchId, String host, int port, String password, int timeoutMs) {}

    /**
     * 连接结果。
     */
    record ConnectionResult(boolean connected, String error) {}
}
