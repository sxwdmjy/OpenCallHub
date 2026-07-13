package com.och.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.och.common.api.PageResponse;
import com.och.ticket.domain.Ticket;
import com.och.ticket.domain.TicketState;
import com.och.ticket.mapper.TicketMapper;
import com.och.ticket.service.ITicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 工单管理服务实现。
 * <p>继承 MyBatis-Plus ServiceImpl 获得批量操作和分页能力。</p>
 */
@Slf4j
@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements ITicketService {

    @Override
    public PageResponse<Ticket> list(int pageNum, int pageSize) {
        log.info("查询工单列表：pageNum={}, pageSize={}", pageNum, pageSize);
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Ticket::getCreatedAt);
        IPage<Ticket> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public Ticket get(Long id) {
        log.info("查询工单详情：id={}", id);
        return baseMapper.selectById(id);
    }

    @Override
    @Transactional
    public Ticket create(Ticket ticket) {
        ticket.setTicketNo(generateTicketNo());
        ticket.setState(TicketState.NEW.name());
        baseMapper.insert(ticket);
        log.info("工单创建成功：id={}, ticketNo={}, title={}", ticket.getId(), ticket.getTicketNo(), ticket.getTitle());
        return ticket;
    }

    @Override
    @Transactional
    public Ticket update(Long id, Ticket ticket) {
        ticket.setId(id);
        baseMapper.updateById(ticket);
        log.info("工单更新成功：id={}", id);
        return baseMapper.selectById(id);
    }

    @Override
    @Transactional
    public void assign(Long id, Long assigneeId) {
        log.info("分配工单：id={}, assigneeId={}", id, assigneeId);
        Ticket ticket = baseMapper.selectById(id);
        if (ticket == null) {
            log.warn("工单不存在：id={}", id);
            throw new IllegalArgumentException("工单不存在：" + id);
        }
        String currentState = ticket.getState();
        if (!TicketState.NEW.name().equals(currentState) && !TicketState.REOPENED.name().equals(currentState)) {
            throw new IllegalStateException("当前工单状态为 " + currentState + "，只有新建或已重开工单才能分配");
        }
        Ticket update = new Ticket();
        update.setId(id);
        update.setAssigneeId(assigneeId);
        update.setState(TicketState.ASSIGNED.name());
        baseMapper.updateById(update);
        log.info("工单分配成功：id={}, assigneeId={}, 状态变更={}->{}", id, assigneeId, currentState, TicketState.ASSIGNED.name());
    }

    @Override
    @Transactional
    public void resolve(Long id, String comment) {
        log.info("解决工单：id={}, comment={}", id, comment);
        Ticket ticket = baseMapper.selectById(id);
        if (ticket == null) throw new IllegalArgumentException("工单不存在：" + id);
        String currentState = ticket.getState();
        if (!TicketState.ASSIGNED.name().equals(currentState) && !TicketState.PROCESSING.name().equals(currentState)) {
            throw new IllegalStateException("当前工单状态为 " + currentState + "，只有已分配或处理中的工单才能解决");
        }
        Ticket update = new Ticket();
        update.setId(id);
        update.setState(TicketState.RESOLVED.name());
        update.setRemark(comment);
        update.setResolvedAt(LocalDateTime.now());
        baseMapper.updateById(update);
    }

    @Override
    @Transactional
    public void close(Long id) {
        log.info("关闭工单：id={}", id);
        Ticket ticket = baseMapper.selectById(id);
        if (ticket == null) throw new IllegalArgumentException("工单不存在：" + id);
        if (TicketState.CLOSED.name().equals(ticket.getState())) throw new IllegalStateException("工单已关闭");
        Ticket update = new Ticket();
        update.setId(id);
        update.setState(TicketState.CLOSED.name());
        baseMapper.updateById(update);
    }

    private String generateTicketNo() {
        return "WO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
