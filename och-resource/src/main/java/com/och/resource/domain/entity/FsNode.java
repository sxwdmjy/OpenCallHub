package com.och.resource.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * FreeSWITCH节点连接信息实体
 * <p>映射 fs_node 表，存储 FreeSWITCH ESL 连接所需的节点地址、端口、认证密码等配置。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fs_node")
public class FsNode {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 节点唯一标识 */
    @TableField("node_id")
    private String nodeId;

    /** 节点名称 */
    @TableField("node_name")
    private String nodeName;

    /** ESL 服务地址 */
    @TableField("host")
    private String host;

    /** ESL 端口 */
    @TableField("port")
    private Integer port;

    /** ESL 认证密码 */
    @TableField("password")
    private String password;

    /** 是否启用：0-禁用 1-启用 */
    @TableField("enabled")
    private Integer enabled;

    /** 备注 */
    @TableField("remark")
    private String remark;

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
