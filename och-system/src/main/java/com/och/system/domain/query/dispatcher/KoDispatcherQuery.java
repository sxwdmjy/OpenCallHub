package com.och.system.domain.query.dispatcher;

import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author danmo
 * @date 2023年09月11日 15:01
 */
@Schema
@Data
public class KoDispatcherQuery extends BaseQuery {

    @Schema(description = "ID", hidden = true)
    private Integer id;

    @Schema(description = "分组ID")
    private Integer groupId;

    @Schema(description = "目标URL")
    private String destination;

    @Schema(description = "标识")
    private Integer flags;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态 0-在线 1-下线")
    private Integer status;

    @Schema(description = "ID", hidden = true)
    private List<Integer> ids;
}
