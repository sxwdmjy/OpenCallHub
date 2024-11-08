package com.och.system.domain.vo.acl;

import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @date 2023年09月13日 14:48
 */
@Schema
@Data
public class FsAclNodeVo extends BaseVo {

    @Schema(description = "ID")
    private Long id;


    @Schema(description = "listId")
    private Long listId;

    /**
     * 规则类型 allow-允许 deny-拒绝
     */
    @Schema(description = "规则类型 allow-允许 deny-拒绝")
    private String nodeType;


    /**
     * IP地址
     */
    @Schema(description = "IP地址")
    private String cidr;


    /**
     * 域地址
     */
    @Schema(description = "域地址")
    private String domain;


}
