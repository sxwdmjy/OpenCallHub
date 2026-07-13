package com.och.esl.domain;

/**
 * FreeSWITCH ESL 事件类型枚举
 * <p>定义所有 ESL 标准事件类型，覆盖通道生命周期、DTMF、录音、会议、心跳等场景。
 * mapEventType() 方法负责将 FreeSWITCH 原始事件名字符串映射到此枚举。</p>
 *
 * <h3>通道生命周期事件</h3>
 * <ul>
 *   <li><b>CHANNEL_CREATE</b> - 通道创建，呼叫发起时触发</li>
 *   <li><b>CHANNEL_OUTGOING</b> - 外呼通道创建（FreeSWITCH 向外部发起呼叫前触发）</li>
 *   <li><b>CHANNEL_ORIGINATE</b> - originate 外呼结果（originate 命令执行完成后触发）</li>
 *   <li><b>CHANNEL_PROGRESS</b> - 通道进度，如收到 SIP 183 Session Progress</li>
 *   <li><b>CHANNEL_PROGRESS_MEDIA</b> - 早期媒体建立</li>
 *   <li><b>CHANNEL_ANSWER</b> - 通道接听（被叫摘机或座席点击接听）</li>
 *   <li><b>CHANNEL_BRIDGE</b> - 通道桥接，两条腿接通开始通话</li>
 *   <li><b>CHANNEL_UNBRIDGE</b> - 通道解桥接（转接完成后原 B-leg 断开）</li>
 *   <li><b>CHANNEL_HOLD</b> - 通道被保持</li>
 *   <li><b>CHANNEL_UNHOLD</b> - 通道取消保持</li>
 *   <li><b>CHANNEL_PARK</b> - 通道被 parking（转接或呼叫等待场景）</li>
 *   <li><b>CHANNEL_UNPARK</b> - 通道从 park 状态恢复</li>
 *   <li><b>CHANNEL_HANGUP</b> - 通道挂断（含 CHANNEL_HANGUP_COMPLETE）</li>
 *   <li><b>CHANNEL_EXECUTE</b> - 通道开始执行 dialplan 应用</li>
 *   <li><b>CHANNEL_EXECUTE_COMPLETE</b> - dialplan 应用执行完成</li>
 *   <li><b>CHANNEL_DESTROY</b> - 通道销毁，资源回收</li>
 * </ul>
 *
 * <h3>媒体与信号事件</h3>
 * <ul>
 *   <li><b>DTMF</b> - 用户按键信号</li>
 *   <li><b>RECORD_START</b> - 录音开始</li>
 *   <li><b>RECORD_STOP</b> - 录音停止</li>
 *   <li><b>CODEC</b> - 编解码协商</li>
 * </ul>
 *
 * <h3>系统事件</h3>
 * <ul>
 *   <li><b>HEARTBEAT</b> - 心跳，FreeSWITCH 定期发送以确认 ESL 连接存活</li>
 *   <li><b>CUSTOM</b> - 自定义事件（含 conference::maintenance、callcenter::info 等子系统事件）</li>
 * </ul>
 *
 * @author danmo
 */
public enum EslEventType {

    /* ── 通道生命周期 ── */
    CHANNEL_CREATE,          // 通道创建
    CHANNEL_OUTGOING,        // 外呼通道创建
    CHANNEL_ORIGINATE,       // originate 外呼结果
    CHANNEL_PROGRESS,        // 通道进度（183 Session Progress）
    CHANNEL_PROGRESS_MEDIA,  // 早期媒体
    CHANNEL_ANSWER,          // 通道接听
    CHANNEL_BRIDGE,          // 通道桥接
    CHANNEL_UNBRIDGE,        // 通道解桥接
    CHANNEL_HOLD,            // 通道保持
    CHANNEL_UNHOLD,          // 取消保持
    CHANNEL_PARK,            // 通道 parking
    CHANNEL_UNPARK,          // 通道从 park 恢复
    CHANNEL_HANGUP,          // 通道挂断
    CHANNEL_EXECUTE,         // dialplan 执行
    CHANNEL_EXECUTE_COMPLETE,// dialplan 执行完成
    CHANNEL_DESTROY,         // 通道销毁

    /* ── 媒体与信号 ── */
    DTMF,                    // 按键
    RECORD_START,            // 录音开始
    RECORD_STOP,             // 录音停止
    CODEC,                   // 编解码

    /* ── 系统 ── */
    HEARTBEAT,               // 心跳
    CUSTOM                   // 自定义
}
