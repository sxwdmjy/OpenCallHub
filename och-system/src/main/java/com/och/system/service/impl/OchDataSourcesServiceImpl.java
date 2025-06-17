package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseEntity;
import com.och.common.base.BaseServiceImpl;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.exception.CommonException;
import com.och.system.domain.entity.OchDataSources;
import com.och.system.domain.entity.OchDataSourcesContact;
import com.och.system.domain.query.calltask.DataSourceAddQuery;
import com.och.system.domain.query.calltask.DataSourceContactQuery;
import com.och.system.domain.query.calltask.DataSourceQuery;
import com.och.system.domain.vo.calltask.DataSourceVo;
import com.och.system.domain.vo.calltask.DataSourcesContactVo;
import com.och.system.handler.CommentWriteHandler;
import com.och.system.mapper.OchDataSourcesMapper;
import com.och.system.service.IOchDataSourcesContactService;
import com.och.system.service.IOchDataSourcesFieldService;
import com.och.system.service.IOchDataSourcesService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据源管理表(OchDataSources)表服务实现类
 *
 * @author danmo
 * @since 2025-06-16 16:08:38
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OchDataSourcesServiceImpl extends BaseServiceImpl<OchDataSourcesMapper, OchDataSources> implements IOchDataSourcesService {

    private final IOchDataSourcesFieldService dataSourcesFieldService;

    private final IOchDataSourcesContactService dataSourcesContactService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DataSourceAddQuery query) {
        OchDataSources dataSources = new OchDataSources();
        dataSources.setName(query.getName());
        dataSources.setRemark(query.getRemark());
        if (save(dataSources) && !CollectionUtils.isEmpty(query.getFieldIdList())) {
            dataSourcesFieldService.saveBatchBySourceId(dataSources.getId(), query.getFieldIdList());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(DataSourceAddQuery query) {
        OchDataSources dataSources = getById(query.getId());
        if (Objects.isNull(dataSources)) {
            throw new CommonException("无效ID");
        }
        dataSources.setName(query.getName());
        dataSources.setRemark(query.getRemark());
        if (updateById(dataSources) && !CollectionUtils.isEmpty(query.getFieldIdList())) {
            dataSourcesFieldService.updateBatchBySourceId(dataSources.getId(), query.getFieldIdList());
        }
    }

    @Override
    public DataSourceVo get(Long id) {
        OchDataSources sources = getById(id);
        if (Objects.isNull(sources)) {
            throw new CommonException("无效ID");
        }
        DataSourceVo dataSourceVo = new DataSourceVo();
        BeanUtils.copyProperties(sources, dataSourceVo);
        dataSourceVo.setFieldList(dataSourcesFieldService.listBySourceId(id));
        dataSourceVo.setFieldNum(dataSourceVo.getFieldList().size());
        long contactNum = dataSourcesContactService.count(new LambdaQueryWrapper<OchDataSourcesContact>()
                .eq(OchDataSourcesContact::getSourceId, id)
                .eq(BaseEntity::getDelFlag, DeleteStatusEnum.DELETE_NO.getIndex()));
        dataSourceVo.setContactNum(contactNum);
        return dataSourceVo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(DataSourceQuery query) {
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
        List<OchDataSources> list = ids.stream().map(id -> {
            OchDataSources dataSources = new OchDataSources();
            dataSources.setId(id);
            dataSources.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return dataSources;
        }).collect(Collectors.toList());
        if (updateBatchById(list)) {
            dataSourcesFieldService.deleteBySourceId(ids);
        }
        ;
    }

    @Override
    public List<DataSourceVo> pageList(DataSourceQuery query) {
        super.startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        return getList(query);
    }

    @Override
    public List<DataSourceVo> getList(DataSourceQuery query) {
        return this.baseMapper.getList(query);
    }

    @Override
    public List<DataSourcesContactVo> getContactPageList(DataSourceContactQuery query) {
        if (Objects.isNull(query.getSourceId())) {
            throw new CommonException("数据源ID不能为空");
        }
        super.startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        return getContactList(query);
    }

    @Override
    public List<DataSourcesContactVo> getContactList(DataSourceContactQuery query) {
        return dataSourcesContactService.getContactList(query);
    }

    @Override
    public void contactTemplateDownload(Long sourceId, HttpServletResponse response) {
        DataSourceVo dataSource = get(sourceId);
        if (Objects.isNull(dataSource)) {
            throw new CommonException("无效数据源ID");
        }
        if (CollectionUtils.isEmpty(dataSource.getFieldList())) {
            throw new CommonException("数据源字段为空");
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String name = URLEncoder.encode(dataSource.getName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + name + ".xlsx");
        List<String> requiredList = Lists.newArrayList();
        List<List<String>> titles = dataSource.getFieldList().stream().map(fieldInfo -> {
            List<String> title = Lists.newArrayList();
            if (Objects.equals(fieldInfo.getRequired(), 1)) {
                requiredList.add(fieldInfo.getFieldName());
            }
            title.add(fieldInfo.getFieldName());
            return title;
        }).toList();
        try {
            EasyExcel.write(response.getOutputStream())
                    .registerWriteHandler(new CommentWriteHandler(requiredList, "请填写必填项"))
                    .head(titles)
                    .sheet(dataSource.getName())
                    .doWrite(Lists.newArrayList());
        } catch (Exception e) {
            throw new CommonException("导出失败");
        }
    }

    @Override
    public void contactTemplateUpload(Long sourceId, MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
                private Map<Integer, String> headMap;
                private static final int BATCH_COUNT = 100;
                private List<OchDataSourcesContact> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

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
                        rowMap.put(headerName, cellValue);
                    }
                    OchDataSourcesContact contact = new OchDataSourcesContact();
                    contact.setSourceId(sourceId);
                    contact.setContact(JSON.toJSONString(rowMap));
                    cachedDataList.add(contact);
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
                    dataSourcesContactService.saveBatch(cachedDataList);
                    log.info("存储数据库成功！");
                }
            }).sheet().doRead();
        } catch (Exception e) {
            throw new CommonException("导入失败");
        }
    }

}

