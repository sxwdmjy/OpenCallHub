package com.och.system.domain.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * WebSocket坐席消息
 * <p>坐席实时通信消息对象。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallWsMsg {

    /** 呼叫ID */
    private Long callId;

    /** 呼叫方向：1-呼入 2-呼出 */
    private Integer direction;

    /** 主叫号码 */
    private String caller;

    /** 被叫号码 */
    private String callee;

    /** 状态：0-空闲 1-忙碌 2-正在呼叫 3-被叫振铃 4-主叫振铃 5-通话中 6-结束通话 */
    private Integer status;

    /** 消息类型：1-坐席状态 */
    private Integer msgType;

    /** 消息内容 */
    private String content;
}
