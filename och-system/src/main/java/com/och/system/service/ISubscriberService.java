package com.och.system.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.och.common.base.IBaseService;
import com.och.system.domain.entity.Subscriber;
import com.och.system.domain.query.subsriber.SubscriberAddQuery;
import com.och.system.domain.query.subsriber.SubscriberBatchAddQuery;
import com.och.system.domain.query.subsriber.SubscriberQuery;
import com.och.system.domain.query.subsriber.SubscriberUpdateQuery;
import com.och.system.domain.vo.sip.SubscriberVo;

import java.util.List;

/**
 * (Subscriber)
 *
 * @author danmo
 * @date 2024-07-29 10:49:24
 */
public interface ISubscriberService extends IBaseService<Subscriber> {

    void add(SubscriberAddQuery query);

    void batchAdd(SubscriberBatchAddQuery query);

    void edit(SubscriberUpdateQuery query);

    Subscriber getDetail(Integer id);

    void delete(SubscriberQuery query);

    List<SubscriberVo> getList(SubscriberQuery query);

    Subscriber getByUserName(String username);


    List<SubscriberVo> getPageList(SubscriberQuery query);
}
