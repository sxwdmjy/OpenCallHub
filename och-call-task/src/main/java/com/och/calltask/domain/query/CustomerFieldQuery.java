package com.och.calltask.domain.query;

import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author danmo
 * @date 2025/06/27
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户字段配置查询参数")
@Data
public class CustomerFieldQuery extends BaseQuery {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "主键ID列表", hidden = true)
    private List<Long> idList;

    @Schema(description = "字段显示名称")
    private String fieldLabel;

    @Schema(description = "字段名称")
    private String fieldName;

    @Schema(description = "是否启用 0-禁用 1-启用")
    private Integer status;

    @Schema(description = "字段类型 0-电话 1-文本 2-数字 3-单选 4-多选 5-电子邮箱 6-日期 7-日期时间 8-时间")
    private Integer fieldType;
}
