package com.och.calltask.domain.query;


import com.och.common.domain.ConditionInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

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

    @Schema(description = "主键ID", hidden = true)
    private Long id;


    /**
     * 人群名称
     */
    @NotBlank(message = "人群名称不能为空")
    @Schema(description = "人群名称")
    private String name;


    /**
     * 是否启用 0-否 1-是
     */
    @NotNull(message = "启用状态不能为空")
    @Schema(description = "是否启用 0-禁用 1-启用")
    private Integer status;


    /**
     * 更新方式 1-手动 2-自动
     */
    @NotNull(message = "更新方式不能为空")
    @Schema(description = "更新方式 1-手动 2-自动")
    private Integer type;


    /**
     * 策略条件
     */
    @NotNull(message = "策略条件不能为空")
    @Size(min = 1, message = "策略条件不能为空")
    @Schema(description = "策略条件")
    private List<ConditionInfo> swipe;


    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
