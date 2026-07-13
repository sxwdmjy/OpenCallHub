package com.och.notice.api;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.notice.service.INoticeService;
import com.och.notice.domain.NoticeMessage;
import com.och.security.audit.Audited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知消息控制器。
 * <p>
 * 提供通知消息相关的 REST API 接口，包括：
 * <ul>
 *   <li>查询用户通知列表（支持过滤未读）</li>
 *   <li>标记单条通知为已读</li>
 *   <li>标记用户全部通知为已读</li>
 * </ul>
 * 所有接口返回统一的 {@link ApiResponse} 响应体。
 * </p>
 *
 * @author danmo
 */
@Tag(name = "Notice", description = "通知消息")
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final INoticeService noticeService;

    /**
     * 查询当前用户的通知列表。
     * <p>
     * 通过 userId 查询通知，支持 unreadOnly 参数过滤未读通知。
     * 当 unreadOnly=true 时仅返回未读通知；否则返回全部通知。
     * </p>
     *
     * @param userId     接收者用户 ID
     * @param unreadOnly 是否仅查询未读通知，默认 false
     * @return 通知消息列表
     */
    @Operation(summary = "我的通知列表")
    @GetMapping("/my")
    public ApiResponse<List<NoticeMessage>> myNotices(@RequestParam Long userId,
                                                       @RequestParam(defaultValue = "false") Boolean unreadOnly) {
        List<NoticeMessage> notices = noticeService.myNotices(userId, unreadOnly != null && unreadOnly);
        return ApiResponse.success(notices);
    }

    /**
     * 标记指定通知为已读。
     * <p>
     * 将通知的 isRead 字段更新为 true，同时记录当前的读取时间。
     * </p>
     *
     * @param id 通知 ID
     * @return 空响应体，表示操作成功
     */
    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        noticeService.markRead(id);
        return ApiResponse.success();
    }

    /**
     * 标记指定用户的所有未读通知为已读。
     * <p>
     * 批量将该用户所有 isRead=false 的通知更新为已读状态，
     * 并记录读取时间为当前时间。
     * </p>
     *
     * @param userId 用户 ID
     * @return 空响应体，表示操作成功
     */
    @Operation(summary = "全部已读")
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead(@RequestParam Long userId) {
        noticeService.markAllRead(userId);
        return ApiResponse.success();
    }

    /**
     * 发送通知给指定用户。
     *
     * @param notice 通知消息（需包含 title、content、type、receiverId）
     * @return 创建的通知消息
     */
    @Operation(summary = "发送通知")
    @PostMapping("/send")
    @Audited(action = "SEND_NOTICE", targetType = "NOTICE")
    public ApiResponse<NoticeMessage> sendNotice(@RequestBody NoticeMessage notice) {
        return ApiResponse.success(noticeService.sendNotice(
                notice.getTitle(), notice.getContent(), notice.getType(),
                notice.getSenderId(), notice.getReceiverId()));
    }

    /**
     * 广播通知给多个用户。
     *
     * @param notice  通知消息（需包含 title、content、type）
     * @param userIds 接收者ID列表
     * @return 发送条数
     */
    @Operation(summary = "广播通知")
    @PostMapping("/broadcast")
    @Audited(action = "BROADCAST_NOTICE", targetType = "NOTICE")
    public ApiResponse<Integer> broadcast(@RequestBody NoticeMessage notice,
                                           @RequestParam List<Long> userIds) {
        int count = noticeService.broadcast(
                notice.getTitle(), notice.getContent(), notice.getType(),
                notice.getSenderId(), userIds);
        return ApiResponse.success(count);
    }

    /**
     * 管理员分页查询通知列表。
     */
    @Operation(summary = "通知分页列表")
    @GetMapping("/page")
    public ApiResponse<PageResponse<NoticeMessage>> pageNotices(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<NoticeMessage> page = noticeService.pageNotices(pageNum, pageSize, keyword);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }
}
