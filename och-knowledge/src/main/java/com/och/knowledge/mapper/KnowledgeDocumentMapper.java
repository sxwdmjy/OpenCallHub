package com.och.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.knowledge.domain.KnowledgeDocument;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识文档 Mapper，基于 MyBatis-Plus BaseMapper。
 * <p>
 * 继承 BaseMapper 后自动拥有 CRUD 基础能力。
 * 全文检索等复杂查询通过 {@link com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper} 的 like 条件实现。
 * </p>
 *
 * @author danmo
 */
@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {
}
