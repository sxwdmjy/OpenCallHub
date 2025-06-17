package com.och.common.domain;

import cn.hutool.core.collection.CollectionUtil;
import com.och.common.constant.FlowDataContext;
import com.och.common.enums.ProcessEnum;
import com.och.common.enums.RouteTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 呼叫对象
 *
 * @author danmo
 * @date 2023-10-23 15:16
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CallInfo {

    /**
     * 呼叫唯一ID
     */
    private Long callId;

    /**
     * 呼叫通道ID列表
     */
    private List<String> uniqueIdList;

    /**
     * 主叫号码
     */
    private String caller;

    /**
     * 主叫显号
     */
    private String callerDisplay;

    /**
     * 被叫号码
     */
    private String callee;

    /**
     * 被叫显号
     */
    private String calleeDisplay;

    /**
     * 号码归属地
     */
    private String numberLocation;

    /**
     * 坐席ID
     */
    private Long agentId;

    /**
     * 坐席号码
     */
    private String agentNumber;

    /**
     * 坐席名称
     */
    private String agentName;


    /**
     * 呼叫方向(1-呼入 2-外呼)
     */
    private Integer direction;

    /**
     * 路由类型
     */
    private Integer routeType;

    /**
     * 开始呼叫时间
     */
    private Long callTime;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 是否隐藏客户号码(0-不隐藏 1-隐藏)
     */
    private Integer hiddenCustomer = 0;

    /**
     * 主叫超时时间
     */
    private Integer callerTimeOut = 10;


    /**
     * 被叫超时时间
     */
    private Integer calleeTimeOut = 10;


    /**
     * 话单通知地址
     */
    private String cdrNotifyUrl;

    /**
     * 1主叫挂机, 2:被叫挂机, 3:平台挂机
     */
    private Integer hangupDir;

    /**
     * 平台挂机原因
     */
    private Integer hangupCause;

    /**
     * 主叫振铃开始时间
     */
    private Long callerRingStartTime;
    /**
     * 主叫振铃结束时间
     */
    private Long callerRingEndTime;

    /**
     * 被叫振铃开始时间
     */
    private Long calleeRingStartTime;
    /**
     * 被叫振铃结束时间
     */
    private Long calleeRingEndTime;

    /**
     * 接听时间
     */
    private Long answerTime;

    /**
     * 录音开始时间
     */
    private Long recordStartTime;

    /**
     * 录音结束时间
     */
    private Long recordEndTime;

    /**
     * 录音时长
     */
    private Long recordTime;

    /**
     * 录音地址
     */
    private String record;

    /**
     * 应答数
     */
    private Integer answerCount = 0;

    /**
     * 技能组ID
     */
    private Long skillId;

    /**
     * 第一次进队列时间
     */
    private Long firstQueueTime;

    /**
     * 进入技能组时间
     */
    private Long queueStartTime;

    /**
     * 出技能组时间
     */
    private Long queueEndTime;

    /**
     * 溢出次数
     */
    private Integer overflowCount;

    /**
     * 呼叫通道信息
     */
    private Map<String, ChannelInfo> channelMap;

    /**
     * 电话流转详情
     */
    private List<CallInfoDetail> detailList;

    /**
     * 执行任务
     */
    private ProcessEnum process;

    /**
     * 流程数据
     */
    private FlowDataContext flowDataContext;

    public void setChannelInfoMap(String uniqueId, ChannelInfo channelInfo) {
        if (CollectionUtil.isEmpty(channelMap)) {
            channelMap = new HashMap<>();
        }
        channelMap.put(uniqueId, channelInfo);
    }

    public void removeChannelInfoMap(String uniqueId) {
        if (CollectionUtil.isNotEmpty(channelMap)) {
            channelMap.remove(uniqueId);
        }
    }


    public void addUniqueIdList(String uniqueId) {
        if (CollectionUtil.isEmpty(uniqueIdList)) {
            this.uniqueIdList = new ArrayList<>();
        }
        this.uniqueIdList.add(uniqueId);
    }

    public void removeUniqueIdList(String uniqueId) {
        if (CollectionUtil.isNotEmpty(uniqueIdList)) {
            uniqueIdList.remove(uniqueId);
        }
    }

    public void addDetailList(CallInfoDetail detail) {
        if (CollectionUtil.isEmpty(detailList)) {
            this.detailList = new ArrayList<>();
        }
        this.detailList.add(detail);
    }

    public void setSkillHangUpReason(String reason) {
        for (CallInfoDetail detail : detailList) {
            if (detail.getTransferType() == 3) {
                detail.setReason(reason);
                return;
            }
        }
    }

    public Integer gainSkillAfterTime() {
        if (CollectionUtil.isNotEmpty(this.detailList)) {
            for (CallInfoDetail detail : this.detailList) {
                if (detail.getTransferType() == 3) {
                    return detail.getAfterTime();
                }
            }
        }
        return null;
    }
}
