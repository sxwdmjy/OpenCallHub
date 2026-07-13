package com.och.knowledge.api;

import lombok.RequiredArgsConstructor;

import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.knowledge.service.IKnowledgeService;
import com.och.knowledge.domain.KnowledgeDocument;
import com.och.knowledge.domain.ScriptTemplate;
import com.och.security.audit.Audited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库管理控制器。
 * <p>
 * 提供知识文档和话术模板的检索与管理 REST API。
 * 坐席在通话中可通过搜索接口快速查找相关知识，提升服务效率。
 * </p>
 *
 * @author danmo
 */
@Tag(name = "Knowledge", description = "知识库与话术")
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final IKnowledgeService knowledgeService;

    // ==================== 知识文档 ====================

    /**
     * 搜索知识库。
     * <p>按关键字匹配标题、摘要、内容、标签字段，仅返回已发布文档。结果按浏览次数降序排列。</p>
     *
     * @param keyword 搜索关键字
     * @return 匹配的知识文档列表
     */
    @Operation(summary = "搜索知识库")
    @GetMapping("/search")
    public ApiResponse<PageResponse<KnowledgeDocument>> search(@RequestParam String keyword) {
        return ApiResponse.success(knowledgeService.search(keyword));
    }

    /**
     * 分页查询知识文档列表。
     * <p>仅返回已发布的文档，按更新时间降序排列。</p>
     *
     * @param pageNum  页码（默认 1）
     * @param pageSize 每页记录数（默认 20）
     * @return 分页响应的文档列表
     */
    @Operation(summary = "知识文档列表")
    @GetMapping("/documents")
    public ApiResponse<PageResponse<KnowledgeDocument>> listDocuments(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(knowledgeService.listDocuments(pageNum, pageSize));
    }

    /**
     * 创建知识文档。
     * <p>新文档默认未发布，需手动发布后方可被检索。</p>
     *
     * @param doc 知识文档
     * @return 创建成功的文档
     */
    @PostMapping("/documents")
    @Audited(action = "CREATE", targetType = "KNOWLEDGE", description = "创建知识文档")
    public ApiResponse<KnowledgeDocument> createDocument(@RequestBody KnowledgeDocument doc) {
        return ApiResponse.success(knowledgeService.createDocument(doc));
    }

    // ==================== 话术模板 ====================

    /**
     * 查询话术模板列表。
     * <p>支持按关键字筛选，匹配话术名称和关键词字段，仅返回已发布的话术。</p>
     *
     * @param keyword 搜索关键字（可选）
     * @return 匹配的话术列表
     */
    @Operation(summary = "话术列表")
    @GetMapping("/scripts")
    public ApiResponse<PageResponse<ScriptTemplate>> listScripts(
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(knowledgeService.listScripts(keyword));
    }

    /**
     * 创建话术模板。
     * <p>自动生成话术编码，初始版本号为 1，默认未发布。</p>
     *
     * @param script 话术模板
     * @return 创建成功的话术
     */
    @PostMapping("/scripts")
    @Audited(action = "CREATE", targetType = "SCRIPT", description = "创建话术")
    public ApiResponse<ScriptTemplate> createScript(@RequestBody ScriptTemplate script) {
        return ApiResponse.success(knowledgeService.createScript(script));
    }
}
