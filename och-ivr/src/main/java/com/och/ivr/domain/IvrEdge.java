package com.och.ivr.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * IVR边（连线）实体
 * <p>
 * 连接两个IVR节点的有向边，可携带条件表达式（如DTMF按键值或工作时间判断条件），
 * 用于决定呼叫在不同节点间的路由方向。
 * </p>
 */
@Data
@TableName("ivr_edge")
public class IvrEdge {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long versionId;
    private String edgeId;
    private String sourceNodeId;
    private String targetNodeId;
    private String conditionExpr;
    private String label;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
