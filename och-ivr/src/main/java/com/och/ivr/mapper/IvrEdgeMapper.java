package com.och.ivr.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.ivr.domain.IvrEdge;
import org.apache.ibatis.annotations.Mapper;

/**
 * IVR边数据访问层
 *
 * @author danmo
 */
@Mapper
public interface IvrEdgeMapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<IvrEdge> {

    /** 按版本ID查询所有边 */
    default java.util.List<IvrEdge> findByVersionId(Long versionId) {
        return selectList(new LambdaQueryWrapper<IvrEdge>().eq(IvrEdge::getVersionId, versionId));
    }

    /** 按版本ID和源节点ID查询出边 */
    default java.util.List<IvrEdge> findByVersionAndSourceNode(Long versionId, String sourceNodeId) {
        return selectList(new LambdaQueryWrapper<IvrEdge>()
                .eq(IvrEdge::getVersionId, versionId)
                .eq(IvrEdge::getSourceNodeId, sourceNodeId));
    }
}