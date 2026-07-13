package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 号码归属地实体
 * <p>映射 phone_location 表，存储手机号码归属地信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("phone_location")
public class PhoneLocation {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 号段前缀 */
    @TableField("pref")
    private String pref;

    /** 手机号 */
    @TableField("phone")
    private String phone;

    /** 省份 */
    @TableField("province")
    private String province;

    /** 城市 */
    @TableField("city")
    private String city;

    /** 运营商类型名称 */
    @TableField("isp")
    private String isp;

    /** 运营商类型：1-移动 2-联通 3-电信 4-广电 5-工信 */
    @TableField("isp_type")
    private Integer ispType;

    /** 邮编 */
    @TableField("post_code")
    private String postCode;

    /** 区号 */
    @TableField("city_code")
    private String cityCode;

    /** 行政区划编码 */
    @TableField("area_code")
    private String areaCode;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

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

