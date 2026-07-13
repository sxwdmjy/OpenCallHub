package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 行政区域实体
 * <p>映射 och_region 表，存储行政区域信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("och_region")
public class OchRegion {

    /** 行政代码 */
    @TableId(value = "region_id", type = IdType.INPUT)
    private String regionId;

    /** 名称 */
    @TableField("region_name")
    private String regionName;

    /** 父级行政代码 */
    @TableField("parent_id")
    private String parentId;

    /** 简称 */
    @TableField("short_name")
    private String shortName;

    /** 层级 */
    @TableField("level")
    private String level;

    /** 区号 */
    @TableField("city_code")
    private String cityCode;

    /** 邮政编码 */
    @TableField("zip_code")
    private String zipCode;

    /** 合并名称 */
    @TableField("merger_name")
    private String mergerName;

    /** 经度 */
    @TableField("lng")
    private String lng;

    /** 纬度 */
    @TableField("lat")
    private String lat;

    /** 拼音 */
    @TableField("pinyin")
    private String pinyin;

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

