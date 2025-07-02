package com.och.calltask.domain.query;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @date 2025/06/27 09:07
 */
@Schema(description = "客户公海新增参数")
@Data
public class CustomerSeasAddQuery {

    @Schema(description = "主键ID", hidden = true)
    private Long id;


    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "第三方ID")
    @TableField("third_id")
    private String thirdId;


    @Schema(description = "客户数据")
    private String customerInfo;
}
