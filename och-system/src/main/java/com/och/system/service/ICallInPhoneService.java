package com.och.system.service;

import com.och.common.base.IBaseService;
import com.och.system.domain.entity.CallInPhone;
import com.och.system.domain.query.callin.CallInPhoneAddQuery;
import com.och.system.domain.query.callin.CallInPhoneQuery;
import com.och.system.domain.vo.callin.CallInPhoneVo;

import java.util.List;

/**
 * 呼入号码表(CallInPhone)表服务接口
 *
 * @author danmo
 * @since 2024-12-10 10:29:11
 */
public interface ICallInPhoneService extends IBaseService<CallInPhone> {


    void add(CallInPhoneAddQuery query);

    void update(CallInPhoneAddQuery query);

    void delete(CallInPhoneQuery query);

    CallInPhoneVo getDetail(CallInPhoneQuery query);

    List<CallInPhoneVo> getPageList(CallInPhoneQuery query);

    CallInPhoneVo getDetailByPhone(String phone);

    List<CallInPhoneVo> getList(CallInPhoneQuery query);
}

