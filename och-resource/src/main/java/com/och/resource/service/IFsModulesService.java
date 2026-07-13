package com.och.system.service;

import com.och.common.base.IBaseService;
import com.och.system.domain.entity.FsModules;
import com.och.system.domain.query.modules.FsModulesAddQuery;
import com.och.system.domain.query.modules.FsModulesQuery;

import java.util.List;

/**
 * fs模块管理表(FsModules)
 *
 * @author danmo
 * @date 2023-09-15 15:42:28
 */
public interface IFsModulesService extends IBaseService<FsModules> {

    void add(FsModulesAddQuery query);

    void edit(FsModulesAddQuery query);

    FsModules getDetail(Long id);

    void delete(FsModulesQuery query);

    List<FsModules> getList(FsModulesQuery query);

    FsModules getModuleByName(String name);

    List<FsModules> getPageList(FsModulesQuery query);
}
