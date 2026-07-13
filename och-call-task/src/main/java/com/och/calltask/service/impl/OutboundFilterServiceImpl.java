package com.och.calltask.service.impl;

import com.och.calltask.domain.OutboundCustomer;
import com.och.calltask.domain.OutboundCustomerState;
import com.och.calltask.mapper.OutboundCustomerMapper;
import com.och.compliance.service.IComplianceCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

/**
 * 外呼客户过滤服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundFilterServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.calltask.mapper.OutboundCustomerMapper, com.och.calltask.domain.OutboundCustomer> implements com.och.calltask.service.IOutboundFilterService {

    private final IComplianceCheckService complianceService;

    /**
     * 对指定任务下所有 IMPORTED 状态的客户执行过滤
     */
    @Transactional
    public void applyFilters(Long taskId) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OutboundCustomer>();
        wrapper.eq(OutboundCustomer::getTaskId, taskId)
                .eq(OutboundCustomer::getState, OutboundCustomerState.IMPORTED.name());
        var customers = baseMapper.selectList(wrapper);

        for (OutboundCustomer c : customers) {
            OutboundCustomerState newState;
            String filterResult;

            // DNC/黑名单检查
            var complianceResult = complianceService.check(c.getPhoneNumber());
            if (complianceResult.blocked()) {
                newState = OutboundCustomerState.BLOCKED;
                filterResult = "BLOCKED:" + complianceResult.filterType() + ":" + complianceResult.reason();
            }
            // 时间窗检查
            else if (!isInAllowedTimeWindow()) {
                newState = OutboundCustomerState.BLOCKED;
                filterResult = "BLOCKED:TIME_WINDOW:不在允许的外呼时间窗内";
            } else {
                newState = OutboundCustomerState.READY;
                filterResult = "PASS";
            }

            c.setState(newState.name());
            c.setFilterResult(filterResult);
            baseMapper.updateById(c);
        }

        log.info("[OutboundFilter] Task {} filtered {} customers", taskId, customers.size());
    }

    /**
     * 检查当前时间是否在允许的外呼时间窗口内（09:00 - 21:00）
     */
    private boolean isInAllowedTimeWindow() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(LocalTime.of(9, 0)) && !now.isAfter(LocalTime.of(21, 0));
    }
}
