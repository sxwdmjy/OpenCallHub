package com.och.system.service;

import com.och.common.base.IBaseService;
import com.och.system.domain.entity.FsDialplan;
import com.och.system.domain.query.dialplan.FsDialplanAddQuery;
import com.och.system.domain.query.dialplan.FsDialplanQuery;
import com.och.system.domain.vo.dialplan.FsDialplanVo;

import java.util.List;

/**
 * fs拨号计划表(FsDialplan)
 *
 * @author danmo
 * @date 2023-09-15 11:04:20
 */
public interface IFsDialplanService extends IBaseService<FsDialplan> {

    void add(FsDialplanAddQuery query);

    void edit(FsDialplanAddQuery query);

    FsDialplan getDetail(Long id);

    void delete(FsDialplanQuery query);

    List<FsDialplanVo> getList(FsDialplanQuery query);

    List<FsDialplanVo> getPageList(FsDialplanQuery query);
}
