package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.calltask.domain.query.CustomerFieldQuery;
import com.och.calltask.domain.vo.CustomerFieldVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.calltask.domain.entity.CustomerField;

import java.util.List;

/**
 * 客户字段管理表(CustomerField)表数据库访问层
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
@Repository()
@Mapper
public interface CustomerFieldMapper extends BaseMapper<CustomerField> {

    List<CustomerFieldVo> getList(CustomerFieldQuery query);
}

