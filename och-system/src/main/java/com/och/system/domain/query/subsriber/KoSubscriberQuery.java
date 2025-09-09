package com.och.system.domain.query.subsriber;

import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @date 2024年07月25日 13:58
 */
@Schema
@Data
public class KoSubscriberQuery extends BaseQuery {

    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "SIP号码")
    private String username;

    @Schema(description = "SIP密码")
    private String password;

    @Schema(description = "状态 0-开启 1-关闭")
    private Integer status;
}
