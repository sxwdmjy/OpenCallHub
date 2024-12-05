package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.exception.CommonException;
import com.och.common.utils.MD5Utils;
import com.och.common.utils.StringUtils;
import com.och.system.domain.entity.KoSubscriber;
import com.och.system.domain.query.subsriber.KoSubscriberAddQuery;
import com.och.system.domain.query.subsriber.KoSubscriberBatchAddQuery;
import com.och.system.domain.query.subsriber.KoSubscriberQuery;
import com.och.system.domain.query.subsriber.KoSubscriberUpdateQuery;
import com.och.system.domain.vo.sip.KoSubscriberVo;
import com.och.system.mapper.KoSubscriberMapper;
import com.och.system.service.IKoSubscriberService;
import com.och.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (Subscriber)
 *
 * @author danmo
 * @date 2024-07-29 10:49:24
 */
@Service
public class KoSubscriberServiceImpl extends BaseServiceImpl<KoSubscriberMapper, KoSubscriber> implements IKoSubscriberService {

    @Autowired
    private ISysUserService sysUserService;

    @Override
    public void add(KoSubscriberAddQuery query) {
        Boolean checked = checkUserName(query.getUsername());
        if (checked) {
            throw new CommonException("账号已存在");
        }
        KoSubscriber subscriber = new KoSubscriber();
        subscriber.setUsername(query.getUsername());
        subscriber.setPassword(query.getPassword());
        subscriber.setStatus(query.getStatus());
        subscriber.setDomain(query.getDomain());
        if(StringUtils.isNotBlank(query.getDomain()) && StringUtils.isBlank(query.getHa1())){
            String ha1 = MD5Utils.MD5(query.getUsername() + ":" + query.getDomain() + ":" + subscriber.getPassword());
            query.setHa1(ha1);
        }
        if(StringUtils.isNotBlank(query.getDomain()) && StringUtils.isBlank(query.getHa1b())){
            String ha1b = MD5Utils.MD5(query.getUsername() + ":" + query.getDomain() + ":" + subscriber.getPassword());
            query.setHa1b(ha1b);
        }
        subscriber.setHa1(query.getHa1());
        subscriber.setHa1b(query.getHa1b());
        subscriber.setVmpin(query.getVmpin());
        save(subscriber);
    }

    private Boolean checkUserName(String username) {
        KoSubscriber subscriber = getByUserName(username);
        return Objects.nonNull(subscriber);
    }

    @Override
    public void batchAdd(KoSubscriberBatchAddQuery query) {
        int initNum = query.getInitNum();

        String prefix = StringUtils.rightPad("", 4, "0");

        List<KoSubscriber> subscriberList = new LinkedList<>();
        for (int i = 0; i < query.getNumber(); i++) {
            KoSubscriber subscriber = new KoSubscriber();
            subscriber.setUsername(prefix + initNum);
            if (StringUtils.isBlank(query.getPassword())) {
                subscriber.setPassword(RandomUtil.randomStringWithoutStr(8, ""));
            } else {
                subscriber.setPassword(query.getPassword());
            }
            initNum++;
            subscriberList.add(subscriber);
        }
        if (CollectionUtil.isNotEmpty(subscriberList)) {
            List<String> userNameList = subscriberList.stream().map(KoSubscriber::getUsername).collect(Collectors.toList());
            List<KoSubscriber> nameList = getByUserNameList(userNameList);
            if (CollectionUtil.isNotEmpty(nameList)) {
                throw new CommonException("账号已存在");
            }
        }
        saveBatch(subscriberList);
    }

    @Override
    public void edit(KoSubscriberUpdateQuery query) {
        KoSubscriber subscriber = getById(query.getId());
        if (Objects.isNull(subscriber)){
            throw new CommonException("无效ID");
        }
        if(!StringUtils.equals(subscriber.getUsername(), query.getUsername()) && checkUserName(query.getUsername())){
            throw new CommonException("账号已存在");
        }else if (StringUtils.isNotBlank(query.getUsername())) {
            subscriber.setUsername(query.getUsername());
        }

        if(StringUtils.isNotBlank(query.getDomain()) && StringUtils.isBlank(query.getHa1())){
            String ha1 = MD5Utils.MD5(query.getUsername() + ":" + query.getDomain() + ":" + subscriber.getPassword());
            query.setHa1(ha1);
        }
        if(StringUtils.isNotBlank(query.getDomain()) && StringUtils.isBlank(query.getHa1b())){
            String ha1b = MD5Utils.MD5(query.getUsername() + ":" + query.getDomain() + ":" + subscriber.getPassword());
            query.setHa1b(ha1b);
        }

        if (StringUtils.isNotBlank(query.getPassword())) {
            subscriber.setPassword(query.getPassword());
        }
        if (StringUtils.isNotBlank(query.getDomain())) {
            subscriber.setDomain(query.getDomain());
        }
        if (StringUtils.isNotBlank(query.getHa1())) {
            subscriber.setHa1(query.getHa1());
        }
        if (StringUtils.isNotBlank(query.getHa1b())) {
            subscriber.setHa1b(query.getHa1b());
        }
        if (StringUtils.isNotBlank(query.getVmpin())) {
            subscriber.setVmpin(query.getVmpin());
        }
        if (Objects.nonNull(query.getStatus())) {
            subscriber.setStatus(query.getStatus());
        }
        updateById(subscriber);
    }

    @Override
    public KoSubscriber getDetail(Integer id) {
        return getById(id);
    }

    @Override
    public void delete(KoSubscriberQuery query) {
        remove(new LambdaQueryWrapper<KoSubscriber>().eq(KoSubscriber::getId, query.getId()));
    }

    @Override
    public List<KoSubscriberVo> getList(KoSubscriberQuery query) {
        return this.baseMapper.getList(query);
    }

    @Override
    public KoSubscriber getByUserName(String username) {
        return this.baseMapper.getByUserName(username);
    }

    @Override
    public List<KoSubscriberVo> getPageList(KoSubscriberQuery query) {
        startPage(query.getPageIndex(), query.getPageSize());
        List<KoSubscriberVo> list = getList(query);
        if (CollectionUtil.isNotEmpty(list)){
            sysUserService.decorate(list);
        }
        return list;
    }

    public List<KoSubscriber> getByUserNameList(List<String> userNameList) {
        return this.baseMapper.getByUserNameList(userNameList);
    }
}
