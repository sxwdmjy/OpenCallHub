package com.och.calltask.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.fastjson2.JSON;
import com.och.calltask.domain.entity.CustomerSeas;
import com.och.calltask.domain.query.CustomerSeasAddQuery;
import com.och.calltask.domain.query.CustomerSeasQuery;
import com.och.calltask.domain.vo.CustomerSeasVo;
import com.och.calltask.domain.vo.CustomerTemplateFieldRelVo;
import com.och.calltask.domain.vo.CustomerTemplateVo;
import com.och.calltask.mapper.CustomerSeasMapper;
import com.och.calltask.service.ICustomerSeasService;
import com.och.calltask.service.ICustomerTemplateService;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.CustomerSourceEnum;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.enums.FieldTypeEnum;
import com.och.common.exception.CommonException;
import com.och.common.utils.StringUtils;
import com.och.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 客户公海表(CustomerSeas)表服务实现类
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class CustomerSeasServiceImpl extends BaseServiceImpl<CustomerSeasMapper, CustomerSeas> implements ICustomerSeasService {

    private final ISysUserService sysUserService;
    private final ICustomerTemplateService customerTemplateService;

    @Override
    public void add(CustomerSeasAddQuery query) {
        CustomerSeas customerSeas = new CustomerSeas();
        BeanUtils.copyProperties(query, customerSeas);
        customerSeas.setSource(CustomerSourceEnum.MANUAL.getCode());
        save(customerSeas);
    }

    @Override
    public void edit(CustomerSeasAddQuery query) {
        CustomerSeas customerSeas = getById(query.getId());
        if (Objects.isNull(customerSeas)) {
            throw new CommonException("无效ID");
        }
        BeanUtils.copyProperties(query, customerSeas);
        updateById(customerSeas);
    }

    @Override
    public CustomerSeasVo getDetail(Long id) {
        return this.baseMapper.getDetail(id);
    }

    @Override
    public void delete(CustomerSeasQuery query) {
        List<Long> ids = new LinkedList<>();
        if (Objects.nonNull(query.getId())) {
            ids.add(query.getId());
        }
        if (CollectionUtil.isNotEmpty(query.getIdList())) {
            ids.addAll(query.getIdList());
        }
        if (CollectionUtil.isEmpty(ids)) {
            return;
        }
        List<CustomerSeas> list = ids.stream().map(id -> {
            CustomerSeas seas = new CustomerSeas();
            seas.setId(id);
            seas.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return seas;
        }).toList();
        updateBatchById(list);
    }

    @Override
    public List<CustomerSeasVo> pageList(CustomerSeasQuery query) {
        super.startPage(query.getPageIndex(), query.getPageSize());
        List<CustomerSeasVo> customerSeas =getList(query);
        if(!CollectionUtil.isEmpty(customerSeas)){
            sysUserService.decorate(customerSeas);
        }
        return customerSeas;
    }

    @Override
    public List<CustomerSeasVo> getList(CustomerSeasQuery query) {
        return this.baseMapper.getList(query);
    }

    @Transactional(rollbackFor = {Exception.class,CommonException.class})
    @Override
    public void importCustomer(Long templateId, MultipartFile file) {
        CustomerTemplateVo customerTemplate = customerTemplateService.getDetail(templateId);
        if(Objects.isNull(customerTemplate)){
            throw new CommonException("无效的模板ID");
        }
        if(CollectionUtils.isEmpty(customerTemplate.getFieldList())){
            throw new CommonException("模板未添加字段");
        }
        //必填字段
        Map<String, Boolean> requiredFieldMap = customerTemplate.getFieldList().stream().collect(Collectors.toMap(CustomerTemplateFieldRelVo::getFieldName, item -> item.getRequired() == 1, (key1, key2) -> key1));
        //字段类型
        Map<String, Integer> fieldTypeMap = customerTemplate.getFieldList().stream().collect(Collectors.toMap(CustomerTemplateFieldRelVo::getFieldName, CustomerTemplateFieldRelVo::getFieldType, (key1, key2) -> key1));
        try {
            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
                private Map<Integer, String> headMap;
                private static final int BATCH_COUNT = 100;
                private List<CustomerSeas> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

                @Override
                public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                    this.headMap = headMap;
                }

                @Override
                public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
                    if (headMap == null) return; // 确保表头已读取
                    Map<String, String> rowMap = new LinkedHashMap<>();
                    for (Map.Entry<Integer, String> entry : rowData.entrySet()) {
                        Integer colIndex = entry.getKey();
                        String headerName = headMap.get(colIndex);
                        String cellValue = entry.getValue();
                        //从headerName()获取括号中的内容
                        headerName = headerName.substring(headerName.indexOf("(") + 1, headerName.indexOf(")"));
                        if(requiredFieldMap.get(headerName) && StringUtils.isEmpty(cellValue)){
                            throw new CommonException("字段" + headerName + "不能为空");
                        }
                        String checkFormat = FieldTypeEnum.checkFormat(fieldTypeMap.get(headerName), cellValue);
                        if(StringUtils.isNotEmpty(checkFormat)){
                            throw new CommonException("字段" + headerName + "," + cellValue + "格式错误," + checkFormat);
                        }
                        rowMap.put(headerName, cellValue);
                    }
                    CustomerSeas customerSeas = new CustomerSeas();
                    customerSeas.setTemplateId(templateId);
                    customerSeas.setSource(CustomerSourceEnum.FILE_IMPORT.getCode());
                    customerSeas.setCustomerInfo(JSON.toJSONString(rowMap));
                    cachedDataList.add(customerSeas);
                    if (cachedDataList.size() >= BATCH_COUNT) {
                        saveData();
                        // 存储完成清理 list
                        cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // 这里也要保存数据，确保最后遗留的数据也存储到数据库
                    saveData();
                    log.info("所有数据解析完成！");
                }

                private void saveData() {
                    log.info("{}条数据，开始存储数据库！", cachedDataList.size());
                    saveBatch(cachedDataList);
                    log.info("存储数据库成功！");
                }
            }).sheet().doRead();
        } catch (Exception e) {
            throw new CommonException("导入失败");
        }

    }
}

