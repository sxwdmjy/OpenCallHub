package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.calltask.domain.OutboundCustomer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/** 外呼客户数据访问层 */
@Mapper
public interface OutboundCustomerMapper extends BaseMapper<OutboundCustomer> {

    /**
     * 查询指定任务的就绪客户（带行锁，防止并发领取同一客户）
     * <p>FOR UPDATE 确保在当前事务中锁定查询到的行，其他事务无法同时读取和修改这些行，
     * 从而保证座席领取客户时的原子性。</p>
     */
    @Select("SELECT * FROM outbound_customer WHERE task_id = #{taskId} AND state = 'READY' LIMIT #{limit} FOR UPDATE")
    List<OutboundCustomer> findReadyCustomers(@Param("taskId") Long taskId, @Param("limit") int limit);

    /** 统计指定任务的待处理客户数 */
    @Select("SELECT COUNT(*) FROM outbound_customer WHERE task_id = #{taskId} AND state IN ('READY', 'SCHEDULED', 'RETRY_WAIT')")
    int countPending(@Param("taskId") Long taskId);
}
