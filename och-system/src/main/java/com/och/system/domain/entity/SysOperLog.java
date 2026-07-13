package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 系统操作日志实体
 * <p>映射 sys_oper_log 表，存储系统操作日志记录。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_oper_log")
public class SysOperLog {

    /** 日志主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    @TableField("title")
    private String title;

    /** 业务类型：0=其它 1=新增 2=修改 3=删除 4=查询 5=导出 6=导入 7=登录 8=登出 */
    @TableField("business_type")
    private Integer businessType;

    /** 方法名称 */
    @TableField("method")
    private String method;

    /** 请求方式 */
    @TableField("request_method")
    private String requestMethod;

    /** 操作类别：0=其它 1=后台用户 2=手机端用户 */
    @TableField("operator_type")
    private Integer operatorType;

    /** 操作人员 */
    @TableField("oper_name")
    private String operName;

    /** 请求URL */
    @TableField("oper_url")
    private String operUrl;

    /** 主机地址 */
    @TableField("oper_ip")
    private String operIp;

    /** 操作地点 */
    @TableField("oper_location")
    private String operLocation;

    /** 请求参数 */
    @TableField("oper_param")
    private String operParam;

    /** 返回参数 */
    @TableField("json_result")
    private String jsonResult;

    /** 操作状态：0=正常 1=异常 */
    @TableField("status")
    private Integer status;

    /** 错误消息 */
    @TableField("error_msg")
    private String errorMsg;

    /** 操作时间 */
    @TableField("oper_time")
    private Date operTime;

    /** 消耗时间 */
    @TableField("cost_time")
    private Long costTime;

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

