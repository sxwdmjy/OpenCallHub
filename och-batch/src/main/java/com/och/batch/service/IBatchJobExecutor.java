package com.och.batch.service;

import com.och.batch.domain.BatchJob;

public interface IBatchJobExecutor {
    void execute(BatchJob job);
    void retry(BatchJob job);
}
