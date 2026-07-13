package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.calltask.domain.CustomerCrowd;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 客户人群数据访问层
 * <p>
 * 基于 MyBatis-Plus BaseMapper 提供人群定义的基本 CRUD 操作。
 * 按筛选条件匹配客户档案的查询逻辑在 CustomerCrowdService 中通过 MyBatis-Plus QueryWrapper 实现。
 * </p>
 */
@Mapper
public interface CustomerCrowdMapper extends BaseMapper<CustomerCrowd> {

    /**
     * 统计人群成员数量（通过关联表）
     *
     * @param crowdId 人群ID
     * @return 人群成员数量
     */
    @Select("SELECT COUNT(*) FROM customer_crowd_rel ccr " +
            "WHERE ccr.crowd_id = #{crowdId} AND ccr.deleted = 0")
    int countCrowdMembers(@Param("crowdId") Long crowdId);
}
