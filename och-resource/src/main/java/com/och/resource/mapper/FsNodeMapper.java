package com.och.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.resource.domain.entity.FsNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * FreeSWITCH 节点表 Mapper
 * <p>提供 fs_node 表的基础 CRUD 和启用的节点查询。</p>
 *
 * @author danmo
 */
@Mapper
public interface FsNodeMapper extends BaseMapper<FsNode> {

    /**
     * 查询所有已启用的节点
     *
     * @return 已启用的 FreeSWITCH 节点列表
     */
    @Select("SELECT * FROM fs_node WHERE enabled = 1 AND del_flag = 0")
    List<FsNode> findEnabledNodes();
}
