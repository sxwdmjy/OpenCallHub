package com.och.ai.service.impl;

import com.och.common.base.BaseServiceImpl;
import com.och.ai.mapper.IntentVectorMapper;
import com.och.ai.domain.IntentVector;
import com.och.ai.service.IIntentVectorService;
import org.springframework.stereotype.Service;

/**
 * 意图向量表(IntentVector)表服务实现类
 *
 * @author danmo
 * @since 2025-10-17 14:54:11
 */
@Service
public class IntentVectorServiceImpl extends BaseServiceImpl<IntentVectorMapper, IntentVector> implements IIntentVectorService {

}

