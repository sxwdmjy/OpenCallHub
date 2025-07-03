package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.calltask.domain.query.CustomerTemplateQuery;
import com.och.calltask.domain.vo.CustomerTemplateVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import com.och.calltask.domain.entity.CustomerTemplate;

import java.util.List;

/**
 * 客户模板管理表(CustomerTemplate)表数据库访问层
 *
 * @author danmo
 * @since 2025-06-30 11:35:44
 */
@Repository()
@Mapper
public interface CustomerTemplateMapper extends BaseMapper<CustomerTemplate> {

    CustomerTemplateVo getDetail(@Param("id") Long id);

    List<Long> getIdsByQuery(CustomerTemplateQuery query);

    List<CustomerTemplateVo> getList(CustomerTemplateQuery query);
}

