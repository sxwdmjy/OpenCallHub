package com.och.ivr.domain;

/**
 * IVR节点类型枚举
 * <p>定义IVR流程中所有可用的节点类型，每种类型对应不同的路由或动作逻辑。</p>
 *
 * <h3>路由节点（决定下一跳）</h3>
 * <ul>
 *   <li>START - 开始节点，流程入口</li>
 *   <li>MENU - 菜单节点，根据DTMF按键匹配边条件进行路由</li>
 *   <li>SCHEDULE_CHECK - 时间检查节点，根据工作时间/非工作时间走对应分支</li>
 *   <li>CONDITION - 条件判断节点，根据变量值动态选择分支</li>
 * </ul>
 *
 * <h3>动作节点（执行特定操作后继续或终结）</h3>
 * <ul>
 *   <li>PLAY - 播放语音，完成后走默认分支</li>
 *   <li>PLAYBACK - 播放语音（与PLAY同义），完成后走默认分支</li>
 *   <li>COLLECT_DIGITS - 收集用户按键输入</li>
 *   <li>API_CALL - 调用外部API，根据返回结果走对应分支</li>
 *   <li>HANGUP - 挂断通话，终态节点</li>
 * </ul>
 *
 * <h3>转接节点（将通话转移到其他目标）</h3>
 * <ul>
 *   <li>QUEUE - 转入排队队列，返回排队标记由上层处理</li>
 *   <li>TRANSFER - 通用转接（转人工/转外线），根据 configJson 中的目标号码转移</li>
 *   <li>TRANSFER_AGENT - 转指定座席</li>
 *   <li>TRANSFER_SKILL_GROUP - 转技能组</li>
 *   <li>TRANSFER_VOICEMAIL - 转语音信箱</li>
 * </ul>
 */
public enum IvrNodeType {
    /** 开始节点，IVR流程的入口 */
    START,
    /** 菜单节点，根据用户DTMF按键匹配边条件决定下一跳 */
    MENU,
    /** 时间检查节点，根据当前时间是否在工作时间范围内选择分支 */
    SCHEDULE_CHECK,
    /** 通用转接：根据configJson中配置的目标号码进行转接 */
    TRANSFER,
    /** 转语音信箱 */
    TRANSFER_VOICEMAIL,
    /** 播放语音节点，播放完成后走默认（第一条）边 */
    PLAY,
    /** 播放语音节点（与PLAY同义），播放完成后走默认分支 */
    PLAYBACK,
    /** 收集用户按键输入 */
    COLLECT_DIGITS,
    /** 条件判断节点，根据变量值动态选择满足条件的分支 */
    CONDITION,
    /** API调用节点，调用外部接口并根据返回结果路由 */
    API_CALL,
    /** 转指定座席 */
    TRANSFER_AGENT,
    /** 转技能组 */
    TRANSFER_SKILL_GROUP,
    /** 转入排队队列 */
    QUEUE,
    /** 挂断节点，终态 */
    HANGUP;
}
