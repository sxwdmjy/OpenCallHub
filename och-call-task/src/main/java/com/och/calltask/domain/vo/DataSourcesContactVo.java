package com.och.calltask.domain.vo;


import com.alibaba.fastjson2.JSONObject;
import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author danmo
 * @date 2025/06/16 17:47
 */
@EqualsAndHashCode(callSuper = true)
@Schema
@Data
public class DataSourcesContactVo extends BaseVo {

    /**
     * 主键ID
     */

    @Schema(description = "主键ID")
    private Long id;

    /**
     * 联系人数据
     */
    @Schema(description = "联系人数据")
    private JSONObject contact;
}
