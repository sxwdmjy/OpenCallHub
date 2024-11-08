package com.och.system.domain.vo.route;

import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author danmo
 * @date 2023-10-18 14:35
 **/
@Schema
@Data
public class CallRouteListVo extends BaseVo {

    @Schema(description = "主键ID")
    private Long id;


    /**
     * 主叫号码
     */
    @Schema(description = "主叫号码")
    private String callerNum;


    /**
     * 路由号码
     */
    @Schema(description = "路由号码")
    private String routeNum;


    /**
     * 号码匹配最小长度
     */
    @Schema(description = "号码匹配最小长度")
    private Integer lenMin;


    /**
     * 号码匹配最大长度
     */
    @Schema(description = "号码匹配最大长度")
    private Integer lenMax;


    /**
     * 主叫替换规则
     */
    @Schema(description = "主叫替换规则")
    private String callerPattern;


    /**
     * 主叫替换号码
     */
    @Schema(description = "主叫替换号码")
    private String callerReplaceNum;


    /**
     * 被叫替换规则
     */
    @Schema(description = "被叫替换规则")
    private String calleePattern;


    /**
     * 被叫替换号码
     */
    @Schema(description = "被叫替换号码")
    private String calleeReplaceNum;


    /**
     * 状态  0-未启用 1-启用
     */
    @Schema(description = "状态  0-未启用 1-启用")
    private Integer status;

}
