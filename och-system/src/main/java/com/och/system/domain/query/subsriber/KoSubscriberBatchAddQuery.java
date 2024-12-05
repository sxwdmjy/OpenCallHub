package com.och.system.domain.query.subsriber;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author danmo
 * @date 2023年09月25日 13:58
 */
@Schema
@Data
public class KoSubscriberBatchAddQuery {

    @Schema(description = "初始值",example = "0000")
    private Integer initNum;

    @Schema(description = "SIP号码长度",requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 8,max = 12,message = "长度最少8位,最大12位")
    private Integer size;

    @Schema(description = "生成号码个数",requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1,max = 10,message = "个数最少1个,最多10个")
    private Integer number;

    @Schema(description = "SIP密码",requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
