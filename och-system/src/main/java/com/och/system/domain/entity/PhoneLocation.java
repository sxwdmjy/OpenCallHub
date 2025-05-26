package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * (PhoneLocation)表实体类
 *
 * @author danmo
 * @since 2025-05-26 17:12:25
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("phone_location")
public class PhoneLocation implements Serializable {
    private static final long serialVersionUID = -92622949713185653L;

    /**
     *
     */

    @Schema(description = "")
    @TableId(type = IdType.AUTO)
    private Integer id;


    /**
     * 号段前缀
     */
    @Schema(description = "号段前缀")
    @TableField("pref")
    private String pref;


    /**
     * 手机号
     */
    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;


    /**
     * 省份
     */
    @Schema(description = "省份")
    @TableField("province")
    private String province;


    /**
     * 城市
     */
    @Schema(description = "城市")
    @TableField("city")
    private String city;


    /**
     * 运营商类型名称
     */
    @Schema(description = "运营商类型名称")
    @TableField("isp")
    private String isp;


    /**
     * 运营商类型 1：移动 2：联通 3：电信 4：广电 5：工信
     */
    @Schema(description = "运营商类型 1：移动 2：联通 3：电信 4：广电 5：工信")
    @TableField("isp_type")
    private Integer ispType;


    /**
     * 邮编
     */
    @Schema(description = "邮编")
    @TableField("post_code")
    private String postCode;


    /**
     * 区号
     */
    @Schema(description = "区号")
    @TableField("city_code")
    private String cityCode;


    /**
     * 行政区划编码
     */
    @Schema(description = "行政区划编码")
    @TableField("area_code")
    private String areaCode;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField("create_time")
    private Date createTime;
}

