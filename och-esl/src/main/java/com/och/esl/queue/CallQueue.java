package com.och.esl.queue;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * @author danmo
 * @date 2024-11-16 14:22
 **/
@Builder
@Data
public class CallQueue implements Comparable<CallQueue> {
    /**
     * 1-普通 2-VIP
     */
    private Integer type;

    /**
     * 通话ID
     */
    private Long callId;

    /**
     * 进入时间
     */
    private Long startTime;

    /**
     * 技能组ID
     */
    private Long skillId;

    /**
     * 腿Id
     */
    private String uniqueId;

    /**
     * fs地址
     */
    private String address;

    /**
     * 播放排队音标识
     */
    private Boolean playFlag;

    @Override
    public int compareTo(@NotNull CallQueue o) {
        return startTime.compareTo(this.startTime);
    }
}
