package com.och.calltask.service;

import com.och.calltask.domain.query.CallTaskAddQuery;
import com.och.calltask.domain.query.CallTaskContactImportQuery;
import com.och.calltask.domain.query.CallTaskContactQuery;
import com.och.calltask.domain.query.CallTaskQuery;
import com.och.calltask.domain.vo.CallTaskContactVo;
import com.och.calltask.domain.vo.CallTaskVo;
import com.och.common.base.IBaseService;
import com.och.calltask.domain.entity.CallTask;
import com.och.common.enums.CallTaskStatusEnum;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 外呼任务表(CallTask)表服务接口
 *
 * @author danmo
 * @since 2025-07-08 10:42:37
 */
public interface ICallTaskService extends IBaseService<CallTask> {

    void add(CallTaskAddQuery query);


    void edit(CallTaskAddQuery query);

    void detele(CallTaskQuery query);

    CallTaskVo getDetail(Long id);

    List<CallTaskVo> pageList(CallTaskQuery query);

    List<CallTaskVo> getList(CallTaskQuery query);

    void startTask(Long id);

    void pauseTask(Long id);

    void endTask(Long id);

    Boolean updateStatus(Long id, CallTaskStatusEnum callTaskStatusEnum);

    List<CallTaskContactVo> getTaskContactPageList(CallTaskContactQuery query);
    List<CallTaskContactVo> getTaskContactList(CallTaskContactQuery query);

    /**
     * 导入任务联系人
     *
     * @param query 查询参数
     * @param file  文件
     */
    void importTaskContact(CallTaskContactImportQuery query, MultipartFile file);
}

