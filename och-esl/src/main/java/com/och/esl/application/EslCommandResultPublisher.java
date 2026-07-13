package com.och.esl.application;

import lombok.extern.slf4j.Slf4j;
import com.och.esl.application.PhoneControlCommandService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * ESL 命令结果异步发布器
 * <p>管理出站 ESL 命令的异步回执。每次发送命令时注册一个 CompletableFuture，
 * ESL 响应到达时通过 commandId 匹配并完成 Future，调用方可阻塞等待或异步回调。</p>
 *
 * <h3>使用示例</h3>
 * <pre>
 * // 发送方
 * String cmdId = UUID.randomUUID().toString();
 * CompletableFuture<EslCommandResult> future = publisher.registerPending(cmdId, 30, TimeUnit.SECONDS);
 * eslClient.sendAsyncApiCommand(cmdId, "uuid_answer " + uuid);
 *
 * // 等待结果（可阻塞或 thenAccept 异步回调）
 * EslCommandResult result = future.get(30, TimeUnit.SECONDS);
 *
 * // 响应方（ESL 事件回调中）
 * publisher.complete(cmdId, result);
 * </pre>
 *
 * @author danmo
 */
@Slf4j
@Component
public class EslCommandResultPublisher {

    /** 待回执的命令 Map，key=commandId */
    private final Map<String, PendingCommand> pendingCommands = new ConcurrentHashMap<>();

    /**
     * 待回执的命令包装
     */
    private record PendingCommand(
            String commandId,
            String command,
            CompletableFuture<PhoneControlCommandService.CommandResult> future,
            LocalDateTime createdAt
    ) {}

    /**
     * 注册一个待回执的命令
     *
     * @param commandId  命令唯一标识
     * @param command    命令字符串（用于日志）
     * @param timeout    超时时间
     * @param unit       时间单位
     * @return 可等待的 Future，超时后自动完成（result.accepted=false, error="TIMEOUT"）
     */
    public CompletableFuture<PhoneControlCommandService.CommandResult> registerPending(
            String commandId, String command, long timeout, TimeUnit unit) {

        CompletableFuture<PhoneControlCommandService.CommandResult> future = new CompletableFuture<>();

        // 超时自动完成
        CompletableFuture.delayedExecutor(timeout, unit).execute(() -> {
            if (!future.isDone()) {
                PhoneControlCommandService.CommandResult timeoutResult =
                        new PhoneControlCommandService.CommandResult(commandId, command, false, "TIMEOUT");
                future.complete(timeoutResult);
                pendingCommands.remove(commandId);
                log.warn("[EslCmdResult] 命令超时: commandId={}, cmd={}", commandId, command);
            }
        });

        pendingCommands.put(commandId, new PendingCommand(commandId, command, future, LocalDateTime.now()));
        log.debug("[EslCmdResult] 注册待回执命令: commandId={}, cmd={}, timeout={}{}",
                commandId, command, timeout, unit.toString().toLowerCase());
        return future;
    }

    /**
     * 完成一个待回执的命令（由 ESL 响应回调触发）
     *
     * @param commandId 命令唯一标识
     * @param result    命令执行结果
     */
    public void complete(String commandId, PhoneControlCommandService.CommandResult result) {
        PendingCommand pending = pendingCommands.remove(commandId);
        if (pending != null) {
            pending.future().complete(result);
            log.debug("[EslCmdResult] 命令完成: commandId={}, accepted={}", commandId, result.accepted());
        } else {
            log.debug("[EslCmdResult] 命令未在待回执列表中: commandId={}", commandId);
        }
    }

    /**
     * 获取当前待回执命令数
     */
    public int pendingCount() {
        return pendingCommands.size();
    }

    /**
     * 清理超时的待回执命令（定时任务调用）
     *
     * @param maxAgeSeconds 命令存活最大秒数
     * @return 清理数量
     */
    public int cleanExpired(long maxAgeSeconds) {
        int before = pendingCommands.size();
        pendingCommands.entrySet().removeIf(entry -> {
            long age = java.time.Duration.between(
                    entry.getValue().createdAt(), LocalDateTime.now()).getSeconds();
            if (age > maxAgeSeconds) {
                entry.getValue().future().complete(
                        new PhoneControlCommandService.CommandResult(
                                entry.getKey(), entry.getValue().command(), false, "EXPIRED"));
                return true;
            }
            return false;
        });
        int removed = before - pendingCommands.size();
        if (removed > 0) {
            log.debug("[EslCmdResult] 清理超时命令: {} 个", removed);
        }
        return removed;
    }
}
