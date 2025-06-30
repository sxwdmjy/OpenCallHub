package com.och.calltask.domain.query;


import com.baomidou.mybatisplus.annotation.TableField;
import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 客户公海查询参数
 * @author danmo
 * @date 2025/6/30 15:37
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户公海查询参数")
@Data
public class CustomerSeasQuery extends BaseQuery {

    @Schema(description = "客户公海ID")
    private Long id;

    @Schema(description = "客户ID列表")
    private List<Long> idList;

    @Schema(description = "模板ID")
    private List<Long> templateIds;

    @Schema(description = "客户电话")
    private String phone;

    @Schema(description = "客户名称")
    private String name;

    @Schema(description = "客户性别 0-未知 1-男 2-女")
    private Integer sex;

    @Schema(description = "来源 0-手动创建 1-文件导入 2-API导入")
    private Integer source;
}
