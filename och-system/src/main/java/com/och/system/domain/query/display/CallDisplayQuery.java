package com.och.system.domain.query.display;

import com.baomidou.mybatisplus.annotation.TableField;
import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author danmo
 * @date 2023-10-23 11:20
 **/
@Schema
@Data
public class CallDisplayQuery extends BaseQuery {

    /**
     * 主键
     */

    @Schema(description = "主键",hidden = true)
    private Long id;


    @Schema(description = "号码类型 1-主叫显号 2-被叫显号")
    private Integer type;

    /**
     *  电话号码
     */
    @Schema(description = "电话号码")
    private String phone;


    @Schema(description = "号码池ID列表")
    private List<Long> idList;

}
