package com.och.system.service;

import com.och.common.base.IBaseService;
import com.och.system.domain.entity.PhoneLocation;

/**
 * (PhoneLocation)表服务接口
 *
 * @author danmo
 * @since 2025-05-26 17:12:25
 */
public interface IPhoneLocationService extends IBaseService<PhoneLocation> {

    /**
     * 获取号码归属地
     * @param phone 号码
     * @return 归属地
     */
    String getLocation(String phone);
}

