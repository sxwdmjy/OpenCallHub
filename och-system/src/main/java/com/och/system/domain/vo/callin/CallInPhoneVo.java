package com.och.system.domain.vo.callin;

import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author danmo
 * @date 2024-11-09 18:23
 **/
@Schema
@Data
public class CallInPhoneVo extends BaseVo {

    /**
     * 主键
     */
    @Schema(description = "主键")
    private Long id;

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
     * 路由ID
     */
    @Schema(description = "路由名称")
    private String routeName;


    /**
     * 路由列表
     */
    @Schema(description = "路由列表")
    private List<CallInPhoneRelVo> routeList;
}
