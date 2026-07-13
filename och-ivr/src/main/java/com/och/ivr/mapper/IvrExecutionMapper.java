package com.och.ivr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.ivr.domain.IvrExecution;
import org.apache.ibatis.annotations.Mapper;

/**
 * IVR执行实例数据访问层
 *
 * @author danmo
 */
@Mapper
public interface IvrExecutionMapper extends BaseMapper<IvrExecution> {
}