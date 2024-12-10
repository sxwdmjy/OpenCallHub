package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import com.och.system.domain.query.callin.CallInPhoneAddQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * 呼入号码表(CallInPhone)表实体类
 *
 * @author danmo
 * @since 2024-12-10 10:29:11
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("call_in_phone")
public class CallInPhone extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -88350276197174009L;

    /**
     * 主键
     */

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 名称
     */
    @Schema(description = "名称")
    @TableField("name")
    private String name;


    /**
     * 呼入号码
     */
    @Schema(description = "呼入号码")
    @TableField("phone")
    private String phone;


    public void setQuery2Entity(CallInPhoneAddQuery query) {
        this.id = query.getId();
        this.name = query.getName();
        this.phone = query.getPhone();
    }

}

