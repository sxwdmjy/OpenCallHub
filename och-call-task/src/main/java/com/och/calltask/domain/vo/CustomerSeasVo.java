package com.och.calltask.domain.vo;


import com.alibaba.fastjson2.JSONObject;
import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户公海VO
 * @author danmo
 * @date 2025/6/30 15:39
 */

@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户公海VO")
@Data
public class CustomerSeasVo extends BaseVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "第三方ID")
    private String thirdId;

    @Schema(description = "客户数据")
    private JSONObject customerInfo;

    @Schema(description = "客户电话")
    private String phone;

    @Schema(description = "客户名称")
    private String name;

    @Schema(description = "虚列客户性别 0-未知 1-男 2-女")
    private Integer sex;

    @Schema(description = "来源 0-手动创建 1-文件导入 2-API导入")
    private Integer source;
}
