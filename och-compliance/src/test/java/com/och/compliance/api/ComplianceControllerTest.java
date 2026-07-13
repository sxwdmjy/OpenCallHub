package com.och.compliance.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.compliance.service.IComplianceCheckService;
import com.och.compliance.service.impl.ComplianceCheckServiceImpl.ComplianceResult;
import com.och.compliance.service.IComplianceManageService;
import com.och.compliance.domain.BlacklistRecord;
import com.och.compliance.domain.DncRecord;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ComplianceController 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ComplianceController 单元测试")
class ComplianceControllerTest {

    @Mock
    private IComplianceCheckService complianceService;

    @Mock
    private IComplianceManageService manageService;

    @InjectMocks
    private ComplianceController controller;

    private MockMvc mockMvc;

    private static final String PHONE = "13900139000";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    @DisplayName("GET /api/compliance/check: 号码在黑名单 -> 返回 blocked=true, filterType=BLACKLIST")
    void checkShouldReturnBlacklistBlocked() throws Exception {
        // given
        ComplianceResult blockedResult = ComplianceResult.blacklist("号码 " + PHONE + " 在黑名单中");
        when(complianceService.check(eq(PHONE))).thenReturn(blockedResult);

        // when / then
        mockMvc.perform(get("/api/compliance/check")
                        .param("phoneNumber", PHONE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.blocked").value(true))
                .andExpect(jsonPath("$.data.filterType").value("BLACKLIST"))
                .andExpect(jsonPath("$.data.reason").value("号码 " + PHONE + " 在黑名单中"));
    }

    @Test
    @DisplayName("GET /api/compliance/check: 号码在 DNC -> 返回 blocked=true, filterType=DNC")
    void checkShouldReturnDncBlocked() throws Exception {
        // given
        ComplianceResult dncResult = ComplianceResult.dnc("号码 " + PHONE + " 在DNC列表中");
        when(complianceService.check(eq(PHONE))).thenReturn(dncResult);

        // when / then
        mockMvc.perform(get("/api/compliance/check")
                        .param("phoneNumber", PHONE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.blocked").value(true))
                .andExpect(jsonPath("$.data.filterType").value("DNC"));
    }

    @Test
    @DisplayName("GET /api/compliance/check: 号码合规 -> 返回 blocked=false")
    void checkShouldReturnPass() throws Exception {
        // given
        ComplianceResult passResult = ComplianceResult.pass();
        when(complianceService.check(eq(PHONE))).thenReturn(passResult);

        // when / then
        mockMvc.perform(get("/api/compliance/check")
                        .param("phoneNumber", PHONE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.blocked").value(false))
                .andExpect(jsonPath("$.data.filterType").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/compliance/check: 空号码 -> 返回 pass")
    void checkShouldPassForEmptyPhoneNumber() throws Exception {
        // given
        when(complianceService.check(any())).thenReturn(ComplianceResult.pass());

        // when / then
        mockMvc.perform(get("/api/compliance/check")
                        .param("phoneNumber", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.blocked").value(false));
    }

    @Test
    @DisplayName("GET /api/compliance/check: 缺少 phoneNumber 参数 -> 400 错误")
    void checkShouldReturnBadRequestWhenMissingParameter() throws Exception {
        // given - 不传 phoneNumber 参数

        // when / then - Spring MVC 默认对缺失必填参数返回 400
        mockMvc.perform(get("/api/compliance/check"))
                .andExpect(status().isBadRequest());
    }

    // ======================== DNC CRUD ========================

    @Test
    @DisplayName("GET /api/compliance/dnc: 分页查询DNC列表")
    void listDnc_shouldReturnPage() throws Exception {
        IPage<DncRecord> page = new Page<>(1, 20);
        page.setRecords(Collections.emptyList());
        page.setTotal(0L);
        when(manageService.pageDnc(1, 20, null)).thenReturn(page);

        mockMvc.perform(get("/api/compliance/dnc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    @DisplayName("POST /api/compliance/dnc: 新增DNC号码")
    void addDnc_shouldReturnCreatedRecord() throws Exception {
        DncRecord record = new DncRecord();
        record.setPhoneNumber("13800138000");
        record.setReason("投诉");
        when(manageService.addDnc(eq("13800138000"), anyString(), any(), any()))
                .thenReturn(record);

        mockMvc.perform(post("/api/compliance/dnc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"13800138000\",\"reason\":\"投诉\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.phoneNumber").value("13800138000"));
    }

    @Test
    @DisplayName("DELETE /api/compliance/dnc/{id}: 删除DNC号码")
    void removeDnc_shouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/compliance/dnc/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ======================== Blacklist CRUD ========================

    @Test
    @DisplayName("GET /api/compliance/blacklist: 分页查询黑名单")
    void listBlacklist_shouldReturnPage() throws Exception {
        IPage<BlacklistRecord> page = new Page<>(1, 20);
        page.setRecords(Collections.emptyList());
        page.setTotal(0L);
        when(manageService.pageBlacklist(1, 20, null)).thenReturn(page);

        mockMvc.perform(get("/api/compliance/blacklist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /api/compliance/blacklist: 新增黑名单")
    void addBlacklist_shouldReturnCreatedRecord() throws Exception {
        BlacklistRecord record = new BlacklistRecord();
        record.setPhoneNumber("13900000000");
        record.setLevel("HIGH");
        when(manageService.addBlacklist(eq("13900000000"), anyString(), any()))
                .thenReturn(record);

        mockMvc.perform(post("/api/compliance/blacklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"13900000000\",\"reason\":\"诈骗\",\"level\":\"HIGH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.phoneNumber").value("13900000000"));
    }

    @Test
    @DisplayName("DELETE /api/compliance/blacklist/{id}: 删除黑名单")
    void removeBlacklist_shouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/compliance/blacklist/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ======================== Batch ========================

    @Test
    @DisplayName("POST /api/compliance/dnc/batch: 批量添加DNC")
    void batchAddDnc_shouldReturnCount() throws Exception {
        when(manageService.batchAddDnc(any())).thenReturn(3);

        mockMvc.perform(post("/api/compliance/dnc/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"phoneNumber\":\"13800138001\"},{\"phoneNumber\":\"13800138002\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(3));
    }

    @Test
    @DisplayName("POST /api/compliance/blacklist/batch: 批量添加黑名单")
    void batchAddBlacklist_shouldReturnCount() throws Exception {
        when(manageService.batchAddBlacklist(any())).thenReturn(5);

        mockMvc.perform(post("/api/compliance/blacklist/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"phoneNumber\":\"13900000001\"},{\"phoneNumber\":\"13900000002\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));
    }
}
