package com.och.esl.application;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * ESL 命令参数校验器
 * <p>
 * 对 FreeSWITCH ESL 命令的所有外部输入参数进行白名单校验，防止命令注入。
 * 校验分为四类：
 * <ol>
 *   <li>UUID 格式校验：FreeSWITCH 通道 UUID 必须是标准 hex 格式</li>
 *   <li>号码格式校验：主被叫号码仅允许安全字符集，拒绝 shell 元字符</li>
 *   <li>挂断原因白名单：仅允许已知 SIP cause 枚举值</li>
 *   <li>通道变量校验：变量名和值均要求安全字符集</li>
 * </ol>
 * </p>
 *
 * <h3>设计原则</h3>
 * <ul>
 *   <li>白名单优于黑名单：明确允许安全字符，拒绝一切未列入的字符</li>
 *   <li>失败封闭：校验不通过直接抛出 IllegalArgumentException，阻断危险命令</li>
 *   <li>最小权限：仅放行 ESL 命令行所必需的字符</li>
 * </ul>
 *
 * @author danmo
 */
public final class EslCommandValidator {

    private EslCommandValidator() {
        // utility class
    }

    // ==================== UUID 校验 ====================

    /** FreeSWITCH UUID 格式：8-4-4-4-12 hex（带连字符或压缩格式） */
    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[a-fA-F0-9]{8}(-?[a-fA-F0-9]{4}){3}-?[a-fA-F0-9]{12}$");

    /**
     * 校验 FreeSWITCH 通道 UUID 格式。
     * <p>FreeSWITCH UUID 为 hex 字符串，允许标准格式（带连字符）或压缩格式（无连字符）。
     * 如果来源是数据库而非外部直接输入，可跳过该校验但建议保留。</p>
     *
     * @param uuid 待校验的 UUID 字符串
     * @throws IllegalArgumentException 格式不合法
     */
    public static void validateUuid(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            throw new IllegalArgumentException("FreeSWITCH UUID 不能为空");
        }
        if (!UUID_PATTERN.matcher(uuid).matches()) {
            throw new IllegalArgumentException(
                    "FreeSWITCH UUID 格式非法，仅允许 hex 字符和连字符: " + truncateForError(uuid));
        }
    }

    // ==================== 号码格式校验 ====================

    /**
     * 安全的电话/分机号字符集：
     * 数字、字母、加号（E.164）、星号（* 键）、井号（# 键）、
     * 连字符、下划线、点号、@（SIP URI）、冒号（SIP 端口）
     */
    private static final Pattern SAFE_PHONE_PATTERN =
            Pattern.compile("^[a-zA-Z0-9+*#\\-_.@:]+$");

    /**
     * 校验主被叫号码或目标号码，仅允许安全字符集。
     * <p>禁止空格、分号、管道、反引号、美元符等 shell/SIP 注入元字符。</p>
     *
     * @param number 待校验的号码字符串
     * @param fieldName 字段名（用于错误信息）
     * @throws IllegalArgumentException 包含非法字符
     */
    public static void validatePhoneNumber(String number, String fieldName) {
        if (number == null || number.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        if (!SAFE_PHONE_PATTERN.matcher(number).matches()) {
            throw new IllegalArgumentException(
                    fieldName + " 包含非法字符: " + truncateForError(number));
        }
        // 额外长度限制，防止超长字符串攻击
        if (number.length() > 256) {
            throw new IllegalArgumentException(fieldName + " 长度超过限制(256): " + number.length());
        }
    }

    // ==================== 挂断原因白名单 ====================

    /** 已知合法的 SIP Hangup Cause 枚举值 */
    private static final Set<String> VALID_HANGUP_CAUSES = Set.of(
            "NORMAL_CLEARING", "USER_BUSY", "NO_ANSWER", "NO_USER_RESPONSE",
            "CALL_REJECTED", "DESTINATION_OUT_OF_ORDER", "NORMAL_TEMPORARY_FAILURE",
            "NORMAL_CIRCUIT_CONGESTION", "NETWORK_OUT_OF_ORDER", "NORMAL_UNSPECIFIED",
            "ORIGINATOR_CANCEL", "LOSE_RACE", "UNALLOCATED_NUMBER",
            "USER_NOT_REGISTERED", "REQUESTED_CHAN_UNAVAIL", "SERVICE_UNAVAILABLE",
            "CHAN_NOT_IMPLEMENTED", "MANDATORY_IE_MISSING", "RECOVERY_ON_TIMER_EXPIRE",
            "WRONG_CALL_STATE", "PICKED_OFF", "UNSPECIFIED"
    );

    /**
     * 校验挂断原因码是否合法（允许 null，null 时跳过校验）。
     * <p>仅允许已知 SIP cause 枚举值，防止注入任意字符串。</p>
     *
     * @param cause 挂断原因码
     * @throws IllegalArgumentException 非法的挂断原因
     */
    public static void validateHangupCause(String cause) {
        if (cause == null) {
            return; // nullable
        }
        if (!VALID_HANGUP_CAUSES.contains(cause)) {
            throw new IllegalArgumentException(
                    "非法的挂断原因码: " + truncateForError(cause));
        }
    }

    // ==================== 通道变量校验 ====================

    /** FreeSWITCH 通道变量名安全字符集：字母或下划线开头，后续可为字母、数字、下划线、连字符 */
    private static final Pattern SAFE_VAR_NAME_PATTERN =
            Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_\\-]*$");

    /**
     * 通道变量值安全字符集。
     * <p>仅允许 SIP 和 FreeSWITCH 通道变量所必需的字符，严格禁止以下可能引发命令注入的字符：</p>
     * <ul>
     *   <li><b>;</b> — FreeSWITCH originate 属性分隔符，可注入 {@code ;execute=system(...)}</li>
     *   <li><b>'</b> — 单引号，在 lua/system dialplan 应用中可逃逸字符串</li>
     *   <li><b>[ ]</b> — FreeSWITCH 通道变量引用语法 {@code [variable]}，可读取/篡改任意变量</li>
     *   <li><b>! ` $ & | &lt; &gt;</b> — shell 元字符</li>
     * </ul>
     */
    private static final Pattern SAFE_VAR_VALUE_PATTERN =
            Pattern.compile("^[a-zA-Z0-9+*#\\-_.@:/=%(), ]*$");

    /**
     * 校验通道变量映射中的键和值。
     * <p>键必须符合 FreeSWITCH 变量命名规范，值仅允许安全字符集。
     * 严格禁止 {@code ; ' [ ]} 等可导致 originate 命令注入的字符。</p>
     *
     * @param vars 通道变量映射
     * @throws IllegalArgumentException 包含非法键或值
     */
    public static void validateChannelVars(Map<String, String> vars) {
        if (vars == null || vars.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // 键校验
            if (key == null || !SAFE_VAR_NAME_PATTERN.matcher(key).matches()) {
                throw new IllegalArgumentException(
                        "通道变量名包含非法字符: " + truncateForError(key));
            }
            if (key.length() > 128) {
                throw new IllegalArgumentException(
                        "通道变量名长度超过限制(128): " + key.length());
            }

            // 值校验
            if (value == null) {
                continue;
            }
            if (!SAFE_VAR_VALUE_PATTERN.matcher(value).matches()) {
                throw new IllegalArgumentException(
                        "通道变量值包含非法字符, key=" + key + ", value=" + truncateForError(value));
            }
            if (value.length() > 1024) {
                throw new IllegalArgumentException(
                        "通道变量值长度超过限制(1024), key=" + key + ", len=" + value.length());
            }
        }
    }

    /**
     * 媒体文件路径安全字符集。
     * <p>允许 FreeSWITCH sounds/recordings 下常见的相对路径字符，禁止反斜杠、空格、分号、管道等注入字符。</p>
     */
    private static final Pattern SAFE_MEDIA_PATH_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_./\\-]+$");

    /**
     * 校验放音或录音文件路径。
     * <p>ESL 命令中的文件路径会直接拼接到命令行，必须限制为安全相对路径，不允许目录回退。</p>
     *
     * @param path      文件路径
     * @param fieldName 字段名（用于错误信息）
     */
    public static void validateMediaPath(String path, String fieldName) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        if (path.contains("..") || path.startsWith("/") || path.startsWith(".")) {
            throw new IllegalArgumentException(fieldName + " 必须是安全相对路径: " + truncateForError(path));
        }
        if (!SAFE_MEDIA_PATH_PATTERN.matcher(path).matches()) {
            throw new IllegalArgumentException(fieldName + " 包含非法字符: " + truncateForError(path));
        }
        if (path.length() > 512) {
            throw new IllegalArgumentException(fieldName + " 长度超过限制(512): " + path.length());
        }
    }

    /**
     * 业务呼叫标识安全字符集。
     * <p>callId 可能来自 FreeSWITCH UUID，也可能来自业务系统写入的 variable_call_id，不能强制按 UUID 校验。</p>
     */
    private static final Pattern SAFE_CALL_ID_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_\\-.]+$");

    /**
     * 校验业务呼叫标识。
     *
     * @param callId 呼叫标识
     */
    public static void validateCallId(String callId) {
        if (callId == null || callId.isEmpty()) {
            throw new IllegalArgumentException("callId 不能为空");
        }
        if (!SAFE_CALL_ID_PATTERN.matcher(callId).matches()) {
            throw new IllegalArgumentException("callId 包含非法字符: " + truncateForError(callId));
        }
        if (callId.length() > 128) {
            throw new IllegalArgumentException("callId 长度超过限制(128): " + callId.length());
        }
    }

    // ==================== FreeSWITCH 节点标识校验 ====================

    /** FreeSWITCH 节点标识安全字符集：字母、数字、连字符、下划线、点号 */
    private static final Pattern SAFE_FS_ID_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_\\-.]+$");

    /**
     * 校验 FreeSWITCH 节点标识符。
     * <p>仅允许字母、数字、连字符、下划线和点号，长度限制 128 字符。</p>
     *
     * @param freeswitchId FreeSWITCH 节点标识
     * @throws IllegalArgumentException 包含非法字符或超长
     */
    public static void validateFreeswitchId(String freeswitchId) {
        if (freeswitchId == null || freeswitchId.isEmpty()) {
            throw new IllegalArgumentException("FreeSWITCH 节点标识不能为空");
        }
        if (!SAFE_FS_ID_PATTERN.matcher(freeswitchId).matches()) {
            throw new IllegalArgumentException(
                    "FreeSWITCH 节点标识包含非法字符: " + truncateForError(freeswitchId));
        }
        if (freeswitchId.length() > 128) {
            throw new IllegalArgumentException(
                    "FreeSWITCH 节点标识长度超过限制(128): " + freeswitchId.length());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 截断并清理错误信息中的用户输入，防止日志注入和日志伪造。
     * <p>过滤换行、回车、制表符等控制字符，避免攻击者在日志中伪造日志行。</p>
     */
    private static String truncateForError(String input) {
        if (input == null) return "null";
        String cleaned = input.replaceAll("[\\r\\n\\t]", "_");
        if (cleaned.length() <= 40) return cleaned;
        return cleaned.substring(0, 40) + "...(truncated)";
    }
}
