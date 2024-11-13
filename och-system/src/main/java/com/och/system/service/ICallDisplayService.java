package com.och.system.service;


import com.och.common.base.IBaseService;
import com.och.system.domain.entity.CallDisplay;
import com.och.system.domain.query.display.CallDisplayAddQuery;
import com.och.system.domain.query.display.CallDisplayQuery;
import com.och.system.domain.vo.display.CallDisplayVo;

import java.util.List;

/**
 * 号码管理(LfsDisplay)表服务接口
 *
 * @author danmo
 * @since 2023-10-23 10:45:58
 */
public interface ICallDisplayService extends IBaseService<CallDisplay> {

    void add(CallDisplayAddQuery query);

    void edit(CallDisplayAddQuery query);

    CallDisplayVo getDetail(Long id);

    void delete(CallDisplayQuery query);

    List<CallDisplayVo> getList(CallDisplayQuery query);

    void allocate(CallDisplayQuery query);

    List<CallDisplayVo> getPageList(CallDisplayQuery query);
}

