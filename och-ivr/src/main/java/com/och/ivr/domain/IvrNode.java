package com.och.ivr.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * IVR节点实体
 * <p>
 * IVR流程中的单个节点，包含节点类型、显示名称、配置JSON和可视化坐标。
 * 节点通过边（IvrEdge）串联形成完整的呼叫路由流程。
 * </p>
 */
@Data
@TableName("ivr_node")
public class IvrNode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long versionId;
    private String nodeId;
    private String nodeType;
    private String nodeName;
    private String configJson;
    private Integer posX;
    private Integer posY;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
