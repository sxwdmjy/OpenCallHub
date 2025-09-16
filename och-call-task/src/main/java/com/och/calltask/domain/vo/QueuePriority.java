package com.och.calltask.domain.vo;

import lombok.Getter;

/**
 * 队列优先级枚举
 * 
 * @author danmo
 * @date 2025/01/15
 */
@Getter
public enum QueuePriority {
    
    /**
     * 紧急 - 最高优先级
     */
    URGENT(1, "紧急", 100),
    
    /**
     * 高优先级
     */
    HIGH(2, "高", 80),
    
    /**
     * 普通优先级
     */
    NORMAL(3, "普通", 60),
    
    /**
     * 低优先级
     */
    LOW(4, "低", 40),
    
    /**
     * 最低优先级
     */
    LOWEST(5, "最低", 20);
    
    private final Integer code;
    private final String description;
    private final Integer weight;
    
    QueuePriority(Integer code, String description, Integer weight) {
        this.code = code;
        this.description = description;
        this.weight = weight;
    }
    
    /**
     * 根据代码获取优先级
     */
    public static QueuePriority getByCode(Integer code) {
        for (QueuePriority priority : values()) {
            if (priority.getCode().equals(code)) {
                return priority;
            }
        }
        return NORMAL; // 默认返回普通优先级
    }
    
    /**
     * 根据权重获取优先级
     */
    public static QueuePriority getByWeight(Integer weight) {
        for (QueuePriority priority : values()) {
            if (priority.getWeight().equals(weight)) {
                return priority;
            }
        }
        return NORMAL; // 默认返回普通优先级
    }
    
    /**
     * 比较优先级（数值越小优先级越高）
     */
    public int comparePriority(QueuePriority other) {
        return Integer.compare(this.code, other.code);
    }
    
    /**
     * 是否比指定优先级高
     */
    public boolean isHigherThan(QueuePriority other) {
        return this.code < other.code;
    }
    
    /**
     * 是否比指定优先级低
     */
    public boolean isLowerThan(QueuePriority other) {
        return this.code > other.code;
    }
}
