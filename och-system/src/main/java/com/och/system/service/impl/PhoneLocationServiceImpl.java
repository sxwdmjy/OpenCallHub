package com.och.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.utils.PhoneUtils;
import com.och.system.domain.entity.OchRegion;
import com.och.system.mapper.PhoneLocationMapper;
import com.och.system.domain.entity.PhoneLocation;
import com.och.system.service.IOchRegionService;
import com.och.system.service.IPhoneLocationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * (PhoneLocation)表服务实现类
 *
 * @author danmo
 * @since 2025-05-26 17:12:25
 */
@AllArgsConstructor
@Service
public class PhoneLocationServiceImpl extends BaseServiceImpl<PhoneLocationMapper, PhoneLocation> implements IPhoneLocationService {

    private final IOchRegionService ochRegionService;

    @Override
    public String getLocation(String phone) {
        String phoneArea = PhoneUtils.getPhoneArea(phone);
        if (phoneArea != null){
            PhoneLocation phoneLocation = this.getOne(new LambdaQueryWrapper<PhoneLocation>()
                    .eq(PhoneLocation::getPhone, phoneArea).last("limit 1"));
            if (phoneLocation != null){
                OchRegion ochRegion = ochRegionService.getOne(new LambdaQueryWrapper<OchRegion>().eq(OchRegion::getRegionId, phoneLocation.getAreaCode()).last("limit 1"));
                if (ochRegion != null){
                    return ochRegion.getMergerName();
                }
            }
        }
        return "";
    }
}

