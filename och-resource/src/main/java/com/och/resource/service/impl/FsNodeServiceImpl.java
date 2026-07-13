package com.och.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.resource.domain.entity.FsNode;
import com.och.resource.mapper.FsNodeMapper;
import com.och.resource.service.IFsNodeService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * FreeSWITCH 节点管理服务实现
 * <p>管理 fs_node 表中 FreeSWITCH ESL 连接节点的增删改查。
 * 节点信息用于 EslNodeInitializer 在系统启动时自动建立 ESL 连接。</p>
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FsNodeServiceImpl implements IFsNodeService {

    private final FsNodeMapper fsNodeMapper;

    @Override
    public List<FsNode> getEnabledNodes() {
        return fsNodeMapper.findEnabledNodes();
    }

    @Override
    public List<FsNode> getAllNodes() {
        LambdaQueryWrapper<FsNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(FsNode::getEnabled).orderByAsc(FsNode::getNodeId);
        return fsNodeMapper.selectList(wrapper);
    }

    @Override
    public FsNode getById(Long id) {
        return fsNodeMapper.selectById(id);
    }

    @Override
    @Transactional
    public void add(FsNode node) {
        fsNodeMapper.insert(node);
        log.info("[FsNodeService] 新增节点: id={}, nodeId={}, host={}:{}", node.getId(), node.getNodeId(), node.getHost(), node.getPort());
    }

    @Override
    @Transactional
    public void update(FsNode node) {
        fsNodeMapper.updateById(node);
        log.info("[FsNodeService] 更新节点: id={}, nodeId={}", node.getId(), node.getNodeId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        fsNodeMapper.deleteById(id);
        log.info("[FsNodeService] 删除节点: id={}", id);
    }
}
