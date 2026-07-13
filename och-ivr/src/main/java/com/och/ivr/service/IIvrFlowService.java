package com.och.ivr.service;

import com.och.ivr.domain.IvrFlow;

import java.util.List;

/**
 * IVR流程服务接口
 * <p>封装 IvrFlow 的 CRUD、发布和下线操作，Controller 通过本接口访问数据，不直接依赖 Mapper 层。</p>
 *
 * @author danmo
 */
public interface IIvrFlowService {

    /** 获取所有未删除的流程列表 */
    List<IvrFlow> list();

    /** 根据主键获取流程 */
    IvrFlow getById(Long id);

    /** 创建新流程 */
    IvrFlow create(IvrFlow flow);

    /** 更新流程 */
    IvrFlow update(Long id, IvrFlow flow);

    /** 逻辑删除流程 */
    void delete(Long id);

    /** 发布流程版本 */
    void publish(Long id);

    /** 下线已发布流程 */
    void offline(Long id);
}
