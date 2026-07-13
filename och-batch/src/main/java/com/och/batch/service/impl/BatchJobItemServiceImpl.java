package com.och.batch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.och.batch.domain.BatchJobItem;
import com.och.batch.domain.ItemState;
import com.och.batch.mapper.BatchJobItemMapper;
import com.och.batch.service.IBatchJobItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批处理条目明细服务。
 * <p>
 * 提供条目级别的查询、创建、批量保存和状态更新功能。
 * 任务执行引擎通过此服务追踪每个导入/导出条目的处理结果。
 * </p>
 */
@Slf4j
@Service
public class BatchJobItemServiceImpl extends ServiceImpl<BatchJobItemMapper, BatchJobItem> implements IBatchJobItemService {

    /**
     * 分页查询任务条目明细。
     *
     * @param jobId    任务ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页条目列表，按行号升序排列
     */
    public IPage<BatchJobItem> listItems(Long jobId, int pageNum, int pageSize) {
        LambdaQueryWrapper<BatchJobItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BatchJobItem::getJobId, jobId);
        wrapper.orderByAsc(BatchJobItem::getRowIndex);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 查询任务下所有失败条目。
     *
     * @param jobId 任务ID
     * @return 失败条目列表（含 FAILED 和 FAILED_RETRY 状态）
     */
    public List<BatchJobItem> listFailedItems(Long jobId) {
        LambdaQueryWrapper<BatchJobItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BatchJobItem::getJobId, jobId);
        wrapper.in(BatchJobItem::getState, ItemState.FAILED.name(), ItemState.FAILED_RETRY.name());
        wrapper.orderByAsc(BatchJobItem::getRowIndex);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 查询任务下待重试的失败条目。
     *
     * @param jobId 任务ID
     * @return 需要重试的条目列表（FAILED_RETRY 状态）
     */
    public List<BatchJobItem> listRetryItems(Long jobId) {
        LambdaQueryWrapper<BatchJobItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BatchJobItem::getJobId, jobId);
        wrapper.eq(BatchJobItem::getState, ItemState.FAILED_RETRY.name());
        return baseMapper.selectList(wrapper);
    }

    /**
     * 创建单个条目记录。
     */
    @Transactional
    public void createItem(Long jobId, int rowIndex, String itemKey, String rawData) {
        BatchJobItem item = new BatchJobItem();
        item.setJobId(jobId);
        item.setRowIndex(rowIndex);
        item.setItemKey(itemKey);
        item.setRawData(rawData);
        item.setState(ItemState.PENDING.name());
        baseMapper.insert(item);
    }

    /**
     * 标记条目为成功。
     */
    @Transactional
    public void markSuccess(Long itemId) {
        BatchJobItem item = new BatchJobItem();
        item.setId(itemId);
        item.setState(ItemState.SUCCESS.name());
        item.setProcessedAt(LocalDateTime.now());
        baseMapper.updateById(item);
    }

    /**
     * 标记条目为失败。
     */
    @Transactional
    public void markFailed(Long itemId, String errorMessage) {
        BatchJobItem item = new BatchJobItem();
        item.setId(itemId);
        item.setState(ItemState.FAILED.name());
        item.setErrorMessage(errorMessage);
        item.setProcessedAt(LocalDateTime.now());
        baseMapper.updateById(item);
    }

    /**
     * 标记条目为待重试。
     */
    @Transactional
    public void markRetry(Long itemId) {
        BatchJobItem item = new BatchJobItem();
        item.setId(itemId);
        item.setState(ItemState.FAILED_RETRY.name());
        baseMapper.updateById(item);
    }

    /**
     * 统计任务下各状态的条目数。
     */
    public long countByState(Long jobId, ItemState state) {
        LambdaQueryWrapper<BatchJobItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BatchJobItem::getJobId, jobId);
        wrapper.eq(BatchJobItem::getState, state.name());
        return baseMapper.selectCount(wrapper);
    }
}
