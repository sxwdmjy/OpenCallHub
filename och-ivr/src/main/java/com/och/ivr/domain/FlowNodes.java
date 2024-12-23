package com.och.ivr.domain;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;


/**
 * 存储流程中的节点信息(FlowNodes)表实体类
 *
 * @author danmo
 * @since 2024-12-17 10:55:17
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("flow_nodes")
public class FlowNodes extends BaseEntity implements Serializable {
  private static final long serialVersionUID = -10642688627934110L;
   
    /**
     *  节点唯一标识符
     */

    @Schema(description = "节点唯一标识符")
    @TableId(type = IdType.AUTO)
    private Long id;


     
    /**
     *  节点名称 
     */
    @Schema(description = "节点名称")
    @TableField("name")
    private String name;
    
    
     
    /**
     *  节点类型 0-开始 1-结束 2-放音 3-菜单 4-收号 5-人工 6-留言 7-转接 8-转IVR 9-应答 10-挂机 11-路由 12-子IVR 13-满意度
     */
    @Schema(description = "节点类型 0-开始 1-结束 2-放音 3-菜单 4-收号 5-人工 6-留言 7-转接 8-转IVR 9-应答 10-挂机 11-路由 12-子IVR 13-满意度")
    @TableField("type")
    private String type;
    
    
     
    /**
     *  流程ID，用于区分不同流程中的节点 
     */
    @Schema(description = "流程ID，用于区分不同流程中的节点")
    @TableField("flow_id")
    private Long flowId;
    
    
     
    /**
     *  节点的属性（例如条件表达式、并行处理等） 
     */
    @Schema(description = "节点的属性（例如条件表达式、并行处理等）")
    @TableField("properties")
    private String properties;
    
    
     
    /**
     *  节点优先级，数值越小优先级越高 
     */
    @Schema(description = "节点优先级，数值越小优先级越高")
    @TableField("priority")
    private Integer priority;
    
    
    
    
    
    
    


}

