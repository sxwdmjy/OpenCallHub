package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * CDR话单实体
 * <p>映射 fs_cdr 表，存储通话详细记录。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fs_cdr")
public class FsCdr {

    /** 通话唯一ID */
    @TableId(type = IdType.INPUT)
    private Long callId;

    /** 通话ID */
    @TableField("uuid")
    private String uuid;

    /** 呼叫方向：outbound-呼出 inbound-呼入 */
    @TableField("direction")
    private String direction;

    /** 本地呼叫地址 */
    @TableField("sip_local_network_addr")
    private String sipLocalNetworkAddr;

    /** 呼叫IP */
    @TableField("sip_network_ip")
    private String sipNetworkIp;

    /** 主叫号码 */
    @TableField("caller_id_number")
    private String callerIdNumber;

    /** 主叫显号 */
    @TableField("caller_display")
    private String callerDisplay;

    /** 被叫号码 */
    @TableField("destination_number")
    private String destinationNumber;

    /** 被叫显号 */
    @TableField("destination_display")
    private String destinationDisplay;

    /** 开始时间 */
    @TableField("start_stamp")
    private Date startStamp;

    /** 应答时间 */
    @TableField("answer_stamp")
    private Date answerStamp;

    /** 结束时间 */
    @TableField("end_stamp")
    private Date endStamp;

    /** 桥接时间 */
    @TableField("bridge_stamp")
    private Date bridgeStamp;

    /** 振铃时间 */
    @TableField("progress_stamp")
    private Date progressStamp;

    /** 呼叫时长 */
    @TableField("duration")
    private Integer duration;

    /** 应答时长 */
    @TableField("answer_sec")
    private Integer answerSec;

    /** 计费时长 */
    @TableField("bill_sec")
    private Integer billSec;

    /** 挂断原因 */
    @TableField("hangup_cause")
    private String hangupCause;

    /** 录音开始时间 */
    @TableField("record_start_time")
    private Date recordStartTime;

    /** 录音结束时间 */
    @TableField("record_end_time")
    private Date recordEndTime;

    /** 录音时长 */
    @TableField("record_sec")
    private Integer recordSec;

    /** 录音地址 */
    @TableField("record")
    private String record;

    /** 读编码类型 */
    @TableField("read_codec")
    private String readCodec;

    /** 写编码类型 */
    @TableField("write_codec")
    private String writeCodec;

    /** 挂断意向 */
    @TableField("sip_hangup_disposition")
    private String sipHangupDisposition;

    /** 负载数据 */
    @TableField(exist = false)
    private String payload;

    /** 话单通知地址 */
    @TableField(exist = false)
    private String cdrNotifyUrl;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    /** 逻辑删除：0-正常 1-已删除 */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

}

