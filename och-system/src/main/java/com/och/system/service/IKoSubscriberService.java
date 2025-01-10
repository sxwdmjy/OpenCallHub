package com.och.system.service;


import com.och.common.base.IBaseService;
import com.och.system.domain.entity.KoSubscriber;
import com.och.system.domain.query.subsriber.KoSubscriberAddQuery;
import com.och.system.domain.query.subsriber.KoSubscriberBatchAddQuery;
import com.och.system.domain.query.subsriber.KoSubscriberQuery;
import com.och.system.domain.query.subsriber.KoSubscriberUpdateQuery;
import com.och.system.domain.vo.sip.KoSubscriberVo;
import com.och.system.domain.vo.sip.SipSimpleVo;

import java.util.List;

/**
 * (Subscriber)
 *
 * @author danmo
 * @date 2024-07-29 10:49:24
 */
public interface IKoSubscriberService extends IBaseService<KoSubscriber> {

    void add(KoSubscriberAddQuery query);

    void batchAdd(KoSubscriberBatchAddQuery query);

    void edit(KoSubscriberUpdateQuery query);

    KoSubscriber getDetail(Integer id);

    void delete(KoSubscriberQuery query);

    List<KoSubscriberVo> getList(KoSubscriberQuery query);

    KoSubscriber getByUserName(String username);


    List<KoSubscriberVo> getPageList(KoSubscriberQuery query);

    List<SipSimpleVo> selectList();

}
