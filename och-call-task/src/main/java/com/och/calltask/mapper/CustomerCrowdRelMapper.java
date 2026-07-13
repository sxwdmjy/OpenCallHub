package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.calltask.domain.CustomerCrowdRel;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 人群-客户关联数据访问层
 * <p>
 * 管理人群与客户的多对多关系，支持批量 upsert 和按人群ID逻辑删除。
 * </p>
 */
@Mapper
public interface CustomerCrowdRelMapper extends BaseMapper<CustomerCrowdRel> {

    /**
     * 批量插入人群-客户关联记录
     * <p>使用 INSERT IGNORE 避免重复插入（依赖 crowd_id + customer_id 唯一索引）</p>
     */
    @Insert("<script>" +
            "INSERT IGNORE INTO customer_crowd_rel (crowd_id, customer_id, created_at, deleted) VALUES " +
            "<foreach collection='relList' item='rel' separator=','>" +
            "(#{rel.crowdId}, #{rel.customerId}, #{rel.createdAt}, 0)" +
            "</foreach>" +
            "</script>")
    void batchInsert(@Param("relList") List<CustomerCrowdRel> relList);

    /**
     * 逻辑删除指定人群下的所有关联记录
     *
     * @param crowdId 人群ID
     */
    @Update("UPDATE customer_crowd_rel SET deleted = 1 WHERE crowd_id = #{crowdId} AND deleted = 0")
    void deleteByCrowdId(@Param("crowdId") Long crowdId);

    /**
     * 查询指定人群下的客户ID列表（只返回未删除记录）
     *
     * @param crowdId 人群ID
     * @return 客户ID列表
     */
    @org.apache.ibatis.annotations.Select("SELECT customer_id FROM customer_crowd_rel " +
            "WHERE crowd_id = #{crowdId} AND deleted = 0 ORDER BY id ASC")
    List<Long> findCustomerIdsByCrowdId(@Param("crowdId") Long crowdId);
}
