package com.och.resource.service;

import com.och.resource.domain.entity.FsNode;

import java.util.List;

/**
 * FreeSWITCH 节点管理服务接口
 * <p>提供 FreeSWITCH 节点的增删改查和启用/禁用操作。
 * 将 FreeSWITCH ESL 连接信息适配为 Java 实体，屏蔽底层通信协议的差异。</p>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li><b>适配器模式</b>：将 FreeSWITCH ESL 连接信息适配为 Java 实体对象，使上层业务代码无需感知底层 ESL 协议细节</li>
 * </ul>
 *
 * @author danmo
 */
public interface IFsNodeService {

    /** 查询所有已启用的节点 */
    List<FsNode> getEnabledNodes();

    /** 查询全部节点 */
    List<FsNode> getAllNodes();

    /** 根据 ID 查询 */
    FsNode getById(Long id);

    /** 新增节点 */
    void add(FsNode node);

    /** 更新节点 */
    void update(FsNode node);

    /** 删除节点（逻辑删除） */
    void delete(Long id);
}
