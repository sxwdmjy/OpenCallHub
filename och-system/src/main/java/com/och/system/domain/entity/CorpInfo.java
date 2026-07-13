package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 企业信息实体
 * <p>映射 corp_info 表，存储企业信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("corp_info")
public class CorpInfo {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 企业名称 */
    @TableField("corp_name")
    private String corpName;

    /** 企业编码 */
    @TableField("corp_code")
    private String corpCode;

    /** 验证秘钥 */
    @TableField("secret_key")
    private String secretKey;

    /** 企业状态：1-启用 2-禁用 */
    @TableField("status")
    private Integer status;

    /** 坐席数量 */
    @TableField("seat_num")
    private Integer seatNum;

    /** IVR并发数量 */
    @TableField("ivr_num")
    private Integer ivrNum;

    /** 录音保留天数 */
    @TableField("record_num")
    private Integer recordNum;

    /** 账户余额 */
    @TableField("balance")
    private String balance;

    /** 企业负责人 */
    @TableField("corp_manage")
    private String corpManage;

    /** 企业负责人手机号 */
    @TableField("corp_manage_phone")
    private String corpManagePhone;

    /** 备注 */
    @TableField("remark")
    private String remark;

    /** 回调地址 */
    @TableField("call_back_path")
    private String callBackPath;

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
