package com.och.calltask.domain.query;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @date 2025/6/30 16:17
 */
@Schema(description = "客户群新增参数")
@Data
public class CustomerCrowdAddQuery {

    /**
     * 主键ID
     */

    @Schema(description = "主键ID")
    private Long id;


    /**
     * 人群名称
     */
    @Schema(description = "人群名称")
    private String name;


    /**
     * 是否允许追加 0-不允许 1-允许
     */
    @Schema(description = "是否允许追加 0-不允许 1-允许")
    private Integer addition;


    /**
     * 是否启用 0-否 1-是
     */
    @Schema(description = "是否启用 0-禁用 1-启用")
    private Integer status;


    /**
     * 更新方式 1-手动 2-自动
     */
    @Schema(description = "更新方式 1-手动 2-自动")
    private Integer type;


    /**
     * 策略条件
     */
    @Schema(description = "策略条件")
    private String swipe;


    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
