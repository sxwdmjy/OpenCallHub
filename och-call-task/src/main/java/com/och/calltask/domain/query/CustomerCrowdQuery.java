package com.och.calltask.domain.query;


import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author danmo
 * @date 2025/6/30 16:17
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户群查询参数")
@Data
public class CustomerCrowdQuery extends BaseQuery {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "人群ID列表")
    private List<Long> idList;

    @Schema(description = "人群名称")
    private String name;

    @Schema(description = "启用状态 0-禁用 1-启用")
    private Integer status;

    @Schema(description = "更新方式 1-手动 2-自动")
    private Integer type;

    @Schema(description = "进度 1-待计算 2-计算中 3-计算完成 4-计算失败")
    private List<Integer> progress;
}
