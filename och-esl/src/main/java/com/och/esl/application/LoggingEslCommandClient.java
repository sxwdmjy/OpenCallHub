package com.och.esl.application;

import com.och.esl.application.PhoneControlCommandService;
import lombok.extern.slf4j.Slf4j;
import com.och.esl.application.PhoneControlCommandService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import com.och.esl.application.PhoneControlCommandService;
import org.springframework.stereotype.Component;

/**
 * 日志型 ESL 命令适配器。
 * <p>
 * 本实现不连接真实 FreeSWITCH，只记录将要发送的命令并返回成功。
 * 它的作用是保留清晰端口，便于本地开发、单元测试和后续替换真实 ESL client。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
@ConditionalOnMissingBean(EslCommandClient.class)
public class LoggingEslCommandClient implements EslCommandClient {

    @Override
    public PhoneControlCommandService.CommandResult sendCommand(String commandType, String commandId, String command) {
        log.info("[EslCommandClient] 模拟发送命令: type={}, commandId={}, command={}", commandType, commandId, command);
        return new PhoneControlCommandService.CommandResult(commandId, command, true, null);
    }
}
