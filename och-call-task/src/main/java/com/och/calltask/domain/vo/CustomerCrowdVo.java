package com.och.calltask.domain.vo;


import com.alibaba.fastjson2.JSONObject;
import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author danmo
 * @date 2025/6/30 16:17
 */

@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户群")
@Data
public class CustomerCrowdVo extends BaseVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "人群名称")
    private String name;

    @Schema(description = "是否允许追加 0-不允许 1-允许")
    private Integer addition;

    @Schema(description = "是否启用 0-禁用 1-启用")
    private Integer status;

    @Schema(description = "更新方式 1-手动 2-自动")
    private Integer type;

    @Schema(description = "策略条件")
    private JSONObject swipe;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "进度 1-待计算 2-计算中 3-计算完成 4-计算失败")
    private Integer progress;

    @Schema(description = "失败原因")
    private String reason;

    @Schema(description = "人群数量")
    private Integer crowdNum;
}
