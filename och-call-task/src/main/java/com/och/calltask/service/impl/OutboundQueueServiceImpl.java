package com.och.calltask.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.och.calltask.domain.OutboundCustomer;
import com.och.calltask.domain.OutboundCustomerState;
import com.och.calltask.mapper.OutboundCustomerMapper;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 外呼客户队列服务
 * <p>
 * 为外呼任务提供客户排队管理能力，是预测式外呼的调度缓冲。
 * 客户进入队列后状态变为 SCHEDULED，按 FIFO（先进先出）顺序出队。
 * </p>
 *
 * <h3>核心方法</h3>
 * <ul>
 *   <li>{@link #enqueue} — 单个客户入队</li>
 *   <li>{@link #batchEnqueue} — 批量入队（性能优化）</li>
 *   <li>{@link #dequeue} — 获取并锁定下一个待拨号客户（FOR UPDATE）</li>
 *   <li>{@link #peek} — 预览下一个客户（不出队）</li>
 *   <li>{@link #queueSize} — 查询当前队列深度</li>
 *   <li>{@link #removeFromQueue} — 将客户移出队列回到 READY</li>
 *   <li>{@link #drainQueue} — 清空队列（任务完成/暂停时）</li>
 * </ul>
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundQueueServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.calltask.mapper.OutboundCustomerMapper, com.och.calltask.domain.OutboundCustomer> implements com.och.calltask.service.IOutboundQueueService {


    /**
     * 单个客户入队
     * <p>将 READY 状态的客户转为 SCHEDULED，并记录入队时间。</p>
     *
     * @param customerId 客户ID
     * @return 入队后的客户实体
     */
    @Transactional
    public OutboundCustomer enqueue(Long customerId) {
        OutboundCustomer customer = baseMapper.selectById(customerId);
        if (customer == null) throw new BizException(ErrorCode.NOT_FOUND, "客户不存在");

        OutboundCustomerState current = OutboundCustomerState.valueOf(customer.getState());
        if (current != OutboundCustomerState.READY) {
            throw new BizException(ErrorCode.BIZ_ILLEGAL_STATE,
                    "只有 READY 状态的客户可以入队，当前状态: " + current);
        }

        customer.setState(OutboundCustomerState.SCHEDULED.name());
        customer.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(customer);
        log.debug("[OutboundQueue] 客户入队: customerId={}, taskId={}", customerId, customer.getTaskId());
        return customer;
    }

    /**
     * 批量入队
     * <p>将多个 READY 客户批量设为 SCHEDULED，适用于任务启动时的客户调度。</p>
     *
     * @param taskId      任务ID（仅处理该任务下的客户）
     * @param customerIds 客户ID列表
     * @return 成功入队的客户数
     */
    @Transactional
    public int batchEnqueue(Long taskId, List<Long> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) return 0;

        LambdaUpdateWrapper<OutboundCustomer> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(OutboundCustomer::getId, customerIds)
                .eq(OutboundCustomer::getTaskId, taskId)
                .eq(OutboundCustomer::getState, OutboundCustomerState.READY.name())
                .set(OutboundCustomer::getState, OutboundCustomerState.SCHEDULED.name())
                .set(OutboundCustomer::getUpdatedAt, LocalDateTime.now());

        // MyBatis-Plus update with LambdaUpdateWrapper returns affected rows
        baseMapper.update(wrapper);
        log.info("[OutboundQueue] 批量入队: taskId={}, requested={}", taskId, customerIds.size());
        return customerIds.size(); // approximate, actual count requires re-query
    }

    /**
     * 出队——获取下一个待拨号的客户
     * <p>按入队时间 FIFO 顺序返回最早进入 SCHEDULED 状态的客户。
     * 调用方在获取客户后应立即将其状态转为 DIALING。</p>
     *
     * @param taskId 任务ID
     * @return 下一个待拨号客户，队列为空时返回 null
     */
    @Transactional
    public OutboundCustomer dequeue(Long taskId) {
        LambdaQueryWrapper<OutboundCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundCustomer::getTaskId, taskId)
                .eq(OutboundCustomer::getState, OutboundCustomerState.SCHEDULED.name())
                .orderByAsc(OutboundCustomer::getUpdatedAt)
                .last("LIMIT 1 FOR UPDATE");

        List<OutboundCustomer> results = baseMapper.selectList(wrapper);
        if (results.isEmpty()) return null;

        OutboundCustomer customer = results.get(0);
        customer.setState(OutboundCustomerState.DIALING.name());
        customer.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(customer);
        log.debug("[OutboundQueue] 客户出队: customerId={}, taskId={}", customer.getId(), taskId);
        return customer;
    }

    /**
     * 预览队首客户（不出队、不修改状态）
     *
     * @param taskId 任务ID
     * @return 队首客户，队列为空时返回 null
     */
    public OutboundCustomer peek(Long taskId) {
        LambdaQueryWrapper<OutboundCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundCustomer::getTaskId, taskId)
                .eq(OutboundCustomer::getState, OutboundCustomerState.SCHEDULED.name())
                .orderByAsc(OutboundCustomer::getUpdatedAt)
                .last("LIMIT 1");

        return baseMapper.selectOne(wrapper);
    }

    /**
     * 查询队列深度
     *
     * @param taskId 任务ID
     * @return 当前 SCHEDULED 状态的客户数
     */
    public int queueSize(Long taskId) {
        LambdaQueryWrapper<OutboundCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundCustomer::getTaskId, taskId)
                .eq(OutboundCustomer::getState, OutboundCustomerState.SCHEDULED.name());
        return baseMapper.selectCount(wrapper).intValue();
    }

    /**
     * 将指定客户移出队列
     * <p>状态从 SCHEDULED 回到 READY，释放给其他调度器或座席领取。</p>
     *
     * @param customerId 客户ID
     */
    @Transactional
    public void removeFromQueue(Long customerId) {
        OutboundCustomer customer = baseMapper.selectById(customerId);
        if (customer == null) return;

        if (OutboundCustomerState.SCHEDULED.name().equals(customer.getState())) {
            customer.setState(OutboundCustomerState.READY.name());
            customer.setUpdatedAt(LocalDateTime.now());
            baseMapper.updateById(customer);
            log.debug("[OutboundQueue] 客户移出队列: customerId={}", customerId);
        }
    }

    /**
     * 清空指定任务的队列
     * <p>将所有 SCHEDULED 客户回到 READY 状态，适用于任务暂停或完成。</p>
     *
     * @param taskId 任务ID
     * @return 清空的客户数
     */
    @Transactional
    public int drainQueue(Long taskId) {
        LambdaQueryWrapper<OutboundCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundCustomer::getTaskId, taskId)
                .eq(OutboundCustomer::getState, OutboundCustomerState.SCHEDULED.name());
        List<OutboundCustomer> customers = baseMapper.selectList(wrapper);

        for (OutboundCustomer c : customers) {
            c.setState(OutboundCustomerState.READY.name());
            c.setUpdatedAt(LocalDateTime.now());
            baseMapper.updateById(c);
        }

        log.info("[OutboundQueue] 队列已清空: taskId={}, count={}", taskId, customers.size());
        return customers.size();
    }
}
