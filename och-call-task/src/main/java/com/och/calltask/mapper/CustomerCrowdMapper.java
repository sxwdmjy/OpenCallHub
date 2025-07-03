package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.calltask.domain.query.CustomerCrowdQuery;
import com.och.calltask.domain.vo.CustomerCrowdVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import com.och.calltask.domain.entity.CustomerCrowd;

import java.util.List;

/**
 * 客户人群管理表(CustomerCrowd)表数据库访问层
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
@Repository()
@Mapper
public interface CustomerCrowdMapper extends BaseMapper<CustomerCrowd> {

    CustomerCrowdVo getDetail(@Param("id") Long id);

    List<CustomerCrowdVo> getList(CustomerCrowdQuery query);
}

