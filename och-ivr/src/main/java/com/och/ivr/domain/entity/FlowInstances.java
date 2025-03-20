package com.och.ivr.domain.entity;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.och.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;


/**
 * 存储流程实例的基本信息(FlowInstances)表实体类
 *
 * @author danmo
 * @since 2024-12-17 10:55:17
 */
@Schema
@Builder
@Data
@SuppressWarnings("serial")
@TableName("flow_instances")
public class FlowInstances extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 327301982712989654L;
   
    /**
     *  流程实例唯一标识符
     */

    @Schema(description = "流程实例唯一标识符")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "通话ID")
    @TableField("call_id")
    private Long callId;

    /**
     *  流程ID，用于区分不同流程
     */
    @Schema(description = "流程ID，用于区分不同流程")
    @TableField("flow_id")
    private Long flowId;
    
    
     
    /**
     *  流程实例的状态：1-进行中、2-已完成  3-失败
     */
    @Schema(description = "流程实例的状态：1-进行中、2-已完成  3-失败")
    @TableField("status")
    private Integer status;
    
    
     
    /**
     *  流程开始时间 
     */
    @Schema(description = "流程开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField("start_time")
    private Date startTime;
    
    
     
    /**
     *  流程结束时间 
     */
    @Schema(description = "流程结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField("end_time")
    private Date endTime;
    
    
     
    /**
     *  当前节点ID 
     */
    @Schema(description = "当前节点ID")
    @TableField("current_node_id")
    private String currentNodeId;
    
    
     
    /**
     *  存储流程实例的变量（例如条件判断、数据传递等） 
     */
    @Schema(description = "存储流程实例的变量（例如条件判断、数据传递等）")
    @TableField("variables")
    private String variables;
    
    
    
    
    
    
    


}

