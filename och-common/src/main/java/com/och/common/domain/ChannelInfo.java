package com.och.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 呼叫通道对象
 * @author danmo
 * @date 2023-10-23 15:16
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ChannelInfo {

    /**
     * 呼叫唯一ID
     */
    private Long callId;

    /**
     * 呼叫通道ID
     */
    private String uniqueId;

    /**
     * 呼叫另一条通道ID
     */
    private String otherUniqueId;

    /**
     * 坐席ID
     */
    private Long agentId;
    /**
     * 坐席
     */
    private String agentNumber;

    /**
     * 坐席
     */
    private String agentName;

    /**
     * 1-坐席 2-客户
     */
    private Integer type;

    /**
     * 1-呼入,2-外呼,3-内呼,4-转接,5-咨询,6-监听,7-强插,8-耳语
     */
    private Integer cdrType;

    /**
     * 咨询或转接来源
     */
    private String fromAgent;

    /**
     * 主叫
     */
    private String caller;

    /**
     * 被叫
     */
    private String called;

    /**
     * 显号
     */
    private String display;

    /**
     * 被叫归属地
     */
    private String calledLocation;

    /**
     * 主叫归属地
     */
    private String callerLocation;

    /**
     * 呼叫开始时间
     */
    private Long callTime;

    /**
     * 振铃开始时间
     */
    private Long ringStartTime;

    /**
     * 振铃结束时间
     */
    private Long ringEndTime;

    /**
     * 接通时间
     */
    private Long answerTime;

    /**
     * 桥接时间
     */
    private Long bridgeTime;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 通话时长
     */
    private Long talkTime;

    /**
     * 信令协议(tcp/udp)
     */
    private String sipProtocol;

    /**
     * 呼叫地址
     */
    private String channelName;

    /**
     * 挂机原因
     */
    private String hangupCause;

    /**
     * 回铃音识别
     */
    private String ringCause;

    /**
     * sip状态
     */
    private String sipStatus;

    /**
     * 当前设备状态
     */
    private String state;
}
