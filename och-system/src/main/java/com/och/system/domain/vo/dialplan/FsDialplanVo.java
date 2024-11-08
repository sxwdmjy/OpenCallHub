package com.och.system.domain.vo.dialplan;

import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FsDialplanVo extends BaseVo {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;


    /**
     * 分组ID
     */
    @Schema(description = "分组ID")
    private Long groupId;


    /**
     * 计划名称
     */
    @Schema(description = "计划名称")
    private String name;

    /**
     * 内容类型 public、default
     */
    @Schema(description = "内容类型 public、default")
    private String contextName;

    /**
     * 类型 xml格式,json格式
     */
    @Schema(description = "类型 xml格式,json格式")
    private String type;


    /**
     * 内容
     */
    @Schema(description = "内容")
    private String content;


    /**
     * 描述
     */
    @Schema(description = "描述")
    private String describe;
}
