package com.och.calltask.service.impl;

import com.och.calltask.application.OutboundCustomerStateMachine;
import com.och.calltask.domain.*;
import com.och.calltask.mapper.OutboundCustomerMapper;
import com.och.calltask.mapper.OutboundDialAttemptMapper;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 预览式外呼服务
 * <p>
 * 实现预览式外呼模式：座席先领取客户，确认后再发起拨号。
 * 核心方法包括客户领取、释放和拨号发起。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreviewDialingServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.calltask.mapper.OutboundCustomerMapper, com.och.calltask.domain.OutboundCustomer> implements com.och.calltask.service.IPreviewDialingService {

    private final OutboundDialAttemptMapper attemptMapper;
    private final OutboundCustomerStateMachine stateMachine;

    /**
     * 座席领取一个就绪状态的客户
     */
    @Transactional
    public OutboundCustomer claimCustomer(Long taskId, Long agentId) {
        var readyCustomers = baseMapper.findReadyCustomers(taskId, 1);
        if (readyCustomers.isEmpty()) {
            throw new BizException(ErrorCode.NOT_FOUND, "没有可用的客户");
        }

        OutboundCustomer customer = readyCustomers.get(0);
        stateMachine.validate(OutboundCustomerState.valueOf(customer.getState()), OutboundCustomerState.CLAIMED);

        customer.setState(OutboundCustomerState.CLAIMED.name());
        customer.setAgentId(agentId);
        baseMapper.updateById(customer);

        log.info("[PreviewDialing] Agent {} claimed customer {} for task {}", agentId, customer.getId(), taskId);
        return customer;
    }

    /**
     * 座席释放已领取的客户，状态回到 READY
     */
    @Transactional
    public void releaseCustomer(Long customerId, Long agentId) {
        OutboundCustomer customer = baseMapper.selectById(customerId);
        if (customer == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "客户不存在");
        }
        if (!Objects.equals(customer.getAgentId(), agentId)) {
            throw new BizException(ErrorCode.BIZ_OPERATION_DENIED, "无权释放该客户");
        }
        stateMachine.validate(OutboundCustomerState.valueOf(customer.getState()), OutboundCustomerState.READY);
        customer.setState(OutboundCustomerState.READY.name());
        customer.setAgentId(null);
        baseMapper.updateById(customer);
    }

    /**
     * 座席对已领取的客户发起拨号
     */
    @Transactional
    public OutboundDialAttempt startDial(Long taskId, Long customerId, Long agentId) {
        OutboundCustomer customer = baseMapper.selectById(customerId);
        if (customer == null || !Objects.equals(customer.getTaskId(), taskId)) {
            throw new BizException(ErrorCode.NOT_FOUND, "客户不存在");
        }
        if (!Objects.equals(customer.getAgentId(), agentId)) {
            throw new BizException(ErrorCode.BIZ_OPERATION_DENIED, "无权拨打该客户");
        }

        stateMachine.validate(OutboundCustomerState.valueOf(customer.getState()), OutboundCustomerState.DIALING);

        int attemptNo = customer.getAttemptCount() != null ? customer.getAttemptCount() + 1 : 1;
        customer.setState(OutboundCustomerState.DIALING.name());
        customer.setAttemptCount(attemptNo);
        baseMapper.updateById(customer);

        String dedupKey = taskId + ":" + customerId + ":" + attemptNo;
        OutboundDialAttempt attempt = OutboundDialAttempt.builder()
                .taskId(taskId)
                .customerId(customerId)
                .attemptNo(attemptNo)
                .dedupKey(dedupKey)
                .state("DIALING")
                .agentId(agentId)
                .dialTime(LocalDateTime.now())
                .build();
        attemptMapper.insert(attempt);

        log.info("[PreviewDialing] Dial attempt {} for task {} customer {}, dedupKey={}",
                attemptNo, taskId, customerId, dedupKey);
        return attempt;
    }
}
