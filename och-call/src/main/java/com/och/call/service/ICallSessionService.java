package com.och.call.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.call.domain.CallSession;

/**
 * 通话会话服务接口
 * <p>
 * 封装 CallSession 的查询操作，Controller 层通过本接口访问数据，
 * 不直接依赖 Mapper 层。复杂查询条件通过参数逐项传入，避免让 Controller 构造 LambdaQueryWrapper。
 * </p>
 *
 * @author danmo
 */
public interface ICallSessionService {

    /**
     * 分页查询通话会话列表
     *
     * @param pageNum      页码（从1开始）
     * @param pageSize     每页大小
     * @param state        会话状态（可选，为空则查询所有）
     * @param keyword      关键字（可选，匹配主被叫号码或 callId）
     * @return 分页结果
     */
    IPage<CallSession> pageList(int pageNum, int pageSize, String state, String keyword);

    /**
     * 根据主键查询通话会话
     *
     * @param id 主键ID
     * @return 会话实体，不存在返回 null
     */
    CallSession getById(Long id);
}