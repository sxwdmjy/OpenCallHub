package com.och.esl.application;

import com.och.esl.application.DtmfProcessingService;
import lombok.extern.slf4j.Slf4j;
import com.och.esl.application.DtmfProcessingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DTMF 按键处理服务
 * <p>缓冲用户的 DTMF 按键序列，支持 IVR 流程中的按键导航。
 * 按键按 callId 分组缓存，支持单键模式和累积模式。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>IVR 菜单导航：用户按键 "1" → 路由到对应分支</li>
 *   <li>身份证号输入：累积多位数字后一次性验证</li>
 *   <li>卡号输入：按结束符（#）分隔多个输入段</li>
 *   <li>防重复：同一按键短时间内重复触发去重</li>
 * </ul>
 *
 * @author danmo
 */
@Slf4j
@Service
public class DtmfProcessingService {

    /** DTMF 缓冲区，按 callId 分组 */
    private final Map<String, DtmfBuffer> buffers = new ConcurrentHashMap<>();

    /** 缓冲区默认超时（秒），超过此时间未收到新按键则自动清空 */
    private static final long DEFAULT_TIMEOUT_SEC = 30;

    /**
     * DTMF 按键缓冲区
     */
    public record DtmfBuffer(
            String callId,
            StringBuilder digits,
            LocalDateTime firstDigitTime,
            int expectedDigits
    ) {
        /** 当前累积的按键字符串 */
        public String getDigits() {
            return digits.toString();
        }
        /** 是否已过期（超过超时时间） */
        public boolean isExpired(long timeoutSec) {
            return ChronoUnit.SECONDS.between(firstDigitTime, LocalDateTime.now()) > timeoutSec;
        }
    }

    /**
     * 推入一个 DTMF 按键。
     * <p>将按键追加到 callId 对应的缓冲区末尾，返回累积的完整按键序列。
     * 超时检查和缓冲区创建通过 compute 原子完成，消除 TOCTOU 竞争窗口。</p>
     *
     * @param callId     呼叫标识
     * @param digit      按键字符（0-9、*、#）
     * @param timeoutSec 缓冲区超时时间（秒），超时后自动清空重建
     * @return 累积的按键序列（含本次按键）
     */
    public String pushDigit(String callId, String digit, long timeoutSec) {
        long effectiveTimeout = timeoutSec <= 0 ? DEFAULT_TIMEOUT_SEC : timeoutSec;

        // 原子处理：获取或创建缓冲区，超时时自动重建
        DtmfBuffer buffer = buffers.compute(callId, (k, old) -> {
            if (old == null) {
                return new DtmfBuffer(callId, new StringBuilder(), LocalDateTime.now(), 0);
            }
            if (old.isExpired(effectiveTimeout)) {
                log.debug("[DtmfProcessing] 缓冲区超时，清空: callId={}, oldDigits={}",
                        callId, old.getDigits());
                return new DtmfBuffer(callId, new StringBuilder(), LocalDateTime.now(), old.expectedDigits());
            }
            return old;
        });

        // ── 结束符 # 处理：立即提交并清空缓冲区，不含 # 本身 ──
        if ("#".equals(digit)) {
            buffers.remove(callId);
            log.info("[DtmfProcessing] # 结束符: callId={}, digits={}", callId, buffer.getDigits());
            return buffer.getDigits();
        }

        // ── 去重：相邻相同按键忽略（防止按键抖动重复触发） ──
        // 注意：此逻辑仅过滤物理抖动（毫秒级重复），不阻止 "111" 合法三连击。
        if (!buffer.digits().isEmpty()
                && buffer.digits().charAt(buffer.digits().length() - 1) == digit.charAt(0)) {
            log.debug("[DtmfProcessing] 重复按键已忽略: callId={}, digit={}", callId, digit);
            return buffer.getDigits();
        }

        // ── 追加按键 ──
        buffer.digits().append(digit.charAt(0));
        String accumulated = buffer.getDigits();
        log.debug("[DtmfProcessing] 按键: callId={}, digit={}, sequence={}", callId, digit, accumulated);

        // ── 达到预期位数 → 自动清空并返回 ──
        if (buffer.expectedDigits() > 0 && buffer.digits().length() >= buffer.expectedDigits()) {
            buffers.remove(callId);
            log.info("[DtmfProcessing] 输入完成: callId={}, digits={}", callId, accumulated);
            return accumulated;
        }

        return accumulated;
    }

    /**
     * 取出并清空指定 callId 的按键缓冲区
     *
     * @param callId 呼叫 ID
     * @return 清空前的按键序列，无缓冲区时返回空字符串
     */
    public String flush(String callId) {
        DtmfBuffer buffer = buffers.remove(callId);
        if (buffer != null) {
            log.debug("[DtmfProcessing] 清空缓冲区: callId={}, digits={}", callId, buffer.getDigits());
            return buffer.getDigits();
        }
        return "";
    }

    /**
     * 设置预期按键位数。
     * <p>设置后，当累积按键数达到此值时自动触发 flush 并返回完整序列。
     * 适用于身份证号、固定长度密码等场景。</p>
     * <p>注意：创建新的 DtmfBuffer 时使用 {@code new StringBuilder(getDigits())}
     * 拷贝一份独立的 StringBuilder，防止与旧 buffer 共享可变对象。</p>
     *
     * @param callId 呼叫 ID
     * @param count  预期位数，≤0 表示不限位数
     */
    public void setExpectedDigits(String callId, int count) {
        DtmfBuffer buffer = buffers.computeIfAbsent(callId,
                k -> new DtmfBuffer(callId, new StringBuilder(), LocalDateTime.now(), count));
        // 使用 getDigits() 获取 String 后重建 StringBuilder，避免共享旧 buffer 的 StringBuilder 引用
        buffers.put(callId, new DtmfBuffer(callId, new StringBuilder(buffer.getDigits()), buffer.firstDigitTime(), count));
    }

    /**
     * 获取当前缓冲区数量（用于监控）
     */
    public int bufferCount() {
        return buffers.size();
    }

    /**
     * 清理所有缓冲区（定时任务调用）
     */
    public int cleanExpired() {
        int before = buffers.size();
        buffers.entrySet().removeIf(e -> e.getValue().isExpired(DEFAULT_TIMEOUT_SEC));
        int removed = before - buffers.size();
        if (removed > 0) {
            log.debug("[DtmfProcessing] 清理过期缓冲区: {} 个", removed);
        }
        return removed;
    }
}
