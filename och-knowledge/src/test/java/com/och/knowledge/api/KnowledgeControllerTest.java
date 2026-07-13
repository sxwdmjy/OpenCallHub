package com.och.knowledge.api;

import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.knowledge.service.impl.KnowledgeServiceImpl;
import com.och.knowledge.domain.KnowledgeDocument;
import com.och.knowledge.domain.ScriptTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * KnowledgeController 单元测试。
 * <p>
 * 使用 Mockito 模拟 KnowledgeService，验证各 REST 端点的调用链路和响应封装。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KnowledgeController 单元测试")
class KnowledgeControllerTest {

    @Mock
    private KnowledgeServiceImpl knowledgeService;

    @InjectMocks
    private KnowledgeController controller;

    private KnowledgeDocument sampleDoc;
    private ScriptTemplate sampleScript;

    @BeforeEach
    void setUp() {
        sampleDoc = new KnowledgeDocument();
        sampleDoc.setId(1L);
        sampleDoc.setTitle("话术指南");
        sampleDoc.setSummary("如何进行有效沟通");
        sampleDoc.setContent("<p>详细内容</p>");
        sampleDoc.setTags("沟通,话术");
        sampleDoc.setViewCount(100);
        sampleDoc.setUseCount(10);
        sampleDoc.setPublished(true);
        sampleDoc.setCreatedAt(LocalDateTime.now());
        sampleDoc.setUpdatedAt(LocalDateTime.now());

        sampleScript = new ScriptTemplate();
        sampleScript.setId(1L);
        sampleScript.setScriptName("欢迎话术");
        sampleScript.setScriptCode("SC-WLCM01");
        sampleScript.setCategory("问候");
        sampleScript.setContent("您好，欢迎致电！");
        sampleScript.setKeywords("欢迎,问候");
        sampleScript.setCurrentVersion(1);
        sampleScript.setPublished(true);
        sampleScript.setCreatedAt(LocalDateTime.now());
        sampleScript.setUpdatedAt(LocalDateTime.now());
    }

    // ── GET /api/knowledge/search ────────────────────────────

    @Test
    @DisplayName("search: 按关键字搜索返回知识文档列表")
    void search_shouldReturnDocumentsWrappedInApiResponse() {
        // given
        PageResponse<KnowledgeDocument> page = PageResponse.of(
                List.of(sampleDoc), 1L, 1, 50);
        when(knowledgeService.search("沟通")).thenReturn(page);

        // when
        ApiResponse<PageResponse<KnowledgeDocument>> response = controller.search("沟通");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getRecords()).hasSize(1);
        assertThat(response.getData().getRecords().get(0).getTitle()).isEqualTo("话术指南");
        assertThat(response.getData().getTotal()).isEqualTo(1L);

        verify(knowledgeService).search("沟通");
    }

    // ── GET /api/knowledge/documents ────────────────────────

    @Test
    @DisplayName("listDocuments: 分页查询返回文档列表")
    void listDocuments_shouldReturnPagedDocuments() {
        // given
        PageResponse<KnowledgeDocument> page = PageResponse.of(
                List.of(sampleDoc), 1L, 1, 20);
        when(knowledgeService.listDocuments(1, 20)).thenReturn(page);

        // when
        ApiResponse<PageResponse<KnowledgeDocument>> response = controller.listDocuments(1, 20);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getRecords()).hasSize(1);
        assertThat(response.getData().getPageNum()).isEqualTo(1);
        assertThat(response.getData().getPageSize()).isEqualTo(20);

        verify(knowledgeService).listDocuments(1, 20);
    }

    @Test
    @DisplayName("listDocuments: 空结果分页查询返回空列表")
    void listDocuments_shouldReturnEmptyPageWhenNoDocs() {
        // given
        PageResponse<KnowledgeDocument> page = PageResponse.of(
                Collections.emptyList(), 0L, 1, 20);
        when(knowledgeService.listDocuments(1, 20)).thenReturn(page);

        // when
        ApiResponse<PageResponse<KnowledgeDocument>> response = controller.listDocuments(1, 20);

        // then
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData().getRecords()).isEmpty();
        assertThat(response.getData().getTotal()).isEqualTo(0L);

        verify(knowledgeService).listDocuments(1, 20);
    }

    // ── POST /api/knowledge/documents ──────────────────────

    @Test
    @DisplayName("createDocument: 创建文档返回已持久化的文档实体")
    void createDocument_shouldReturnCreatedDocument() {
        // given
        KnowledgeDocument input = new KnowledgeDocument();
        input.setTitle("新文档");
        input.setContent("<p>内容</p>");

        KnowledgeDocument created = new KnowledgeDocument();
        created.setId(1L);
        created.setTitle("新文档");
        created.setContent("<p>内容</p>");
        created.setPublished(false);
        created.setViewCount(0);
        created.setUseCount(0);

        when(knowledgeService.createDocument(any(KnowledgeDocument.class))).thenReturn(created);

        // when
        ApiResponse<KnowledgeDocument> response = controller.createDocument(input);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getTitle()).isEqualTo("新文档");
        assertThat(response.getData().getPublished()).isFalse();
        assertThat(response.getData().getViewCount()).isEqualTo(0);

        verify(knowledgeService).createDocument(input);
    }

    // ── GET /api/knowledge/scripts ──────────────────────────

    @Test
    @DisplayName("listScripts: 按关键字查询话术模板列表")
    void listScripts_shouldReturnScriptsWithKeyword() {
        // given
        PageResponse<ScriptTemplate> page = PageResponse.of(
                List.of(sampleScript), 1L, 1, 50);
        when(knowledgeService.listScripts("欢迎")).thenReturn(page);

        // when
        ApiResponse<PageResponse<ScriptTemplate>> response = controller.listScripts("欢迎");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getRecords()).hasSize(1);
        assertThat(response.getData().getRecords().get(0).getScriptName()).isEqualTo("欢迎话术");
        assertThat(response.getData().getRecords().get(0).getScriptCode()).isEqualTo("SC-WLCM01");

        verify(knowledgeService).listScripts("欢迎");
    }

    @Test
    @DisplayName("listScripts: 无关键字时返回所有话术")
    void listScripts_shouldReturnAllScriptsWhenNoKeyword() {
        // given
        PageResponse<ScriptTemplate> page = PageResponse.of(
                List.of(sampleScript), 1L, 1, 50);
        when(knowledgeService.listScripts(null)).thenReturn(page);

        // when
        ApiResponse<PageResponse<ScriptTemplate>> response = controller.listScripts(null);

        // then
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData().getRecords()).hasSize(1);

        verify(knowledgeService).listScripts(null);
    }

    // ── POST /api/knowledge/scripts ────────────────────────

    @Test
    @DisplayName("createScript: 创建话术返回已生成编码的话术")
    void createScript_shouldReturnCreatedScriptWithCode() {
        // given
        ScriptTemplate input = new ScriptTemplate();
        input.setScriptName("投诉处理");
        input.setContent("非常抱歉给您带来不便");

        ScriptTemplate created = new ScriptTemplate();
        created.setId(1L);
        created.setScriptName("投诉处理");
        created.setScriptCode("SC-ABC123");
        created.setCurrentVersion(1);
        created.setPublished(false);

        when(knowledgeService.createScript(any(ScriptTemplate.class))).thenReturn(created);

        // when
        ApiResponse<ScriptTemplate> response = controller.createScript(input);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getScriptCode()).isEqualTo("SC-ABC123");
        assertThat(response.getData().getCurrentVersion()).isEqualTo(1);
        assertThat(response.getData().getPublished()).isFalse();

        verify(knowledgeService).createScript(input);
    }
}
