package com.och.system.service;


import com.och.common.base.IBaseService;
import com.och.system.domain.entity.FsConfig;
import com.och.system.domain.query.fsconfig.FsConfigAddQuery;
import com.och.system.domain.query.fsconfig.FsConfigQuery;

import java.util.List;

/**
 * fs管理配置表(FsConfig)表服务接口
 *
 * @author danmo
 * @since 2023-10-17 11:04:58
 */
public interface IFsConfigService extends IBaseService<FsConfig> {

    void updateStatusById(Integer id, Integer status);

    void add(FsConfigAddQuery query);

    void edit(FsConfigAddQuery query);

    FsConfig getDetail(Integer id);

    void delete(FsConfigQuery query);

    List<FsConfig> getList(FsConfigQuery query);
    List<FsConfig> getPageList(FsConfigQuery query);
}

