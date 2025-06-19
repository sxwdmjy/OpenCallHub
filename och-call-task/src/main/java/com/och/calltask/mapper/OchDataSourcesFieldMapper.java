package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.calltask.domain.entity.OchDataSourcesField;
import com.och.calltask.domain.vo.FieldInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据源字段表(OchDataSourcesField)表数据库访问层
 *
 * @author danmo
 * @since 2025-06-16 16:08:38
 */
@Repository()
@Mapper
public interface OchDataSourcesFieldMapper extends BaseMapper<OchDataSourcesField> {

    List<FieldInfoVo> listBySourceId(Long sourceId);
}

