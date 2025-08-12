package com.och.calltask.listener;


import com.alibaba.fastjson2.JSONObject;
import com.och.calltask.domain.CallTaskContactImportEvent;
import com.och.calltask.domain.entity.CallTaskAssignment;
import com.och.calltask.domain.vo.CustomerSeasVo;
import com.och.calltask.service.ICallTaskAssignmentService;
import com.och.calltask.service.ICustomerSeasService;
import com.och.common.utils.TraceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * 任务联系人导入监听器
 *
 * @author danmo
 * @date 2025/7/2 22:19
 */

@RequiredArgsConstructor
@Slf4j
@Component
public class CallTaskContactImportEventListener implements ApplicationListener<CallTaskContactImportEvent> {

    private final ICustomerSeasService iCustomerSeasService;
    private final ICallTaskAssignmentService iCallTaskAssignmentService;


    @Override
    public void onApplicationEvent(CallTaskContactImportEvent event) {
        TraceUtil.setTraceId(UUID.randomUUID().toString().replaceAll("-", ""));
        try {
            log.info("任务联系人导入监听器 event:{}", JSONObject.toJSONString(event));
            if (Objects.equals(0, event.getType())) {
                crowdImport(event);
            } else if (Objects.equals(1, event.getType())) {
                fileImport(event);
            }
        } finally {
            TraceUtil.clear();
        }
    }


    private void crowdImport(CallTaskContactImportEvent event) {
        try {
            CustomerSeasVo customerSeas = iCustomerSeasService.getDetail(event.getCustomerId());
            CallTaskAssignment assignment = new CallTaskAssignment();
            assignment.setTaskId(event.getTaskId());
            assignment.setPhone(customerSeas.getPhone());
            assignment.setName(customerSeas.getName());
            assignment.setSex(customerSeas.getSex());
            assignment.setExt(customerSeas.getCustomerInfo().toJSONString());
            assignment.setSource(0);
            assignment.setCrowdId(event.getCrowdId());
            assignment.setStatus(0);
            iCallTaskAssignmentService.save(assignment);
        } catch (Exception e) {
            log.error("任务联系人导入监听器 crowdImport error:{}", e.getMessage(), e);
        }
    }

    private void fileImport(CallTaskContactImportEvent event) {
        try {
            JSONObject customerInfo = event.getCustomerInfo();
            CallTaskAssignment assignment = new CallTaskAssignment();
            assignment.setTaskId(event.getTaskId());
            assignment.setPhone(customerInfo.getString("phone"));
            assignment.setName(customerInfo.getString("name"));
            assignment.setSex(customerInfo.getIntValue("sex", 0));
            assignment.setExt(customerInfo.toJSONString());
            assignment.setSource(1);
            assignment.setTemplateId(event.getTemplateId());
            assignment.setStatus(0);
            iCallTaskAssignmentService.save(assignment);
        } catch (Exception e) {
            log.error("任务联系人导入监听器 fileImport error:{}", e.getMessage(), e);
        }
    }


    @Override
    public boolean supportsAsyncExecution() {
        return true;
    }
}
