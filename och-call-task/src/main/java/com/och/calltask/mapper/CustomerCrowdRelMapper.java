package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import com.och.calltask.domain.entity.CustomerCrowdRel;

import java.util.List;

/**
 * 人群客户关联表(CustomerCrowdRel)表数据库访问层
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
@Repository()
@Mapper
public interface CustomerCrowdRelMapper extends BaseMapper<CustomerCrowdRel> {

    void batchUpsert(@Param("relList") List<CustomerCrowdRel> relList);

}

