package com.och.ivr.application.node;

import com.och.ivr.domain.IvrEdge;
import com.och.ivr.domain.IvrNode;

import java.util.List;
import java.util.Map;

/**
 * IVR节点处理器接口（策略模式）
 * <p>
 * 每种 IVR 节点类型对应一个处理器实现，负责节点的路由决策和动作执行。
 * 通过工厂根据节点类型获取对应处理器，替代大段 switch-case 分支。
 * </p>
 *
 * @author danmo
 */
public interface IvrNodeHandler {

    /**
     * 处理当前节点并返回执行结果
     *
     * @param currentNode 当前节点
     * @param dtmfInput   用户DTMF输入（MENU节点使用，其他节点可为null）
     * @param edges       当前节点的所有出边
     * @param variables   上下文变量（可被修改，如TRANSFER节点设置transferTarget）
     * @return 执行结果，包含动作标记、下一节点ID和更新后的变量
     */
    IvrNodeHandlerResult handle(IvrNode currentNode, String dtmfInput,
                                List<IvrEdge> edges, Map<String, Object> variables);
}