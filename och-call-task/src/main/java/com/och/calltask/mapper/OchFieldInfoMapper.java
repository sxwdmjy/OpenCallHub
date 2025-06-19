package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.calltask.domain.entity.OchFieldInfo;
import com.och.calltask.domain.query.FieldQuery;
import com.och.calltask.domain.vo.FieldInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字段管理表(OchFieldInfo)表数据库访问层
 *
 * @author danmo
 * @since 2025-06-16 14:53:21
 */
@Repository()
@Mapper
public interface OchFieldInfoMapper extends BaseMapper<OchFieldInfo> {

    List<FieldInfoVo> getList(FieldQuery query);
}

