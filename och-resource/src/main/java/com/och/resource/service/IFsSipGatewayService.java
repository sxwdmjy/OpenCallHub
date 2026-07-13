package com.och.system.service;

import com.och.common.base.IBaseService;
import com.och.system.domain.entity.FsSipGateway;
import com.och.system.domain.query.fssip.FsSipGatewayAddQuery;
import com.och.system.domain.query.fssip.FsSipGatewayQuery;

import java.util.List;

/**
 * SIP网关表(FsSipGateway)
 *
 * @author danmo
 * @date 2023-09-13 13:53:45
 */
public interface IFsSipGatewayService extends IBaseService<FsSipGateway> {

    void add(FsSipGatewayAddQuery query);

    void edit(FsSipGatewayAddQuery query);

    FsSipGateway getDetail(Long id);

    void delete(FsSipGatewayQuery query);

    List<FsSipGateway> getList(FsSipGatewayQuery query);

    List<FsSipGateway> getPageList(FsSipGatewayQuery query);

}
