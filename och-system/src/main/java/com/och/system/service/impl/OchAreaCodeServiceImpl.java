package com.och.system.service.impl;

import com.och.common.base.BaseServiceImpl;
import com.och.system.domain.entity.OchAreaCode;
import com.och.system.mapper.OchAreaCodeMapper;
import com.och.system.service.IOchAreaCodeService;
import org.springframework.stereotype.Service;

/**
 * 基于location_gaode手工整理后的表(用于匹配区号)(OchAreaCode)表服务实现类
 *
 * @author danmo
 * @since 2025-05-26 17:10:03
 */
@Service
public class OchAreaCodeServiceImpl extends BaseServiceImpl<OchAreaCodeMapper, OchAreaCode> implements IOchAreaCodeService {

}

