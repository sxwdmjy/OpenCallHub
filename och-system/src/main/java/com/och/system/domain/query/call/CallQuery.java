package com.och.system.domain.query.call;

import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "拨打入参")
@Data
public class CallQuery extends BaseQuery {

    @NotEmpty(message = "坐席ID不能为空")
    @Schema(description = "坐席ID")
    private Long agentId;

    @Schema(description = "员工ID")
    private Long userId;

    @Schema(description = "主叫",hidden = true)
    private String caller;

    @NotEmpty(message = "被叫号码不能为空")
    @Schema(description = "被叫")
    private String callee;

    @Schema(description = "主叫超时时间,默认10秒")
    private Integer callerTimeOut = 10;


    @Schema(description = "被叫超时时间,默认10秒")
    private Integer calleeTimeOut = 10;


    @Schema(description = "是否隐藏客户号码(0-不隐藏 1-隐藏)")
    private Integer hiddenCustomer = 0;

}
