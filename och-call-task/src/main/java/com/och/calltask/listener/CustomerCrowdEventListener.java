package com.och.calltask.listener;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.och.calltask.domain.CustomerCrowdEvent;
import com.och.calltask.domain.CustomerCrowdEventParam;
import com.och.calltask.domain.entity.CustomerCrowd;
import com.och.calltask.domain.entity.CustomerCrowdRel;
import com.och.calltask.domain.entity.CustomerSeas;
import com.och.calltask.domain.query.CustomerCrowdQuery;
import com.och.calltask.domain.vo.CustomerCrowdVo;
import com.och.calltask.domain.vo.CustomerSeasVo;
import com.och.calltask.service.ICustomerCrowdRelService;
import com.och.calltask.service.ICustomerCrowdService;
import com.och.calltask.service.ICustomerSeasService;
import com.och.common.base.BaseEntity;
import com.och.common.domain.ConditionInfo;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.enums.RelationEnum;
import com.och.common.utils.StringUtils;
import com.och.common.utils.TraceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        TraceUtil.setTraceId(UUID.randomUUID().toString().replaceAll("-", ""));
        try {
            log.info("客户群事件监听器 event:{}", JSONObject.toJSONString(event.getSource()));
            CustomerCrowdEventParam param = event.getCustomerCrowd();

            if(param.getEventType() == 1){
                //新增客户到人群
                addCustomerToCrowd(param.getCustomerId());
            }else if (param.getEventType() == 2){
                //从人群删除客户
                removeCustomerFromCrowd(param.getCustomerId());
            }else if (param.getEventType() == 3){
                //计算客户群数据
                calculationCrowdCustomer(param);
            }
        } finally {
            TraceUtil.clear();
        }
    }

    private void addCustomerToCrowd(Long customerId) {
        CustomerSeasVo customerSeas = iCustomerSeasService.getDetail(customerId);
        if(Objects.isNull(customerSeas)){
            log.info("客户不存在 customerId:{}", customerId);
            return;
        }
        //查询人群列表
        CustomerCrowdQuery crowdQuery = new CustomerCrowdQuery();
        crowdQuery.setStatus(1);
        crowdQuery.setType(2);
        List<CustomerCrowdVo> customerCrowdList = iCustomerCrowdService.getList(crowdQuery);
        if(CollectionUtils.isEmpty(customerCrowdList)){
            log.info("没有客户群 customerId:{}", customerId);
            return;
        }

        List<Long> crowdIdList = new ArrayList<>();
        //判断是否符合人群筛选条件
        for (CustomerCrowdVo customerCrowd : customerCrowdList) {
            boolean flag = false;
            JSONObject customerInfo = customerSeas.getCustomerInfo();
            List<ConditionInfo> swipe = customerCrowd.getSwipe();
            if(CollectionUtils.isEmpty(swipe)){
                continue;
            }
            for (ConditionInfo conditionInfo : swipe) {
                try {
                    String fieldName = conditionInfo.getFieldName();
                    RelationEnum anEnum = RelationEnum.getEnum(conditionInfo.getRelation());
                    if(customerInfo.containsKey(fieldName)){
                        switch (anEnum){
                            case EQUAL ->{
                                if(conditionInfo.getValue().size() == 1){
                                    if(customerInfo.getString(fieldName).equals(conditionInfo.getValue().get(0))){
                                        flag = true;
                                    }else {
                                        flag = false;
                                    }
                                }else if(conditionInfo.getValue().size() > 1 ){
                                    for (String value : conditionInfo.getValue()) {
                                        if(customerInfo.containsKey(fieldName) && customerInfo.getString(fieldName).equals(value)){
                                            flag = true;
                                        }else {
                                            flag = false;
                                        }
                                    }
                                }

                            }
                            case NOT_EQUAL ->{
                                if(conditionInfo.getValue().size() == 1 ){
                                    if(!customerInfo.getString(fieldName).equals(conditionInfo.getValue().get(0))){
                                        flag = true;
                                    }else {
                                        flag = false;
                                    }
                                }else if(conditionInfo.getValue().size() > 1 ){
                                    for (String value : conditionInfo.getValue()) {
                                        if(customerInfo.containsKey(fieldName) && !customerInfo.getString(fieldName).equals(value)){
                                            flag = true;
                                        }else {
                                            flag = false;
                                        }
                                    }
                                }
                            }
                            case MORE_THAN ->{
                                if(conditionInfo.getValue().size() == 1 ){
                                    if(customerInfo.getInteger(fieldName).compareTo(Integer.valueOf(conditionInfo.getValue().get(0))) > 0){
                                        flag = true;
                                    }else {
                                        flag = false;
                                    }
                                }else if(conditionInfo.getValue().size() > 1 ){
                                    for (String value : conditionInfo.getValue()) {
                                        if(customerInfo.containsKey(fieldName) && customerInfo.getInteger(fieldName).compareTo(Integer.valueOf(value)) > 0){
                                            flag = true;
                                        }else {
                                            flag = false;
                                        }
                                    }
                                }
                            }
                            case GREATER_EQUAL ->{
                                if(conditionInfo.getValue().size() == 1 ){
                                    if(customerInfo.getInteger(fieldName).compareTo(Integer.valueOf(conditionInfo.getValue().get(0))) >= 0){
                                        flag = true;
                                    }else {
                                        flag = false;
                                    }
                                }else if(conditionInfo.getValue().size() > 1 ){
                                    for (String value : conditionInfo.getValue()) {
                                        if(customerInfo.containsKey(fieldName) && customerInfo.getInteger(fieldName).compareTo(Integer.valueOf(value)) >= 0){
                                            flag = true;
                                        }else {
                                            flag = false;
                                        }
                                    }
                                }
                            }
                            case LESS_THAN ->{
                                if(conditionInfo.getValue().size() == 1 ){
                                    if(customerInfo.getInteger(fieldName).compareTo(Integer.valueOf(conditionInfo.getValue().get(0))) < 0){
                                        flag = true;
                                    }else {
                                        flag = false;
                                    }
                                }else if(conditionInfo.getValue().size() > 1 ){
                                    for (String value : conditionInfo.getValue()) {
                                        if(customerInfo.containsKey(fieldName) && customerInfo.getInteger(fieldName).compareTo(Integer.valueOf(value)) < 0){
                                            flag = true;
                                        }else {
                                            flag = false;
                                        }
                                    }
                                }
                            }
                            case LESS_EQUAL ->{
                                if(conditionInfo.getValue().size() == 1 ){
                                    if(customerInfo.getInteger(fieldName).compareTo(Integer.valueOf(conditionInfo.getValue().get(0))) <= 0){
                                        flag = true;
                                    }else {
                                        flag = false;
                                    }
                                }else if(conditionInfo.getValue().size() > 1 ){
                                    for (String value : conditionInfo.getValue()) {
                                        if(customerInfo.containsKey(fieldName) && customerInfo.getInteger(fieldName).compareTo(Integer.valueOf(value)) <= 0){
                                            flag = true;
                                        }else {
                                            flag = false;
                                        }
                                    }
                                }
                            }
                            case INTERVAL ->{
                                if(conditionInfo.getValue().size() == 2 ){
                                    if(customerInfo.getInteger(fieldName).compareTo(Integer.valueOf(conditionInfo.getValue().get(0))) >= 0
                                            && customerInfo.getInteger(fieldName).compareTo(Integer.valueOf(conditionInfo.getValue().get(1))) <= 0){
                                        flag = true;
                                    }else {
                                        flag = false;
                                    }
                                }
                            }
                            case NULL ->{
                                if(StringUtils.isNull(customerInfo.getString(fieldName))){
                                    flag = true;
                                }else {
                                    flag = false;
                                }
                            }
                            case NOT_NULL ->{
                                if(StringUtils.isNotBlank(customerInfo.getString(fieldName))){
                                    flag = true;
                                }else {
                                    flag = false;
                                }
                            }
                            case INCLUDE ->{
                                if(conditionInfo.getValue().size() == 1){
                                    if(customerInfo.getString(fieldName).contains(conditionInfo.getValue().get(0))){
                                        flag = true;
                                    }else {
                                        flag = false;
                                    }
                                }else if(conditionInfo.getValue().size() > 1){
                                    for (String value : conditionInfo.getValue()) {
                                        if(customerInfo.containsKey(fieldName)){
                                            if(customerInfo.getString(fieldName).contains(value)){
                                                flag = true;
                                            }else {
                                                flag = false;
                                            }
                                        }
                                    }
                                }
                            }
                            case NOT_INCLUDE ->{
                                if(conditionInfo.getValue().size() == 1){
                                    if(!customerInfo.getString(fieldName).contains(conditionInfo.getValue().get(0))){
                                        flag = true;
                                    }else {
                                        flag = false;
                                    }
                                }else if(conditionInfo.getValue().size() > 1){
                                    for (String value : conditionInfo.getValue()) {
                                        if(customerInfo.containsKey(fieldName)){
                                            if(!customerInfo.getString(fieldName).contains(value)){
                                                flag = true;
                                            }else {
                                                flag = false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }else {
                        flag = false;
                    }
                } catch (Exception e) {
                    log.error("addCustomerToCrowd客户人群任务执行异常 customerId:{}", customerId,e);
                }
            }
            if(flag){
                crowdIdList.add(customerCrowd.getId());
            }
        }
        processAndSave(crowdIdList, customerSeas.getId());

    }

    private void removeCustomerFromCrowd(Long customerId) {
        CustomerCrowdRel customerCrowdRel = new CustomerCrowdRel();
        customerCrowdRel.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
        iCustomerCrowdRelService.update(customerCrowdRel,new LambdaQueryWrapper<CustomerCrowdRel>().eq(CustomerCrowdRel::getCustomerId,customerId).eq(BaseEntity::getDelFlag,DeleteStatusEnum.DELETE_NO.getIndex()));
    }

    private void calculationCrowdCustomer(CustomerCrowdEventParam param) {
        try {
            CustomerCrowdVo customerCrowd = iCustomerCrowdService.getDetail(param.getCrowdId());
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
                    log.error("客户群事件监听器异常 crowdId:{}, lastId:{}", param.getCrowdId(), lastId, e);
                }
            }

            iCustomerCrowdService.update(new LambdaUpdateWrapper<CustomerCrowd>()
                    .eq(CustomerCrowd::getId, customerCrowd.getId())
                    .set(CustomerCrowd::getProgress, 3)
                    .set(CustomerCrowd::getCrowdNum, count));
        } catch (Exception e) {
            log.error("客户群事件监听器异常 event:{}", JSONObject.toJSONString(param), e);
            CustomerCrowdVo customerCrowd = iCustomerCrowdService.getDetail(param.getCrowdId());
            iCustomerCrowdService.update(new LambdaUpdateWrapper<CustomerCrowd>()
                    .eq(CustomerCrowd::getId, customerCrowd.getId())
                    .set(CustomerCrowd::getProgress, 4)
                    .set(CustomerCrowd::getReason, e.getMessage()));
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

    private void processAndSave(List<Long> crowdIds, Long customerId) {
        if (CollectionUtils.isEmpty(crowdIds)) {
            return;
        }
        List<CustomerCrowdRel> rels = crowdIds.stream()
                .map(crowdId -> {
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
