package com.och.system.domain.query.display;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @date 2023-10-23 11:20
 **/
@Schema
@Data
public class CallDisplayAddQuery {

    /**
     * 主键
     */
    @Schema(description = "主键",hidden = true)
    private Long id;


    /**
     *  电话号码
     */
    @Schema(description = "电话号码",requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    /**
     * 号码类型 1-主叫显号 2-被叫显号
     */
    @Schema(description = "号码类型 1-主叫显号 2-被叫显号",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;
}
