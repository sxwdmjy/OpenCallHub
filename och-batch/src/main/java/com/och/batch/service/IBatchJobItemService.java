package com.och.batch.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.batch.domain.BatchJobItem;
import com.och.batch.domain.ItemState;

import java.util.List;

public interface IBatchJobItemService {
    IPage<BatchJobItem> listItems(Long jobId, int pageNum, int pageSize);
    List<BatchJobItem> listFailedItems(Long jobId);
    List<BatchJobItem> listRetryItems(Long jobId);
    void createItem(Long jobId, int rowIndex, String itemKey, String rawData);
    void markSuccess(Long itemId);
    void markFailed(Long itemId, String errorMessage);
    void markRetry(Long itemId);
    long countByState(Long jobId, ItemState state);
}
