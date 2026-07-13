package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.system.domain.entity.SysOperLog;
import com.och.system.domain.query.BaseQuery;
import com.och.system.domain.query.log.SysOperationLogQuery;
import com.och.system.mapper.SysOperLogMapper;
import com.och.system.service.ISysOperLogService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统操作日志记录(SysOperLog)表服务实现类
 *
 * @author danmo
 * @since 2024-03-12 19:09:58
 */
@Service
public class SysOperLogServiceImpl extends BaseServiceImpl<SysOperLogMapper, SysOperLog> implements ISysOperLogService {

    @Override
    public List<SysOperLog> getList(SysOperationLogQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        return this.baseMapper.getList(query);
    }

    @Override
    public void delete(SysOperationLogQuery query) {
        if (CollectionUtil.isEmpty(query.getIds())) {
            throw new BizException(ErrorCode.BIZ_RESOURCE_ID_REQUIRED);
        }
        List<SysOperLog> operLogs = query.getIds().stream().map(id -> {
            SysOperLog operLog = new SysOperLog();
            operLog.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            operLog.setId(id);
            return operLog;
        }).collect(Collectors.toList());
        updateBatchById(operLogs,200);

    }

    @Override
    public void emptyOperation(BaseQuery query) {
        remove(new LambdaQueryWrapper<SysOperLog>().gt(SysOperLog::getId,0));
    }
}

