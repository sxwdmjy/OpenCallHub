package com.och.knowledge.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.common.api.PageResponse;
import com.och.knowledge.domain.KnowledgeDocument;
import com.och.knowledge.domain.ScriptTemplate;
import com.och.knowledge.mapper.KnowledgeDocumentMapper;
import com.och.knowledge.service.impl.KnowledgeServiceImpl;
import com.och.knowledge.mapper.ScriptTemplateMapper;
import com.och.knowledge.service.impl.KnowledgeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * KnowledgeServiceImpl 单元测试。
 * <p>
 * 覆盖知识文档搜索、分页列表、创建，以及话术模板列表查询与创建的正常流程。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KnowledgeServiceImpl 单元测试")
class KnowledgeServiceTest {

    @Mock
    private KnowledgeDocumentMapper documentMapper;

    @Mock
    private ScriptTemplateMapper scriptMapper;

    @InjectMocks
    private KnowledgeServiceImpl knowledgeService;

    // ── 知识文档：搜索 ───────────────────────────────────────

    @Test
    @DisplayName("search: 按关键字搜索已发布文档，返回匹配结果")
    void search_shouldReturnMatchedDocuments() {
        // given
        KnowledgeDocument doc = buildDocument(1L, "话术指南", "如何进行有效沟通", "沟通", 100, true);
        Page<KnowledgeDocument> mockPage = new Page<>(1, 50);
        mockPage.setRecords(List.of(doc));
        mockPage.setTotal(1L);
        when(documentMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        // when
        PageResponse<KnowledgeDocument> response = knowledgeService.search("沟通");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecords()).hasSize(1);
        assertThat(response.getRecords().get(0).getTitle()).isEqualTo("话术指南");
        assertThat(response.getTotal()).isEqualTo(1L);
        assertThat(response.getPageNum()).isEqualTo(1);
        assertThat(response.getPageSize()).isEqualTo(50);

        verify(documentMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("search: 关键字为空时返回所有已发布文档")
    void search_shouldReturnAllPublishedWhenKeywordIsEmpty() {
        // given
        KnowledgeDocument doc1 = buildDocument(1L, "文档A", "摘要A", "内容", 10, true);
        KnowledgeDocument doc2 = buildDocument(2L, "文档B", "摘要B", "内容", 20, true);
        Page<KnowledgeDocument> mockPage = new Page<>(1, 50);
        mockPage.setRecords(List.of(doc1, doc2));
        mockPage.setTotal(2L);
        when(documentMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        // when
        PageResponse<KnowledgeDocument> response = knowledgeService.search(null);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecords()).hasSize(2);
        assertThat(response.getTotal()).isEqualTo(2L);

        verify(documentMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    // ── 知识文档：分页列表 ──────────────────────────────────

    @Test
    @DisplayName("listDocuments: 分页查询已发布文档，按更新时间降序")
    void listDocuments_shouldReturnPageResponse() {
        // given
        KnowledgeDocument doc = buildDocument(1L, "产品手册", "使用说明", "详细内容", 5, true);
        Page<KnowledgeDocument> mockPage = new Page<>(1, 20);
        mockPage.setRecords(List.of(doc));
        mockPage.setTotal(1L);
        when(documentMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        // when
        PageResponse<KnowledgeDocument> response = knowledgeService.listDocuments(1, 20);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecords()).hasSize(1);
        assertThat(response.getTotal()).isEqualTo(1L);
        assertThat(response.getPageNum()).isEqualTo(1);
        assertThat(response.getPageSize()).isEqualTo(20);

        verify(documentMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("listDocuments: 无已发布文档时返回空列表")
    void listDocuments_shouldReturnEmptyWhenNoPublishedDocs() {
        // given
        Page<KnowledgeDocument> mockPage = new Page<>(1, 20);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0L);
        when(documentMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        // when
        PageResponse<KnowledgeDocument> response = knowledgeService.listDocuments(1, 20);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecords()).isEmpty();
        assertThat(response.getTotal()).isEqualTo(0L);

        verify(documentMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    // ── 知识文档：创建 ──────────────────────────────────────

    @Test
    @DisplayName("createDocument: 创建文档时自动设置默认值并入库")
    void createDocument_shouldSetDefaultsAndInsert() {
        // given
        KnowledgeDocument input = new KnowledgeDocument();
        input.setTitle("新文档");
        input.setContent("<p>内容</p>");
        // published/viewCount/useCount 均未设置

        when(documentMapper.insert(any(KnowledgeDocument.class))).thenReturn(1);

        // when
        KnowledgeDocument result = knowledgeService.createDocument(input);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("新文档");
        assertThat(result.getPublished()).isFalse();
        assertThat(result.getViewCount()).isEqualTo(0);
        assertThat(result.getUseCount()).isEqualTo(0);

        ArgumentCaptor<KnowledgeDocument> captor = ArgumentCaptor.forClass(KnowledgeDocument.class);
        verify(documentMapper).insert(captor.capture());
        KnowledgeDocument inserted = captor.getValue();
        assertThat(inserted.getPublished()).isFalse();
        assertThat(inserted.getViewCount()).isEqualTo(0);
        assertThat(inserted.getUseCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("createDocument: 创建时已有默认值则保留已有值")
    void createDocument_shouldPreserveExistingDefaults() {
        // given
        KnowledgeDocument input = new KnowledgeDocument();
        input.setTitle("已预设文档");
        input.setPublished(true);
        input.setViewCount(100);
        input.setUseCount(50);

        when(documentMapper.insert(any(KnowledgeDocument.class))).thenReturn(1);

        // when
        KnowledgeDocument result = knowledgeService.createDocument(input);

        // then
        assertThat(result.getPublished()).isTrue();
        assertThat(result.getViewCount()).isEqualTo(100);
        assertThat(result.getUseCount()).isEqualTo(50);

        ArgumentCaptor<KnowledgeDocument> captor = ArgumentCaptor.forClass(KnowledgeDocument.class);
        verify(documentMapper).insert(captor.capture());
        assertThat(captor.getValue().getPublished()).isTrue();
    }

    // ── 话术模板：列表查询 ─────────────────────────────────

    @Test
    @DisplayName("listScripts: 按关键字筛选已发布话术")
    void listScripts_shouldReturnFilteredScripts() {
        // given
        ScriptTemplate script = buildScript(1L, "欢迎话术", "SC-WELCOME", "欢迎致电", "欢迎,问候", true, 1);
        Page<ScriptTemplate> mockPage = new Page<>(1, 50);
        mockPage.setRecords(List.of(script));
        mockPage.setTotal(1L);
        when(scriptMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        // when
        PageResponse<ScriptTemplate> response = knowledgeService.listScripts("欢迎");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecords()).hasSize(1);
        assertThat(response.getRecords().get(0).getScriptName()).isEqualTo("欢迎话术");
        assertThat(response.getTotal()).isEqualTo(1L);

        verify(scriptMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("listScripts: 关键字为空时返回所有已发布话术")
    void listScripts_shouldReturnAllPublishedWhenKeywordIsNull() {
        // given
        ScriptTemplate s1 = buildScript(1L, "问候", "SC-A001", "您好", "问候", true, 1);
        ScriptTemplate s2 = buildScript(2L, "结束", "SC-A002", "再见", "结束", true, 1);
        Page<ScriptTemplate> mockPage = new Page<>(1, 50);
        mockPage.setRecords(List.of(s1, s2));
        mockPage.setTotal(2L);
        when(scriptMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        // when
        PageResponse<ScriptTemplate> response = knowledgeService.listScripts(null);

        // then
        assertThat(response.getRecords()).hasSize(2);
        assertThat(response.getTotal()).isEqualTo(2L);

        verify(scriptMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    // ── 话术模板：创建 ──────────────────────────────────────

    @Test
    @DisplayName("createScript: 创建话术时自动生成编码和版本号")
    void createScript_shouldGenerateCodeAndVersion() {
        // given
        ScriptTemplate input = new ScriptTemplate();
        input.setScriptName("投诉处理话术");
        input.setContent("非常抱歉给您带来不便");

        when(scriptMapper.insert(any(ScriptTemplate.class))).thenReturn(1);

        // when
        ScriptTemplate result = knowledgeService.createScript(input);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getScriptName()).isEqualTo("投诉处理话术");
        assertThat(result.getScriptCode()).isNotNull();
        assertThat(result.getScriptCode()).startsWith("SC-");
        assertThat(result.getScriptCode()).hasSize(9); // "SC-" + 6 uppercase
        assertThat(result.getCurrentVersion()).isEqualTo(1);
        assertThat(result.getPublished()).isFalse();

        ArgumentCaptor<ScriptTemplate> captor = ArgumentCaptor.forClass(ScriptTemplate.class);
        verify(scriptMapper).insert(captor.capture());
        ScriptTemplate inserted = captor.getValue();
        assertThat(inserted.getScriptCode()).startsWith("SC-");
        assertThat(inserted.getCurrentVersion()).isEqualTo(1);
        assertThat(inserted.getPublished()).isFalse();
    }

    // ── 辅助方法 ───────────────────────────────────────────

    private static KnowledgeDocument buildDocument(Long id, String title, String summary,
                                                    String content, int viewCount, boolean published) {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(id);
        doc.setTitle(title);
        doc.setSummary(summary);
        doc.setContent(content);
        doc.setTags("标签A,标签B");
        doc.setViewCount(viewCount);
        doc.setUseCount(0);
        doc.setPublished(published);
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());
        return doc;
    }

    private static ScriptTemplate buildScript(Long id, String name, String code,
                                               String content, String keywords,
                                               boolean published, int version) {
        ScriptTemplate script = new ScriptTemplate();
        script.setId(id);
        script.setScriptName(name);
        script.setScriptCode(code);
        script.setContent(content);
        script.setKeywords(keywords);
        script.setCurrentVersion(version);
        script.setPublished(published);
        script.setCreatedAt(LocalDateTime.now());
        script.setUpdatedAt(LocalDateTime.now());
        return script;
    }
}
