package com.och.quality.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.och.quality.service.impl.QualityServiceImpl;
import com.och.quality.domain.QualityAppeal;
import com.och.quality.domain.QualityScore;
import com.och.quality.domain.QualityTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * QualityController 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QualityController 单元测试")
class QualityControllerTest {

    @Mock
    private QualityServiceImpl qualityService;

    @InjectMocks
    private QualityController controller;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    // ==================== 质检模板 ====================

    @Test
    @DisplayName("GET /api/quality/templates: 返回模板列表 -> code=200")
    void listTemplatesShouldReturnSuccess() throws Exception {
        // given
        QualityTemplate t1 = new QualityTemplate();
        t1.setId(1L);
        t1.setTemplateName("服务规范模板");
        t1.setTemplateCode("QT-001");
        t1.setPublished(true);

        when(qualityService.listTemplates()).thenReturn(Arrays.asList(t1));

        // when / then
        mockMvc.perform(get("/api/quality/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].templateName").value("服务规范模板"))
                .andExpect(jsonPath("$.data[0].templateCode").value("QT-001"));
    }

    @Test
    @DisplayName("POST /api/quality/templates: 创建模板成功 -> code=200")
    void createTemplateShouldReturnSuccess() throws Exception {
        // given
        QualityTemplate input = new QualityTemplate();
        input.setTemplateName("新模板");
        input.setTotalScore(100);
        input.setPassScore(60);

        QualityTemplate saved = new QualityTemplate();
        saved.setId(1L);
        saved.setTemplateName("新模板");
        saved.setTemplateCode("QT-ABCDEF");
        saved.setPublished(false);

        when(qualityService.createTemplate(any(QualityTemplate.class))).thenReturn(saved);

        // when / then
        mockMvc.perform(post("/api/quality/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.templateCode").value("QT-ABCDEF"))
                .andExpect(jsonPath("$.data.templateName").value("新模板"));
    }

    @Test
    @DisplayName("GET /api/quality/templates: 空模板列表 -> data 为空数组")
    void listTemplatesShouldReturnEmptyArrayWhenNoTemplates() throws Exception {
        // given
        when(qualityService.listTemplates()).thenReturn(Collections.emptyList());

        // when / then
        mockMvc.perform(get("/api/quality/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ==================== 质检评分 ====================

    @Test
    @DisplayName("POST /api/quality/scores: 提交评分成功 -> code=200，返回 passScore")
    void submitScoreShouldReturnSuccess() throws Exception {
        // given
        QualityScore input = new QualityScore();
        input.setTemplateId(1L);
        input.setTotalScore(85);
        input.setScorerId(100L);
        input.setType("MANUAL");

        QualityScore saved = new QualityScore();
        saved.setId(10L);
        saved.setTemplateId(1L);
        saved.setTotalScore(85);
        saved.setPassScore(1);
        saved.setType("MANUAL");

        when(qualityService.submitScore(any(QualityScore.class))).thenReturn(saved);

        // when / then
        mockMvc.perform(post("/api/quality/scores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.passScore").value(1))
                .andExpect(jsonPath("$.data.totalScore").value(85));
    }

    // ==================== 质检申诉 ====================

    @Test
    @DisplayName("POST /api/quality/appeals: 提交申诉成功 -> code=200，状态为 PENDING")
    void submitAppealShouldReturnSuccess() throws Exception {
        // given
        QualityAppeal input = new QualityAppeal();
        input.setScoreId(100L);
        input.setAppellantId(200L);
        input.setReason("评分不合理");

        QualityAppeal saved = new QualityAppeal();
        saved.setId(1L);
        saved.setScoreId(100L);
        saved.setAppellantId(200L);
        saved.setReason("评分不合理");
        saved.setState("PENDING");

        when(qualityService.submitAppeal(any(QualityAppeal.class))).thenReturn(saved);

        // when / then
        mockMvc.perform(post("/api/quality/appeals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.state").value("PENDING"));
    }

    @Test
    @DisplayName("PUT /api/quality/appeals/{id}/review: 审核申诉 -> code=200")
    void reviewAppealShouldReturnSuccess() throws Exception {
        // given
        doNothing().when(qualityService).reviewAppeal(eq(1L), eq(true), eq("同意申诉"));

        // when / then
        mockMvc.perform(put("/api/quality/appeals/1/review")
                        .param("approved", "true")
                        .param("comment", "同意申诉"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
