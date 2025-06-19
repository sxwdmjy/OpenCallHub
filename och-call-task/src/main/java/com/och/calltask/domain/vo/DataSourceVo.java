package com.och.calltask.domain.vo;


import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author danmo
 * @date 2025/06/16 16:16
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "数据源出参")
@Data
public class DataSourceVo extends BaseVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "数据源名称")
    private String name;

    @Schema(description = "数据源备注")
    private String remark;

    @Schema(description = "字段列表")
    private List<FieldInfoVo> fieldList;

    @Schema(description = "联系人数量")
    private Long contactNum;

    @Schema(description = "字段数量")
    private Integer fieldNum;

}
