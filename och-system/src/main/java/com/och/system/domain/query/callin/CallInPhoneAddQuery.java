package com.och.system.domain.query.callin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author danmo
 * @date 2024-11-09 18:18
 **/
@Schema
@Data
public class CallInPhoneAddQuery {

    /**
     * 主键
     */
    @Schema(description = "主键",hidden = true)
    private Long id;

    /**
     * 名称
     */
    @Schema(description = "名称",requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 呼入号码
     */
    @Schema(description = "呼入号码",requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;


    /**
     * 前缀替换
     */
    @Schema(description = "前缀替换")
    private String prefix;


    /**
     * 后缀替换
     */
    @Schema(description = "后缀替换")
    private String suffix;

    /**
     * 路由列表
     */
    @Schema(description = "路由列表",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CallInPhoneRelQuery> routList;
}
