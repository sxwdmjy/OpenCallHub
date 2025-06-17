package com.och.system.domain.query.calltask;


import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *  数据源联系人查询参数
 * @author danmo
 * @date 2025/06/16 17:29
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "数据源联系人查询参数")
@Data
public class DataSourceContactQuery extends BaseQuery {

    @Schema(description = "数据源ID")
    private Long sourceId;

    @Schema(description = "联系人手机号")
    private String phone;

    @Schema(description = "联系人UUID")
    private String uuid;
}
