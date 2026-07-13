package com.och.esl.application;

import com.och.esl.domain.EslChannelState;
import com.och.esl.domain.NormalizedEslEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 活跃通道状态追踪器
 * <p>在内存中维护当前活跃通话通道的快照表，通过监听 ESL 事件实时更新通道状态。
 * 覆盖全部 17 种 ESL 事件类型的通道状态转换。</p>
 *
 * <h3>状态转换表（ESL 事件 → 通道状态）</h3>
 * <ul>
 *   <li>CHANNEL_CREATE           → CS_NEW（注册）</li>
 *   <li>CHANNEL_PROGRESS         → CS_ROUTING</li>
 *   <li>CHANNEL_PROGRESS_MEDIA   → CS_ROUTING</li>
 *   <li>CHANNEL_OUTGOING         → CS_ROUTING（外呼通道创建）</li>
 *   <li>CHANNEL_ANSWER           → CS_EXECUTE</li>
 *   <li>CHANNEL_EXECUTE          → CS_EXECUTE</li>
 *   <li>CHANNEL_EXECUTE_COMPLETE → CS_EXECUTE</li>
 *   <li>CHANNEL_ORIGINATE        → CS_EXECUTE（外呼结果）</li>
 *   <li>CHANNEL_BRIDGE           → CS_EXCHANGE_MEDIA</li>
 *   <li>CHANNEL_UNHOLD           → CS_EXCHANGE_MEDIA</li>
 *   <li>CHANNEL_UNPARK           → CS_EXCHANGE_MEDIA</li>
 *   <li>CHANNEL_HOLD             → 保持原状态（状态不变）</li>
 *   <li>CHANNEL_PARK             → 保持原状态（状态不变）</li>
 *   <li>CHANNEL_HANGUP           → CS_HANGUP（标记，不移除）</li>
 *   <li>CHANNEL_DESTROY          → CS_DESTROY（从活跃表移除）</li>
 *   <li>DTMF / RECORD_* / CODEC  → 不改变通道状态</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>byUuid 使用 {@link ConcurrentHashMap#compute} 实现原子读写，确保同一 UUID 的
 * 并发更新安全。byCallId 存储 callId → UUID 的单向映射，写入操作通过同步块
 * 与 byUuid 的写操作协调。</p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class ChannelStateTracker {

    /** UUID → 通道快照（主索引） */
    private final Map<String, ChannelSnapshot> byUuid = new ConcurrentHashMap<>();

    /** callId → UUID（辅助索引，仅存映射关系避免数据冗余） */
    private final Map<String, String> byCallId = new ConcurrentHashMap<>();

    /** 双索引写操作锁（保护 byUuid 和 byCallId 的联合更新原子性） */
    private final Object indexLock = new Object();

    /**
     * 通道快照（不可变记录）
     *
     * @param freeswitchUuid FreeSWITCH 通道 UUID
     * @param callId         呼叫唯一标识（Caller-Unique-ID）
     * @param callerNumber   主叫号码
     * @param calledNumber   被叫号码
     * @param channelState   当前通道状态
     * @param lastEventTime  最后一次事件时间
     * @param createTime     通道创建时间
     */
    public record ChannelSnapshot(
            String freeswitchUuid,
            String callId,
            String callerNumber,
            String calledNumber,
            EslChannelState channelState,
            LocalDateTime lastEventTime,
            LocalDateTime createTime
    ) {}

    // ═══════════════════ 注册 ═══════════════════

    /** CHANNEL_CREATE → 注册新通道 */
    @EventListener(condition = "#event.eventType.name() == 'CHANNEL_CREATE'")
    public void onChannelCreate(NormalizedEslEvent event) {
        if (!isTrackable(event)) {
            log.debug("[ChannelStateTracker] 忽略缺少关键标识的通道创建事件: callId={}, uuid={}",
                    event.getCallId(), event.getFreeswitchUuid());
            return;
        }
        ChannelSnapshot snapshot = new ChannelSnapshot(
                event.getFreeswitchUuid(), event.getCallId(),
                EslUtils.maskPhone(event.getCallerNumber()), EslUtils.maskPhone(event.getCalledNumber()),
                EslChannelState.CS_NEW, LocalDateTime.now(), LocalDateTime.now()
        );
        synchronized (indexLock) {
            byUuid.put(event.getFreeswitchUuid(), snapshot);
            byCallId.put(event.getCallId(), event.getFreeswitchUuid());
        }
    }

    // ═══════════════════ 状态更新 ═══════════════════

    /** CHANNEL_PROGRESS / PROGRESS_MEDIA / OUTGOING → CS_ROUTING */
    @EventListener(condition = "#event.eventType.name() == 'CHANNEL_PROGRESS' || #event.eventType.name() == 'CHANNEL_PROGRESS_MEDIA' || #event.eventType.name() == 'CHANNEL_OUTGOING'")
    public void onProgress(NormalizedEslEvent event) {
        updateState(event, EslChannelState.CS_ROUTING);
    }

    /** CHANNEL_ANSWER / EXECUTE / EXECUTE_COMPLETE / ORIGINATE → CS_EXECUTE */
    @EventListener(condition = "#event.eventType.name() == 'CHANNEL_ANSWER' || #event.eventType.name() == 'CHANNEL_EXECUTE' || #event.eventType.name() == 'CHANNEL_EXECUTE_COMPLETE' || #event.eventType.name() == 'CHANNEL_ORIGINATE'")
    public void onExecute(NormalizedEslEvent event) {
        updateState(event, EslChannelState.CS_EXECUTE);
    }

    /** CHANNEL_BRIDGE / UNHOLD / UNPARK → CS_EXCHANGE_MEDIA */
    @EventListener(condition = "#event.eventType.name() == 'CHANNEL_BRIDGE' || #event.eventType.name() == 'CHANNEL_UNHOLD' || #event.eventType.name() == 'CHANNEL_UNPARK'")
    public void onMedia(NormalizedEslEvent event) {
        updateState(event, EslChannelState.CS_EXCHANGE_MEDIA);
    }

    /** CHANNEL_HOLD / PARK → 状态不变，仅更新事件时间 */
    @EventListener(condition = "#event.eventType.name() == 'CHANNEL_HOLD' || #event.eventType.name() == 'CHANNEL_PARK'")
    public void onHold(NormalizedEslEvent event) {
        touchTime(event);
    }

    // ═══════════════════ 移除 ═══════════════════

    /** CHANNEL_HANGUP → CS_HANGUP（标记挂断，等待 DESTROY 时移除） */
    @EventListener(condition = "#event.eventType.name() == 'CHANNEL_HANGUP'")
    public void onChannelHangup(NormalizedEslEvent event) {
        updateState(event, EslChannelState.CS_HANGUP);
    }

    /** CHANNEL_DESTROY → 从活跃表中移除 */
    @EventListener(condition = "#event.eventType.name() == 'CHANNEL_DESTROY'")
    public void onChannelDestroy(NormalizedEslEvent event) {
        if (event.getFreeswitchUuid() == null || event.getFreeswitchUuid().isBlank()) {
            return;
        }
        ChannelSnapshot removed;
        synchronized (indexLock) {
            removed = byUuid.remove(event.getFreeswitchUuid());
            if (removed != null) {
                byCallId.remove(removed.callId());
            }
        }
        if (removed != null) {
            log.debug("[ChannelStateTracker] 通道销毁: uuid={}, callId={}, duration={}s",
                    event.getFreeswitchUuid(), removed.callId(),
                    java.time.Duration.between(removed.createTime(), LocalDateTime.now()).getSeconds());
        }
    }

    // ═══════════════════ 辅助 ═══════════════════

    /**
     * 原子更新通道状态。
     * <p>使用 {@link ConcurrentHashMap#compute} 保证对同一 UUID 的读写原子性，
     * 消除"读取旧值 → 构建新值 → 写入"过程中的竞态窗口。</p>
     */
    private void updateState(NormalizedEslEvent event, EslChannelState newState) {
        if (!isTrackable(event)) {
            return;
        }
        byUuid.compute(event.getFreeswitchUuid(), (uuid, snapshot) -> {
            if (snapshot == null) return null;
            return new ChannelSnapshot(
                    snapshot.freeswitchUuid(), snapshot.callId(),
                    snapshot.callerNumber(), snapshot.calledNumber(),
                    newState, LocalDateTime.now(), snapshot.createTime()
            );
        });
    }

    /**
     * 仅更新时间戳，不改变状态。
     * <p>同样使用 compute 保证原子性。</p>
     */
    private void touchTime(NormalizedEslEvent event) {
        if (!isTrackable(event)) {
            return;
        }
        byUuid.compute(event.getFreeswitchUuid(), (uuid, snapshot) -> {
            if (snapshot == null) return null;
            return new ChannelSnapshot(
                    snapshot.freeswitchUuid(), snapshot.callId(),
                    snapshot.callerNumber(), snapshot.calledNumber(),
                    snapshot.channelState(), LocalDateTime.now(), snapshot.createTime()
            );
        });
    }

    /**
     * 判断事件是否具备通道追踪所需的最小标识。
     */
    private boolean isTrackable(NormalizedEslEvent event) {
        return event != null
                && event.getFreeswitchUuid() != null && !event.getFreeswitchUuid().isBlank()
                && event.getCallId() != null && !event.getCallId().isBlank();
    }

    // ═══════════════════ 查询 ═══════════════════

    /**
     * 根据 FreeSWITCH 通道 UUID 查找活跃通道。
     *
     * @param uuid FreeSWITCH 通道 UUID
     * @return 通道快照（可能为空）
     */
    public Optional<ChannelSnapshot> findByUuid(String uuid) {
        return Optional.ofNullable(byUuid.get(uuid));
    }

    /**
     * 根据呼叫唯一标识查找活跃通道。
     * <p>先通过 callId → UUID 映射查找 UUID，再从主索引获取快照。</p>
     *
     * @param callId 呼叫唯一标识
     * @return 通道快照（可能为空）
     */
    public Optional<ChannelSnapshot> findByCallId(String callId) {
        if (callId == null || callId.isBlank()) {
            return Optional.empty();
        }
        String uuid = byCallId.get(callId);
        if (uuid == null) return Optional.empty();
        return Optional.ofNullable(byUuid.get(uuid));
    }

    /**
     * 获取所有活跃通道快照列表。
     *
     * @return 不可变的活跃通道快照集合
     */
    public List<ChannelSnapshot> getAllActive() {
        return List.copyOf(byUuid.values());
    }

    /**
     * 获取当前活跃通道数量。
     *
     * @return 活跃通道计数
     */
    public int activeCount() {
        return byUuid.size();
    }
}
