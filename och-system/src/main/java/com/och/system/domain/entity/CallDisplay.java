package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * 号码管理(CallDisplay)表实体类
 *
 * @author danmo
 * @since 2023-10-23 10:47:11
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("call_display")
public class CallDisplay extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -36627269402625579L;

    /**
     * 主键
     */

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 电话号码
     */
    @Schema(description = "电话号码")
    @TableField("phone")
    private String phone;


    /**
     * 号码类型 1-主叫显号 2-被叫显号
     */
    @Schema(description = "号码类型 1-主叫显号 2-被叫显号")
    @TableField("type")
    private Integer type;

    /**
     * 归属地
     */
    @Schema(description = "归属地")
    @TableField("area")
    private String area;


}

