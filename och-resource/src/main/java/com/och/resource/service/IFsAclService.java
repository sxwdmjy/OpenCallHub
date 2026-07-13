package com.och.system.service;

import com.github.pagehelper.PageInfo;
import com.och.common.base.IBaseService;
import com.och.system.domain.entity.FsAcl;
import com.och.system.domain.query.acl.FsAclAddQuery;
import com.och.system.domain.query.acl.FsAclNodeAddQuery;
import com.och.system.domain.query.acl.FsAclQuery;
import com.och.system.domain.vo.acl.FsAclVo;

import java.util.List;

/**
 * fs访问控制表(FsAcl)
 *
 * @author danmo
 * @date 2023-09-13 13:53:45
 */
public interface IFsAclService extends IBaseService<FsAcl> {

    void addList(FsAclAddQuery query);

    void addNode(FsAclNodeAddQuery query);

    void editList(FsAclAddQuery query);

    void editNode(FsAclNodeAddQuery query);

    FsAcl getDetail(Long id);

    void delete(FsAclQuery query);

    PageInfo<FsAclVo> pageList(FsAclQuery query);

    List<FsAclVo> getList(FsAclQuery query);
}
