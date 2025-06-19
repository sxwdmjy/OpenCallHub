package com.och.calltask.domain.query;


import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author danmo
 * @date 2025/06/16 16:25
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "数据源查询参数")
@Data
public class DataSourceQuery extends BaseQuery {

    @Schema(description = "数据源ID")
    private Long id;

    @Schema(description = "数据源ID")
    private List<Long> idList;

    @Schema(description = "数据源名称")
    private String name;
}
