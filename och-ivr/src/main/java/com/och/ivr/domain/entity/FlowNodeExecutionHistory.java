package com.och.ivr.domain.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.och.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;


/**
 * 记录每次节点执行的历史记录(FlowNodeExecutionHistory)表实体类
 *
 * @author danmo
 * @since 2024-12-17 10:55:17
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("flow_node_execution_history")
public class FlowNodeExecutionHistory extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 831652463054354395L;
   
    /**
     *  节点执行历史记录ID
     */

    @Schema(description = "节点执行历史记录ID")
    @TableId(type = IdType.AUTO)
    private Long id;


     
    /**
     *  流程实例ID 
     */
    @Schema(description = "流程实例ID")
    @TableField("instance_id")
    private Long instanceId;
    
    
     
    /**
     *  节点ID 
     */
    @Schema(description = "节点ID")
    @TableField("node_id")
    private String nodeId;
    
    
     
    /**
     *  节点执行状态：1-进入 2-退出 3-跳过 4-失败
     */
    @Schema(description = "节点执行状态：1-进入 2-退出 3-跳过 4-失败")
    @TableField("status")
    private Integer status;
    
    
     
    /**
     *  节点开始执行时间 
     */
    @Schema(description = "节点开始执行时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField("start_time")
    private Date startTime;
    
    
     
    /**
     *  节点结束执行时间 
     */
    @Schema(description = "节点结束执行时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField("end_time")
    private Date endTime;
    
    
     
    /**
     *  节点执行时长（秒） 
     */
    @Schema(description = "节点执行时长（秒）")
    @TableField("duration")
    private Integer duration;
    
    
     
    /**
     *  节点执行描述 
     */
    @Schema(description = "节点执行描述")
    @TableField("description")
    private String description;
    
    
    
    
    
    
    


}

