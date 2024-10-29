package com.och.system.domain.query.skill;

import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author danmo
 * @date 2024-10-31 14:15
 **/
@EqualsAndHashCode(callSuper = true)
@Schema
@Data
public class CallSkillQuery extends BaseQuery {

    @Schema(description = "主键ID")
    private Long id;

    private List<Long> ids;

    /**
     * 分组ID
     */
    @Schema(description = "分组ID")
    private Long groupId;

    /**
     * 技能名称
     */
    @Schema(description = "技能名称")
    private String name;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String describe;

    @Schema(description = "坐席ID")
    private Long agentId;

    @Schema(description = "坐席名称")
    private String agentName;

}
