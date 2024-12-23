package com.och.ivr.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema
@Data
public class FlowEdgeVo {

    /**
     * 边的唯一标识符
     */

    @Schema(description = "边的唯一标识符")
    private Long id;


    /**
     * 源节点ID
     */
    @Schema(description = "源节点ID")
    private Long sourceNodeId;


    /**
     * 目标节点ID
     */
    @Schema(description = "目标节点ID")
    private Long targetNodeId;


    /**
     * 流转条件，决定从源节点到目标节点的流转条件（可为空表示无条件）
     */
    @Schema(description = "流转条件，决定从源节点到目标节点的流转条件（可为空表示无条件）")
    private String condition;


    /**
     * 流程ID，标识这条边属于哪个流程
     */
    @Schema(description = "流程ID，标识这条边属于哪个流程")
    private Long flowId;
}
