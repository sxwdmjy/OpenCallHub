package com.och.system.domain.vo.acl;

import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author danmo
 * @date 2023年09月13日 14:48
 */
@Schema
@Data
public class FsAclVo extends BaseVo {

    @Schema(description = "ID", hidden = true)
    private Long id;

    /**
     * 网关名称
     */
    @Schema(description = "名称")
    private String name;


    /**
     * 类型 allow-允许 deny-拒绝
     */
    @Schema(description = "类型 allow-允许 deny-拒绝")
    private String defaultType;


    @Schema(description = "规则列表")
    private List<FsAclNodeVo> nodeList;

}
