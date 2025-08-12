package com.och.calltask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.och.calltask.domain.entity.CallTaskAssignment;

/**
 * 任务分配联系人表(CallTaskAssignment)表数据库访问层
 *
 * @author danmo
 * @since 2025-07-14 10:06:12
 */
@Repository()
@Mapper
public interface CallTaskAssignmentMapper extends BaseMapper<CallTaskAssignment> {

}

