package com.och.system.domain.query.callin;

import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author danmo
 * @date 2024-11-09 18:18
 **/
@Schema
@Data
public class CallInPhoneQuery extends BaseQuery {

    /**
     * 主键
     */
    @Schema(description = "主键")
    private Long id;

    private List<Long> ids;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;
    /**
     * 呼入号码
     */
    @Schema(description = "呼入号码")
    private String phone;


    /**
     * 路由ID
     */
    @Schema(description = "路由ID")
    private Long routeId;


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
}
