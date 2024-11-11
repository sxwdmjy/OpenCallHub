package com.och.system.domain.vo.sip;

import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubscriberVo extends BaseVo {

    @Schema(description = "id")
    private String id;


    @Schema(description = "Sip名称")
    private String userName;


    @Schema(description = "来源")
    private String domain;


    @Schema(description = "密码")
    private String password;


    @Schema(description = "ha1")
    private String ha1;


    @Schema(description = "ha1b")
    private String ha1b;


    @Schema(description = "vmpin")
    private String vmpin;


    @Schema(description = "状态 0-开启 1-关闭")
    private Integer status;
}
