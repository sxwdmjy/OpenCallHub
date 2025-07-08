package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.calltask.domain.entity.CustomerTemplateFieldRel;

/**
 * 客户模板字段关联表(CustomerTemplateFieldRel)表数据库访问层
 *
 * @author danmo
 * @since 2025-06-30 11:35:45
 */
@Repository()
@Mapper
public interface CustomerTemplateFieldRelMapper extends BaseMapper<CustomerTemplateFieldRel> {

}

