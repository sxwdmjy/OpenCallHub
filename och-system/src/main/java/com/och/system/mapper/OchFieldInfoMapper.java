package com.och.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.system.domain.query.calltask.FieldQuery;
import com.och.system.domain.vo.calltask.FieldInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.system.domain.entity.OchFieldInfo;

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

