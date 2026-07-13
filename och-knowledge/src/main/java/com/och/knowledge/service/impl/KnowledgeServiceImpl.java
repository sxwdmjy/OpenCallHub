package com.och.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.common.api.PageResponse;
import com.och.knowledge.domain.KnowledgeDocument;
import com.och.knowledge.domain.ScriptTemplate;
import com.och.knowledge.mapper.KnowledgeDocumentMapper;
import com.och.knowledge.mapper.ScriptTemplateMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 知识库管理服务。
 * <p>
 * 提供知识文档和话术模板的检索、创建与管理功能。
 * 支持按关键字全文检索知识文档（匹配标题、摘要、内容、标签），
 * 以及话术模板的关键词筛选和版本管理。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.knowledge.mapper.KnowledgeDocumentMapper, com.och.knowledge.domain.KnowledgeDocument> implements com.och.knowledge.service.IKnowledgeService {

    private final KnowledgeDocumentMapper documentMapper;
    private final ScriptTemplateMapper scriptMapper;

    // ==================== 知识文档 ====================

    /**
     * 按关键字搜索知识文档。
     * <p>
     * 使用 LIKE 查询匹配标题、摘要、内容和标签字段，仅返回已发布文档。
     * 结果按浏览次数降序排列，让高频使用的文档优先展示。
     * </p>
     *
     * @param keyword 搜索关键字
     * @return 匹配的知识文档列表
     */
    public PageResponse<KnowledgeDocument> search(String keyword) {
        log.info("搜索知识库：keyword={}", keyword);
        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDocument::getPublished, true);
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(KnowledgeDocument::getTitle, kw)
                    .or().like(KnowledgeDocument::getSummary, kw)
                    .or().like(KnowledgeDocument::getContent, kw)
                    .or().like(KnowledgeDocument::getTags, kw));
        }
        wrapper.orderByDesc(KnowledgeDocument::getViewCount);
        IPage<KnowledgeDocument> page = documentMapper.selectPage(new Page<>(1, 50), wrapper);
        return PageResponse.of(page.getRecords(), page.getTotal(), 1, 50);
    }

    /**
     * 分页查询知识文档列表。
     * <p>仅返回已发布的文档，按更新时间降序排列。</p>
     *
     * @param pageNum  页码
     * @param pageSize 每页记录数
     * @return 分页响应
     */
    public PageResponse<KnowledgeDocument> listDocuments(int pageNum, int pageSize) {
        log.info("查询知识文档列表：pageNum={}, pageSize={}", pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDocument::getPublished, true)
               .orderByDesc(KnowledgeDocument::getUpdatedAt);
        IPage<KnowledgeDocument> page = documentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    /**
     * 创建知识文档。
     * <p>新文档默认未发布状态，需手动发布后才可被检索。</p>
     *
     * @param doc 知识文档实体
     * @return 持久化后的文档实体
     */
    @Transactional
    public KnowledgeDocument createDocument(KnowledgeDocument doc) {
        if (doc.getPublished() == null) {
            doc.setPublished(false);
        }
        if (doc.getViewCount() == null) {
            doc.setViewCount(0);
        }
        if (doc.getUseCount() == null) {
            doc.setUseCount(0);
        }
        documentMapper.insert(doc);
        log.info("知识文档创建成功：id={}, title={}", doc.getId(), doc.getTitle());
        return doc;
    }

    // ==================== 话术模板 ====================

    /**
     * 按关键字查询话术模板列表。
     * <p>
     * 匹配话术名称和关键词字段，支持模糊搜索。仅返回已发布的话术。
     * </p>
     *
     * @param keyword 搜索关键字，为空则返回所有已发布话术
     * @return 匹配的话术模板列表
     */
    public PageResponse<ScriptTemplate> listScripts(String keyword) {
        log.info("查询话术列表：keyword={}", keyword);
        LambdaQueryWrapper<ScriptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScriptTemplate::getPublished, true);
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(ScriptTemplate::getScriptName, kw)
                    .or().like(ScriptTemplate::getKeywords, kw));
        }
        wrapper.orderByDesc(ScriptTemplate::getUpdatedAt);
        IPage<ScriptTemplate> page = scriptMapper.selectPage(new Page<>(1, 50), wrapper);
        return PageResponse.of(page.getRecords(), page.getTotal(), 1, 50);
    }

    /**
     * 创建话术模板。
     * <p>
     * 自动生成唯一话术编码（SC + UUID 前 6 位），初始版本号设为 1。
     * 默认未发布状态。
     * </p>
     *
     * @param script 话术模板实体
     * @return 持久化后的话术模板
     */
    @Transactional
    public ScriptTemplate createScript(ScriptTemplate script) {
        script.setScriptCode("SC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        script.setCurrentVersion(1);
        if (script.getPublished() == null) {
            script.setPublished(false);
        }
        scriptMapper.insert(script);
        log.info("话术模板创建成功：id={}, scriptCode={}, scriptName={}",
                script.getId(), script.getScriptCode(), script.getScriptName());
        return script;
    }
}
