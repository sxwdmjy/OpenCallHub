package com.och.calltask.domain.query;


import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户群客户查询参数
 * @author danmo
 * @date 2025/7/4 9:53
 */
@Schema(description = "客户群客户查询参数")
@EqualsAndHashCode(callSuper = true)
@Data
public class CrowdCustomerQuery extends BaseQuery {

    @NotNull(message = "人群ID不能为空")
    @Schema(description = "人群ID")
    private Long crowdId;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "客户电话")
    private String customerPhone;

    @Schema(description = "客户性别 0-未知 1-男 2-女")
    private Integer customerSex;

    @Schema(description = "客户来源 0-手动创建 1-文件导入 2-API导入")
    private Integer customerSource;
}
