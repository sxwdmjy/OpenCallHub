package com.och.resource.domain;

/**
 * 资源类型枚举，定义系统中可管理的资源种类。
 * 每个枚举值对应一种资源类型，其 label 用于前端展示。
 *
 * @author danmo
 * @date 2025-01-01
 */
public enum ResourceType {

    SIP_ACCOUNT("SIP账号"),
    EXTENSION("分机"),
    PHONE_NUMBER("电话号码"),
    NUMBER_POOL("号码池"),
    TRUNK_GATEWAY("中继网关"),
    FREESWITCH_NODE("FreeSWITCH节点"),
    KAMAILIO_NODE("Kamailio节点"),
    AUDIO_ASSET("音频素材"),
    STORAGE_CONFIG("存储配置"),
    AI_PLUGIN("AI插件");

    private final String label;

    ResourceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
