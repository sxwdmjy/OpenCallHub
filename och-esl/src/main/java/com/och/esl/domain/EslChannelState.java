package com.och.esl.domain;

/**
 * FreeSWITCH 通道状态枚举
 * <p>将 FreeSWITCH 原始的 CS_* 通道状态映射为强类型枚举，
 * 替代 {@link NormalizedEslEvent} 中的 {@code String channelState} 字段。</p>
 *
 * <h3>状态流转（典型场景）</h3>
 * <pre>
 * CS_NEW → CS_INIT → CS_ROUTING → CS_EXECUTE → CS_EXCHANGE_MEDIA → CS_HANGUP → CS_REPORTING → CS_DESTROY
 * </pre>
 *
 * @author danmo
 */
public enum EslChannelState {

    /** 新通道已创建，尚未初始化 */
    CS_NEW("新创建"),
    /** 通道已初始化，正在分配资源 */
    CS_INIT("初始化"),
    /** 通道正在路由（dialplan 查找中） */
    CS_ROUTING("路由中"),
    /** 通道正在执行 dialplan 应用 */
    CS_EXECUTE("执行中"),
    /** 通道已建立媒体交换（双方通话中） */
    CS_EXCHANGE_MEDIA("媒体交换"),
    /** 通道已挂断，等待清理 */
    CS_HANGUP("已挂断"),
    /** 通道正在生成 CDR 报告 */
    CS_REPORTING("报告生成"),
    /** 通道已销毁，资源已释放 */
    CS_DESTROY("已销毁"),
    /** 通道状态未定义或未知 */
    CS_NONE("未知");

    private final String label;

    EslChannelState(String label) { this.label = label; }
    public String getLabel() { return label; }

    /**
     * 从 FreeSWITCH 原始字符串解析通道状态
     *
     * @param fsState FreeSWITCH 通道状态字符串（如 "CS_EXECUTE"）
     * @return 对应的枚举值，无法匹配时返回 CS_NONE
     */
    public static EslChannelState fromFsString(String fsState) {
        if (fsState == null || fsState.isEmpty()) return CS_NONE;
        try {
            return valueOf(fsState.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CS_NONE;
        }
    }

    /** 是否为活跃状态（通话中或正在处理） */
    public boolean isActive() {
        return this == CS_EXECUTE || this == CS_EXCHANGE_MEDIA || this == CS_ROUTING;
    }

    /** 是否为终态 */
    public boolean isTerminal() {
        return this == CS_DESTROY;
    }
}
