package com.och.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WebSocket 消息事件名称注解
 * <p>标注在 {@link com.och.websocket.factory.WsMsgEventStrategy} 的实现类上，
 * 用于声明该策略处理的 WebSocket 事件类型。
 * WsMsgEventStrategyFactory 通过此注解自动建立事件名称到处理器的映射。</p>
 *
 * <pre>
 * // 使用示例：
 * {@code @WsEventName(name = "HEARTBEAT")}
 * public class WsHeartbeatEventHandler extends AbstractWsMsgEventStrategy {
 *     // 处理心跳消息
 * }
 * </pre>
 *
 * @author danmo
 * @date 2025/05/27
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WsEventName {

    /**
     * WebSocket 事件名称
     * <p>对应客户端发送的 {@code wsMsgPayload.event} 字段值。</p>
     *
     * @return 事件名称（如 HEARTBEAT、AGENT_STATUS）
     */
    String name();
}
