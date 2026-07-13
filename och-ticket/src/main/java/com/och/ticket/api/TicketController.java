package com.och.ticket.api;

import lombok.RequiredArgsConstructor;

import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.security.audit.Audited;
import com.och.ticket.service.ITicketService;
import com.och.ticket.domain.Ticket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 工单管理控制器。
 * <p>
 * 提供工单 CRUD 及核心流转操作的 REST API：
 * 工单创建、查询、更新、分配处理人、解决、关闭。
 * 所有操作均通过 {@link TicketService} 执行，保证事务一致性和状态机校验。
 * </p>
 *
 * @author danmo
 */
@Tag(name = "Ticket", description = "工单管理")
@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final ITicketService ticketService;

    /**
     * 分页查询工单列表。
     * <p>按创建时间降序排列，最新工单在前。</p>
     *
     * @param pageNum  页码（默认 1）
     * @param pageSize 每页记录数（默认 20）
     * @return 包含工单列表和分页信息的响应
     */
    @Operation(summary = "工单列表")
    @GetMapping
    public ApiResponse<PageResponse<Ticket>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(ticketService.list(pageNum, pageSize));
    }

    /**
     * 根据 ID 查询工单详情。
     *
     * @param id 工单 ID
     * @return 工单详情，不存在则返回 null
     */
    @Operation(summary = "获取工单详情")
    @GetMapping("/{id}")
    public ApiResponse<Ticket> get(@PathVariable Long id) {
        return ApiResponse.success(ticketService.get(id));
    }

    /**
     * 创建工单。
     * <p>自动生成工单编号，初始状态为 NEW。</p>
     *
     * @param ticket 工单实体
     * @return 创建成功的工单（含自动生成的编号和 ID）
     */
    @PostMapping
    @Audited(action = "CREATE", targetType = "TICKET", description = "创建工单")
    public ApiResponse<Ticket> create(@RequestBody Ticket ticket) {
        return ApiResponse.success(ticketService.create(ticket));
    }

    /**
     * 更新工单信息。
     * <p>根据 ID 全量覆盖更新，ID 不可修改。</p>
     *
     * @param id     工单 ID
     * @param ticket 更新内容
     * @return 更新后的工单
     */
    @PutMapping("/{id}")
    @Audited(action = "UPDATE", targetType = "TICKET", description = "更新工单")
    public ApiResponse<Ticket> update(@PathVariable Long id, @RequestBody Ticket ticket) {
        return ApiResponse.success(ticketService.update(id, ticket));
    }

    /**
     * 分配工单。
     * <p>将工单分配给指定处理人，仅 NEW 或 REOPENED 状态的工单可被分配。</p>
     *
     * @param id         工单 ID
     * @param assigneeId 处理人 ID
     * @return 空响应
     */
    @PutMapping("/{id}/assign")
    @Audited(action = "ASSIGN", targetType = "TICKET", description = "分配工单")
    public ApiResponse<Void> assign(@PathVariable Long id, @RequestParam Long assigneeId) {
        ticketService.assign(id, assigneeId);
        return ApiResponse.success();
    }

    /**
     * 解决工单。
     * <p>标记工单已解决，仅 ASSIGNED 或 PROCESSING 状态的工单可流转至已解决。</p>
     *
     * @param id      工单 ID
     * @param comment 解决备注
     * @return 空响应
     */
    @PutMapping("/{id}/resolve")
    @Audited(action = "RESOLVE", targetType = "TICKET", description = "解决工单")
    public ApiResponse<Void> resolve(@PathVariable Long id, @RequestParam String comment) {
        ticketService.resolve(id, comment);
        return ApiResponse.success();
    }

    /**
     * 关闭工单。
     * <p>将工单状态变更为 CLOSED，已关闭的工单不可重复关闭。</p>
     *
     * @param id 工单 ID
     * @return 空响应
     */
    @PutMapping("/{id}/close")
    @Audited(action = "CLOSE", targetType = "TICKET", description = "关闭工单")
    public ApiResponse<Void> close(@PathVariable Long id) {
        ticketService.close(id);
        return ApiResponse.success();
    }
}
