package com.och.ticket.service;

import com.och.common.api.PageResponse;
import com.och.ticket.domain.Ticket;

public interface ITicketService {
    PageResponse<Ticket> list(int pageNum, int pageSize);
    Ticket get(Long id);
    Ticket create(Ticket ticket);
    Ticket update(Long id, Ticket ticket);
    void assign(Long id, Long assigneeId);
    void resolve(Long id, String comment);
    void close(Long id);
}
