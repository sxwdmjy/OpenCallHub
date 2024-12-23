package com.och.ivr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * 存储流程中节点之间的连接信息（流转规则）(FlowEdges)表实体类
 *
 * @author danmo
 * @since 2024-12-17 10:55:17
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("flow_edges")
public class FlowEdges extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 253662002536646723L;

    /**
     * 边的唯一标识符
     */

    @Schema(description = "边的唯一标识符")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 源节点ID
     */
    @Schema(description = "源节点ID")
    @TableField("source_node_id")
    private Long sourceNodeId;


    /**
     * 目标节点ID
     */
    @Schema(description = "目标节点ID")
    @TableField("target_node_id")
    private Long targetNodeId;


    /**
     * 流转条件，决定从源节点到目标节点的流转条件（可为空表示无条件）
     */
    @Schema(description = "流转条件，决定从源节点到目标节点的流转条件（可为空表示无条件）")
    @TableField("condition")
    private String condition;


    /**
     * 流程ID，标识这条边属于哪个流程
     */
    @Schema(description = "流程ID，标识这条边属于哪个流程")
    @TableField("flow_id")
    private String flowId;


}

