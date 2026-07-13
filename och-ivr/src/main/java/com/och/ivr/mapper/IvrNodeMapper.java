package com.och.ivr.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.ivr.domain.IvrNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * IVR节点数据访问层
 *
 * @author danmo
 */
@Mapper
public interface IvrNodeMapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<IvrNode> {

    /** 按版本ID统计节点数 */
    default long countByVersionId(Long versionId) {
        return selectCount(new LambdaQueryWrapper<IvrNode>().eq(IvrNode::getVersionId, versionId));
    }

    /** 按版本ID查询所有节点 */
    default java.util.List<IvrNode> findByVersionId(Long versionId) {
        return selectList(new LambdaQueryWrapper<IvrNode>().eq(IvrNode::getVersionId, versionId));
    }
}