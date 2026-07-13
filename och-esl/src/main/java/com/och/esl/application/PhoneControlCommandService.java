package com.och.esl.application;

import com.och.esl.application.EslCommandClient;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * 电话控制命令服务。
 * <p>
 * 对外提供接听、挂断、保持、桥接、转接、外呼、放音和录音等电话控制动作，
 * 内部统一转换为 FreeSWITCH ESL API 命令后通过 {@link EslCommandClient} 发送。
 * 这样业务代码不直接依赖具体 ESL 客户端，后续接入真实 FreeSWITCH 时只替换命令适配器。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneControlCommandService {

    private final EslCommandClient commandClient;

    /**
     * 命令执行结果记录。
     *
     * @param commandId 命令关联标识，通常是 FreeSWITCH 通道 UUID 或 originate UUID
     * @param command   发送的完整 ESL 命令字符串
     * @param accepted  命令是否被 ESL 服务端接受
     * @param error     命令失败原因，成功时为空
     */
    public record CommandResult(String commandId, String command, boolean accepted, String error) {}

    /**
     * 接听来电。
     * <p>向指定通道发送 {@code uuid_answer}，使振铃通道进入接通状态。</p>
     */
    public CommandResult answer(String freeswitchUuid) {
        EslCommandValidator.validateUuid(freeswitchUuid);
        return executeCmd("answer", freeswitchUuid, "uuid_answer " + freeswitchUuid);
    }

    /**
     * 挂断电话。
     * <p>挂断原因只允许 FreeSWITCH/SIP 已知 cause，避免外部输入拼接进命令。</p>
     */
    public CommandResult hangup(String freeswitchUuid, String cause) {
        EslCommandValidator.validateUuid(freeswitchUuid);
        EslCommandValidator.validateHangupCause(cause);
        String cmd = "uuid_kill " + freeswitchUuid + (cause != null ? " " + cause : "");
        return executeCmd("hangup", freeswitchUuid, cmd);
    }

    /**
     * 保持通话。
     * <p>保持后双方媒体流由 FreeSWITCH 控制，业务侧等待 CHANNEL_HOLD 事件确认最终状态。</p>
     */
    public CommandResult hold(String freeswitchUuid) {
        EslCommandValidator.validateUuid(freeswitchUuid);
        return executeCmd("hold", freeswitchUuid, "uuid_hold " + freeswitchUuid);
    }

    /**
     * 取消保持。
     * <p>恢复通话后业务侧等待 CHANNEL_UNHOLD 或 CHANNEL_BRIDGE 事件刷新状态。</p>
     */
    public CommandResult unhold(String freeswitchUuid) {
        EslCommandValidator.validateUuid(freeswitchUuid);
        return executeCmd("unhold", freeswitchUuid, "uuid_hold off " + freeswitchUuid);
    }

    /**
     * 将通道置于 park 状态。
     * <p>park 常用于转接、外呼桥接前的临时等待，不代表通话已经结束。</p>
     */
    public CommandResult park(String freeswitchUuid) {
        EslCommandValidator.validateUuid(freeswitchUuid);
        return executeCmd("park", freeswitchUuid, "uuid_park " + freeswitchUuid);
    }

    /**
     * 从 park 状态恢复通道。
     */
    public CommandResult unpark(String freeswitchUuid) {
        EslCommandValidator.validateUuid(freeswitchUuid);
        return executeCmd("unpark", freeswitchUuid, "uuid_transfer " + freeswitchUuid + " XML default");
    }

    /**
     * 桥接两个通道。
     * <p>桥接是通话双方真正进入媒体交换的动作，必须等待后续 CHANNEL_BRIDGE 事件确认。</p>
     */
    public CommandResult bridge(String uuidA, String uuidB) {
        EslCommandValidator.validateUuid(uuidA);
        EslCommandValidator.validateUuid(uuidB);
        return executeCmd("bridge", uuidA, "uuid_bridge " + uuidA + " " + uuidB);
    }

    /**
     * 盲转。
     * <p>座席不先与目标确认，FreeSWITCH 直接把当前通道转到目标号码。</p>
     */
    public CommandResult blindTransfer(String freeswitchUuid, String target) {
        EslCommandValidator.validateUuid(freeswitchUuid);
        EslCommandValidator.validatePhoneNumber(target, "targetNumber");
        String cmd = "uuid_transfer " + freeswitchUuid + " " + target + " XML default";
        return executeCmd("blindTransfer", freeswitchUuid, cmd);
    }

    /**
     * 咨询转。
     * <p>当前骨架按“创建咨询通道并桥接”的最小闭环实现，真实生产环境可把创建和完成转接拆成两步。</p>
     */
    public CommandResult consultTransfer(String freeswitchUuid, String target) {
        EslCommandValidator.validateUuid(freeswitchUuid);
        EslCommandValidator.validatePhoneNumber(target, "targetNumber");
        String consultUuid = UUID.randomUUID().toString().replace("-", "");
        String originateCmd = "originate {origination_uuid=" + consultUuid + "}" + target + " &park()";
        executeCmd("consultOriginate", consultUuid, originateCmd);
        String bridgeCmd = "uuid_bridge " + freeswitchUuid + " " + consultUuid;
        return executeCmd("consultBridge", freeswitchUuid, bridgeCmd);
    }

    /**
     * 创建外呼通道。
     * <p>通道变量来自业务系统，必须先做白名单校验，再拼接到 originate 参数中。</p>
     */
    public CommandResult originate(String destNumber, String callerId, Map<String, String> vars) {
        EslCommandValidator.validatePhoneNumber(destNumber, "destNumber");
        EslCommandValidator.validatePhoneNumber(callerId, "callerId");
        EslCommandValidator.validateChannelVars(vars);

        String cmdId = UUID.randomUUID().toString().replace("-", "");
        StringBuilder sb = new StringBuilder("originate {");
        if (vars != null) {
            vars.forEach((k, v) -> sb.append(k).append("=").append(v == null ? "" : v).append(","));
        }
        sb.append("origination_uuid=").append(cmdId).append("}").append(destNumber).append(" &park()");
        return executeCmd("originate", cmdId, sb.toString());
    }

    /**
     * 播放音频文件。
     * <p>只允许相对声音路径或常见文件名字符，防止通过文件路径注入额外 ESL 参数。</p>
     */
    public CommandResult playback(String freeswitchUuid, String filePath) {
        EslCommandValidator.validateUuid(freeswitchUuid);
        EslCommandValidator.validateMediaPath(filePath, "filePath");
        return executeCmd("playback", freeswitchUuid, "uuid_playback " + freeswitchUuid + " " + filePath);
    }

    /**
     * 中断当前媒体操作。
     */
    public CommandResult breakPlayback(String freeswitchUuid) {
        EslCommandValidator.validateUuid(freeswitchUuid);
        return executeCmd("breakPlayback", freeswitchUuid, "uuid_break " + freeswitchUuid);
    }

    /**
     * 开始录音。
     * <p>录音路径由业务侧生成，必须限制为安全相对路径，避免写入任意位置。</p>
     */
    public CommandResult startRecord(String freeswitchUuid, String filePath) {
        EslCommandValidator.validateUuid(freeswitchUuid);
        EslCommandValidator.validateMediaPath(filePath, "filePath");
        return executeCmd("startRecord", freeswitchUuid, "uuid_record " + freeswitchUuid + " start " + filePath);
    }

    /**
     * 停止录音。
     */
    public CommandResult stopRecord(String freeswitchUuid) {
        EslCommandValidator.validateUuid(freeswitchUuid);
        return executeCmd("stopRecord", freeswitchUuid, "uuid_record " + freeswitchUuid + " stop");
    }

    /**
     * 动态设置通道变量。
     */
    public CommandResult setVar(String freeswitchUuid, String varName, String varValue) {
        EslCommandValidator.validateUuid(freeswitchUuid);
        if (varName == null || varName.isEmpty()) {
            throw new IllegalArgumentException("变量名不能为空");
        }
        EslCommandValidator.validateChannelVars(Map.of(varName, varValue == null ? "" : varValue));
        String cmd = "uuid_setvar " + freeswitchUuid + " " + varName + " " + (varValue == null ? "" : varValue);
        return executeCmd("setVar", freeswitchUuid, cmd);
    }

    private CommandResult executeCmd(String type, String id, String eslCommand) {
        CommandResult result = commandClient.sendCommand(type, id, eslCommand);
        if (result.accepted()) {
            log.info("[PhoneControl] 命令已接受: type={}, commandId={}", type, id);
        } else {
            log.warn("[PhoneControl] 命令被拒绝: type={}, commandId={}, error={}", type, id, result.error());
        }
        return result;
    }
}
