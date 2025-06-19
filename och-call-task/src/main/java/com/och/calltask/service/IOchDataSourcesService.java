package com.och.calltask.service;

import com.och.calltask.domain.entity.OchDataSources;
import com.och.calltask.domain.query.DataSourceAddQuery;
import com.och.calltask.domain.query.DataSourceContactQuery;
import com.och.calltask.domain.query.DataSourceQuery;
import com.och.calltask.domain.vo.DataSourceVo;
import com.och.calltask.domain.vo.DataSourcesContactVo;
import com.och.common.base.IBaseService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 数据源管理表(OchDataSources)表服务接口
 *
 * @author danmo
 * @since 2025-06-16 16:08:38
 */
public interface IOchDataSourcesService extends IBaseService<OchDataSources> {

    /**
     * 新增数据源
     *
     * @param query 新增数据源参数
     */
    void add(DataSourceAddQuery query);

    /**
     * 修改数据源
     *
     * @param query 修改数据源参数
     */
    void edit(DataSourceAddQuery query);

    /**
     * 获取数据源详情
     *
     * @param id 数据源ID
     * @return 数据源详情
     */
    DataSourceVo get(Long id);

    /**
     * 删除数据源
     *
     * @param query 删除数据源参数
     */
    void delete(DataSourceQuery query);

    /**
     * 获取数据源列表(分页)
     *
     * @param query 查询参数
     * @return 数据源列表
     */
    List<DataSourceVo> pageList(DataSourceQuery query);

    /**
     * 获取数据源列表(不分页)
     *
     * @param query 获取数据源列表参数
     * @return 数据源列表
     */
    List<DataSourceVo> getList(DataSourceQuery query);

    /**
     * 获取数据源联系人列表(分页)
     *
     * @param query    查询参数
     * @return 数据源联系人列表
     */
    List<DataSourcesContactVo> getContactPageList(DataSourceContactQuery query);

    /**
     * 获取数据源联系人列表(不分页)
     *
     * @param query    获取数据源联系人列表参数
     * @return 数据源联系人列表
     */
    List<DataSourcesContactVo> getContactList(DataSourceContactQuery query);

    /**
     * 数据源联系人模板下载
     * @param sourceId 数据源ID
     * @param response 响应
     */
    void contactTemplateDownload(Long sourceId, jakarta.servlet.http.HttpServletResponse response);

    /**
     * 数据源联系人模板上传
     * @param sourceId 数据源ID
     * @param file 文件
     */
    void contactTemplateUpload(Long sourceId, MultipartFile file);
}

