package com.och.ticket.domain;

/** 工单状态枚举 */
public enum TicketState {
    NEW("新建"), ASSIGNED("已分配"), PROCESSING("处理中"),
    PENDING("待确认"), RESOLVED("已解决"), CLOSED("已关闭"), REOPENED("已重开");
    private final String label;
    TicketState(String label) { this.label = label; }
    public String getLabel() { return label; }
}
