package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 区号区域编码实体
 * <p>映射 och_area_code 表，存储基于高德地图整理的区号匹配数据。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("och_area_code")
public class OchAreaCode {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 省份名称 */
    @TableField("provinceName")
    private String provincename;

    /** 城市名称 */
    @TableField("cityName")
    private String cityname;

    /** 省份地域编码 */
    @TableField("provinceAdcode")
    private Integer provinceadcode;

    /** 省份中心坐标 */
    @TableField("provinceCenter")
    private String provincecenter;

    /** 城市区号 */
    @TableField("cityCode")
    private String citycode;

    /** 城市地域编码 */
    @TableField("cityAdcode")
    private Integer cityadcode;

    /** 城市中心坐标 */
    @TableField("cityCenter")
    private String citycenter;

    /** 区域映射 */
    @TableField("districts")
    private String districts;

    /** 省份国际ISO编码 */
    @TableField("isoCode")
    private String isocode;

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

