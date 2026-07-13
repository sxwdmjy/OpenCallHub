package com.och.system.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.system.domain.entity.FsAcl;
import com.och.system.domain.query.acl.FsAclQuery;
import com.och.system.domain.vo.acl.FsAclVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * fs访问控制表(FsAcl)
 *
 * @author danmo
 * @date 2023-09-13 13:53:45
 */
@Mapper
public interface FsAclMapper extends BaseMapper<FsAcl> {

    List<Long> getIdsByQuery(FsAclQuery query);

    List<FsAclVo> getList(FsAclQuery query);

}

