package com.och.esl.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 日志型 ESL 连接客户端。
 * <p>
 * 当前实现不建立真实 TCP 连接，只记录连接意图并返回成功，用于本地开发和单元测试。
 * 后续接入 FreeSWITCH SDK 时新增真实适配器即可，不需要改动连接管理器。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
@ConditionalOnMissingBean(EslConnectionClient.class)
public class LoggingEslConnectionClient implements EslConnectionClient {

    @Override
    public ConnectionResult connect(ConnectionRequest request, Consumer<Map<String, String>> eventConsumer) {
        log.info("[EslConnectionClient] 模拟连接 FreeSWITCH: id={}, host={}:{}, timeoutMs={}",
                request.freeswitchId(), request.host(), request.port(), request.timeoutMs());
        return new ConnectionResult(true, null);
    }

    @Override
    public void disconnect(String freeswitchId) {
        log.info("[EslConnectionClient] 模拟断开 FreeSWITCH: id={}", freeswitchId);
    }
}
