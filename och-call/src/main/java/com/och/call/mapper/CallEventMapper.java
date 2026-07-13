package com.och.call.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.call.domain.CallEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 呼叫事件数据访问层
 * <p>仅保留 MyBatis-Plus BaseMapper 能力，复杂查询由服务层使用 LambdaQueryWrapper 组合。</p>
 *
 * @author danmo
 */
@Mapper
public interface CallEventMapper extends BaseMapper<CallEvent> {

    /**
     * 根据去重 key 统计已成功处理的事件数量，用于幂等检查。
     *
     * @param dedupKey 事件去重 key
     * @return 已处理事件数量
     */
    default long countByDedupKey(String dedupKey) {
        return selectCount(new LambdaQueryWrapper<CallEvent>()
                .eq(CallEvent::getDedupKey, dedupKey)
                .eq(CallEvent::getProcessed, true));
    }
}
