package com.och.calltask.domain;

public enum OutboundTaskMode {
    PREVIEW("预览式"),
    PREDICTIVE("预测式"),
    ROBOT("机器人");
    private final String label;
    OutboundTaskMode(String label) { this.label = label; }
    public String getLabel() { return label; }
}
