package com.och.esl.application;

import com.och.esl.application.PhoneControlCommandService;

/**
 * ESL 命令发送端口。
 * <p>
 * 内部电话控制服务只依赖这个端口，不直接依赖具体 FreeSWITCH ESL 客户端。
 * 当前阶段可以用日志适配器保证模块可编译、可测试；接入真实 FreeSWITCH 时只需要替换端口实现。
 * </p>
 *
 * @author danmo
 */
public interface EslCommandClient {

    /**
     * 发送 FreeSWITCH ESL API 命令。
     *
     * @param commandType 命令类型，用于日志、审计和问题排查
     * @param commandId   命令关联标识，通常是通道 UUID 或 originate UUID
     * @param command     完整 ESL 命令文本
     * @return 命令执行结果
     */
    PhoneControlCommandService.CommandResult sendCommand(String commandType, String commandId, String command);
}
