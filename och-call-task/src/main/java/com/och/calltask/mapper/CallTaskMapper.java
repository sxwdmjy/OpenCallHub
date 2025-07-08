package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.calltask.domain.query.CallTaskQuery;
import com.och.calltask.domain.vo.CallTaskVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import com.och.calltask.domain.entity.CallTask;

import java.util.List;

/**
 * 外呼任务表(CallTask)表数据库访问层
 *
 * @author danmo
 * @since 2025-07-08 10:42:34
 */
@Repository()
@Mapper
public interface CallTaskMapper extends BaseMapper<CallTask> {

    List<CallTaskVo> getList(CallTaskQuery query);
}

