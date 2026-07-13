package com.och.quality.api;

import lombok.RequiredArgsConstructor;

import com.och.common.api.ApiResponse;
import com.och.quality.service.IQualityService;
import com.och.quality.domain.QualityAppeal;
import com.och.quality.domain.QualityScore;
import com.och.quality.domain.QualityTemplate;
import com.och.security.audit.Audited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 质检中心控制器。
 * <p>
 * 提供质检模板管理、人工/AI 评分提交、申诉复核等 REST API。
 * 评分自动根据模板规则判断及格状态；申诉经质检员复核后可更新原评分。
 * </p>
 *
 * @author danmo
 */
@Tag(name = "Quality", description = "质检中心")
@RestController
@RequestMapping("/api/quality")
@RequiredArgsConstructor
public class QualityController {

    private final IQualityService qualityService;

    // ==================== 质检模板 ====================

    /**
     * 查询所有已发布的质检模板列表。
     *
     * @return 已发布的质检模板列表
     */
    @Operation(summary = "质检模板列表")
    @GetMapping("/templates")
    public ApiResponse<List<QualityTemplate>> listTemplates() {
        return ApiResponse.success(qualityService.listTemplates());
    }

    /**
     * 创建质检模板。
     * <p>自动生成模板编码，默认未发布，需手动发布后方可使用。</p>
     *
     * @param template 质检模板实体
     * @return 创建成功的模板
     */
    @PostMapping("/templates")
    @Audited(action = "CREATE", targetType = "QUALITY_TEMPLATE", description = "创建质检模板")
    public ApiResponse<QualityTemplate> createTemplate(@RequestBody QualityTemplate template) {
        return ApiResponse.success(qualityService.createTemplate(template));
    }

    // ==================== 质检评分 ====================

    /**
     * 提交质检评分（人工或 AI）。
     * <p>根据关联模板的 passScore 自动判定是否及格。</p>
     *
     * @param score 质检评分实体
     * @return 持久化后的评分记录
     */
    @Operation(summary = "提交评分")
    @PostMapping("/scores")
    @Audited(action = "SCORE", targetType = "QUALITY", description = "质检评分")
    public ApiResponse<QualityScore> submitScore(@RequestBody QualityScore score) {
        return ApiResponse.success(qualityService.submitScore(score));
    }

    // ==================== 质检申诉 ====================

    /**
     * 提交质检申诉。
     * <p>坐席对评分结果有异议时可提交申诉，初始状态为 PENDING，等待质检员复核。</p>
     *
     * @param appeal 申诉实体（需包含 scoreId、appellantId、reason）
     * @return 持久化后的申诉记录
     */
    @Operation(summary = "提交申诉")
    @PostMapping("/appeals")
    @Audited(action = "APPEAL", targetType = "QUALITY", description = "质检申诉")
    public ApiResponse<QualityAppeal> submitAppeal(@RequestBody QualityAppeal appeal) {
        return ApiResponse.success(qualityService.submitAppeal(appeal));
    }

    /**
     * 复核申诉。
     * <p>
     * 质检员对申诉进行复核：通过则更新原评分备注；驳回则保留原评分不变。
     * 记录复核意见和复核时间。
     * </p>
     *
     * @param id       申诉 ID
     * @param approved 是否通过（true 为通过，false 为驳回）
     * @param comment  复核意见
     * @return 空响应
     */
    @Operation(summary = "复核申诉")
    @PutMapping("/appeals/{id}/review")
    @Audited(action = "REVIEW_APPEAL", targetType = "QUALITY", description = "复核申诉")
    public ApiResponse<Void> reviewAppeal(@PathVariable Long id,
                                           @RequestParam Boolean approved,
                                           @RequestParam String comment) {
        qualityService.reviewAppeal(id, approved, comment);
        return ApiResponse.success();
    }
}
