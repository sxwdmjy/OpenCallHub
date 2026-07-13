package com.och.knowledge.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 话术模板 */
@Data @TableName("script_template")
public class ScriptTemplate {
    @TableId(type = IdType.AUTO) private Long id;
    private String scriptName;         // 话术名称
    private String scriptCode;         // 话术编码
    private String category;           // 分类
    private String content;            // 话术内容
    private String keywords;           // 关键词(逗号分隔)
    private Integer currentVersion;    // 当前版本号
    private Boolean published;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.UPDATE) private LocalDateTime updatedAt;
    @TableLogic private Integer deleted;
}
