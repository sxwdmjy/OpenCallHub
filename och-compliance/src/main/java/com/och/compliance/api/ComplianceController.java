package com.och.compliance.api;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.compliance.service.IComplianceCheckService;
import com.och.compliance.service.IComplianceManageService;
import com.och.compliance.service.impl.ComplianceCheckServiceImpl;
import com.och.compliance.domain.BlacklistRecord;
import com.och.compliance.domain.DncRecord;
import com.och.security.audit.Audited;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 合规检查控制器。
 * 提供号码合规性校验接口，在外呼前调用以判断是否允许拨打。
 */
@Tag(name = "Compliance", description = "合规检查")
@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
public class ComplianceController {

    private final IComplianceCheckService complianceService;
    private final IComplianceManageService manageService;

    @Operation(summary = "检查号码是否合规")
    @GetMapping("/check")
    @Audited(action = "COMPLIANCE_CHECK", targetType = "COMPLIANCE")
    public ApiResponse<ComplianceCheckServiceImpl.ComplianceResult> check(@RequestParam String phoneNumber) {
        return ApiResponse.success(complianceService.check(phoneNumber));
    }

    // ======================== DNC 管理 ========================

    @Operation(summary = "DNC分页列表")
    @GetMapping("/dnc")
    public ApiResponse<PageResponse<DncRecord>> listDnc(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<DncRecord> page = manageService.pageDnc(pageNum, pageSize, keyword);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @Operation(summary = "新增DNC号码")
    @PostMapping("/dnc")
    @Audited(action = "ADD_DNC", targetType = "COMPLIANCE")
    public ApiResponse<DncRecord> addDnc(@RequestBody DncRecord record) {
        return ApiResponse.success(manageService.addDnc(
                record.getPhoneNumber(), record.getReason(), record.getSource(), record.getExpireAt()));
    }

    @Operation(summary = "删除DNC号码")
    @DeleteMapping("/dnc/{id}")
    @Audited(action = "REMOVE_DNC", targetType = "COMPLIANCE")
    public ApiResponse<Void> removeDnc(@PathVariable Long id) {
        manageService.removeDnc(id);
        return ApiResponse.success();
    }

    @Operation(summary = "批量添加DNC号码")
    @PostMapping("/dnc/batch")
    @Audited(action = "BATCH_ADD_DNC", targetType = "COMPLIANCE")
    public ApiResponse<Integer> batchAddDnc(@RequestBody List<DncRecord> records) {
        return ApiResponse.success(manageService.batchAddDnc(records));
    }

    // ======================== 黑名单管理 ========================

    @Operation(summary = "黑名单分页列表")
    @GetMapping("/blacklist")
    public ApiResponse<PageResponse<BlacklistRecord>> listBlacklist(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<BlacklistRecord> page = manageService.pageBlacklist(pageNum, pageSize, keyword);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @Operation(summary = "新增黑名单号码")
    @PostMapping("/blacklist")
    @Audited(action = "ADD_BLACKLIST", targetType = "COMPLIANCE")
    public ApiResponse<BlacklistRecord> addBlacklist(@RequestBody BlacklistRecord record) {
        return ApiResponse.success(manageService.addBlacklist(
                record.getPhoneNumber(), record.getReason(), record.getLevel()));
    }

    @Operation(summary = "删除黑名单号码")
    @DeleteMapping("/blacklist/{id}")
    @Audited(action = "REMOVE_BLACKLIST", targetType = "COMPLIANCE")
    public ApiResponse<Void> removeBlacklist(@PathVariable Long id) {
        manageService.removeBlacklist(id);
        return ApiResponse.success();
    }

    @Operation(summary = "批量添加黑名单号码")
    @PostMapping("/blacklist/batch")
    @Audited(action = "BATCH_ADD_BLACKLIST", targetType = "COMPLIANCE")
    public ApiResponse<Integer> batchAddBlacklist(@RequestBody List<BlacklistRecord> records) {
        return ApiResponse.success(manageService.batchAddBlacklist(records));
    }

    @PreAuthorize("@authz.hasPerm('compliance:dnc:import')")
    @Operation(summary = "CSV导入DNC号码")
    @PostMapping("/dnc/import-csv")
    @Audited(action = "IMPORT_DNC_CSV", targetType = "COMPLIANCE")
    public ApiResponse<Integer> importDncCsv(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                                              @RequestParam(defaultValue = "CSV") String source) {
        try {
            return ApiResponse.success(manageService.importDncCsv(file.getInputStream(), source));
        } catch (java.io.IOException e) {
            return ApiResponse.error(500, "文件读取失败: " + e.getMessage());
        }
    }

    @PreAuthorize("@authz.hasPerm('compliance:blacklist:import')")
    @Operation(summary = "CSV导入黑名单")
    @PostMapping("/blacklist/import-csv")
    @Audited(action = "IMPORT_BLACKLIST_CSV", targetType = "COMPLIANCE")
    public ApiResponse<Integer> importBlacklistCsv(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            return ApiResponse.success(manageService.importBlacklistCsv(file.getInputStream()));
        } catch (java.io.IOException e) {
            return ApiResponse.error(500, "文件读取失败: " + e.getMessage());
        }
    }
}
