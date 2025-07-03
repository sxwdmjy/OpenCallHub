package com.och.calltask.listener;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.och.calltask.domain.CustomerCrowdEvent;
import com.och.calltask.domain.entity.CustomerCrowd;
import com.och.calltask.domain.entity.CustomerCrowdRel;
import com.och.calltask.domain.entity.CustomerSeas;
import com.och.calltask.domain.vo.CustomerCrowdVo;
import com.och.calltask.service.ICustomerCrowdRelService;
import com.och.calltask.service.ICustomerCrowdService;
import com.och.calltask.service.ICustomerSeasService;
import com.och.common.domain.ConditionInfo;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.enums.RelationEnum;
import com.och.common.utils.TraceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 客户群事件监听器
 *
 * @author danmo
 * @date 2025/7/2 22:19
 */

@RequiredArgsConstructor
@Slf4j
@Component
public class CustomerCrowdEventListener implements ApplicationListener<CustomerCrowdEvent> {

    private final ICustomerCrowdService iCustomerCrowdService;
    private final ICustomerSeasService iCustomerSeasService;
    private final ICustomerCrowdRelService iCustomerCrowdRelService;
    private static final String CUSTOMER_INFO_FIELD = "customer_info";

    @Override
    public void onApplicationEvent(CustomerCrowdEvent event) {
        try {
            TraceUtil.setTraceId(UUID.randomUUID().toString().replaceAll("-", ""));
            log.info("客户群事件监听器 crowdId:{}", event.getSource());

            CustomerCrowdVo customerCrowd = event.getCustomerCrowd();
            // 更新进度
            iCustomerCrowdService.update(new LambdaUpdateWrapper<CustomerCrowd>()
                    .eq(CustomerCrowd::getId, customerCrowd.getId())
                    .set(CustomerCrowd::getProgress, 2));

            //查看筛选条件
            List<ConditionInfo> swipe = customerCrowd.getSwipe();
            if (CollectionUtils.isEmpty(swipe)) {
                return;
            }
            Long lastId = 0L;
            int pageSize = 1000;
            int count = 0;
            //根据筛选条件查询客户公海
            while (true) {
                LambdaQueryWrapper<CustomerSeas> wrapper = buildQueryWrapper(swipe, lastId, pageSize);
                try {
                    List<CustomerSeas> customerSeasList = iCustomerSeasService.list(wrapper);
                    if (CollectionUtils.isEmpty(customerSeasList)) {
                        break;
                    }
                    List<Long> customerSeasIds = customerSeasList.stream().map(CustomerSeas::getId).toList();
                    // 批量处理保存
                    processAndSave(customerCrowd.getId(), customerSeasIds);
                    lastId = customerSeasIds.get(customerSeasIds.size() - 1);
                    count += customerSeasIds.size();
                } catch (Exception e) {
                    log.error("客户群事件监听器异常 crowdId:{}, lastId:{}", event.getSource(), lastId, e);
                }
            }

            iCustomerCrowdService.update(new LambdaUpdateWrapper<CustomerCrowd>()
                    .eq(CustomerCrowd::getId, customerCrowd.getId())
                    .set(CustomerCrowd::getProgress, 3)
                    .set(CustomerCrowd::getCrowdNum, count));
        } catch (Exception e) {
            log.error("客户群事件监听器异常 crowdId:{}", event.getSource(), e);
            CustomerCrowdVo customerCrowd = event.getCustomerCrowd();
            iCustomerCrowdService.update(new LambdaUpdateWrapper<CustomerCrowd>()
                    .eq(CustomerCrowd::getId, customerCrowd.getId())
                    .set(CustomerCrowd::getProgress, 4)
                    .set(CustomerCrowd::getReason, e.getMessage()));
        } finally {
            TraceUtil.clear();
        }

    }

    private LambdaQueryWrapper<CustomerSeas> buildQueryWrapper(List<ConditionInfo> swipe, Long lastId, int pageSize) {
        LambdaQueryWrapper<CustomerSeas> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(CustomerSeas::getId);
        wrapper.gt(CustomerSeas::getId, lastId);
        wrapper.last("limit " + pageSize);
        wrapper.orderByAsc(CustomerSeas::getId);
        wrapper.eq(CustomerSeas::getDelFlag, DeleteStatusEnum.DELETE_NO.getIndex());
        swipe.forEach(conditionInfo -> {
            RelationEnum anEnum = RelationEnum.getEnum(conditionInfo.getRelation());
            switch (anEnum) {
                case EQUAL, NOT_EQUAL, MORE_THAN, LESS_THAN, GREATER_EQUAL, LESS_EQUAL, INCLUDE,
                     NOT_INCLUDE -> {
                    String jsonSql = anEnum.getJsonFormat();
                    if (conditionInfo.getValue().size() == 1) {
                        wrapper.apply(jsonSql, CUSTOMER_INFO_FIELD, conditionInfo.getFieldName(), conditionInfo.getValue().get(0));
                    } else if (conditionInfo.getValue().size() > 1) {
                        wrapper.and(wrap ->
                                conditionInfo.getValue().forEach(val ->
                                        wrap.or(w -> w.apply(jsonSql, CUSTOMER_INFO_FIELD, conditionInfo.getFieldName(), val))
                                ));
                    }
                }
                case INTERVAL -> {
                    String jsonSql = anEnum.getJsonFormat();
                    wrapper.apply(jsonSql, CUSTOMER_INFO_FIELD, conditionInfo.getFieldName(), conditionInfo.getValue().get(0), conditionInfo.getValue().get(1));
                }
                case NULL, NOT_NULL -> {
                    String jsonSql = anEnum.getJsonFormat();
                    wrapper.apply(jsonSql, CUSTOMER_INFO_FIELD, conditionInfo.getFieldName());
                }
            }
        });
        return wrapper;
    }

    private void processAndSave(Long crowdId, List<Long> customerSeasIds) {
        if (CollectionUtils.isEmpty(customerSeasIds)) {
            return;
        }
        List<CustomerCrowdRel> rels = customerSeasIds.stream()
                .map(customerId -> {
                    CustomerCrowdRel rel = new CustomerCrowdRel();
                    rel.setCustomerId(customerId);
                    rel.setCrowdId(crowdId);
                    return rel;
                })
                .toList();

        iCustomerCrowdRelService.batchUpsert(rels);
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
