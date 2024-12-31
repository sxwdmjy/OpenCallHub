package com.och.system.domain.query.route;

import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author danmo
 * @date 2023-10-18 14:34
 **/
@Schema
@Data
public class CallRouteQuery extends BaseQuery {

    @Schema(description = "ID")
    private Long id;

    private List<Long> ids;

    @Schema(description = "路由名称")
    private String name;

    @Schema(description = "路由号码")
    private String routeNumber;

    @Schema(description = "路由类型  1-呼入 2-呼出")
    private Integer type;

    @Schema(description = "路由优先级")
    private Integer level;

    @Schema(description = "状态  0-未启用 1-启用")
    private Integer status;

    @Schema(description = "呼出路由类型")
    private Integer routeType;


}
