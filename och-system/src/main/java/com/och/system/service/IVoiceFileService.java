package com.och.system.service;


import com.github.pagehelper.PageInfo;
import com.och.common.base.IBaseService;
import com.och.system.domain.entity.VoiceFile;
import com.och.system.domain.query.file.VoiceFileAddQuery;
import com.och.system.domain.query.file.VoiceFileQuery;
import com.och.system.domain.vo.file.VoiceFileVo;

import java.util.List;

/**
 * 语音文件管理服务接口
 * <p>提供语音文件的增删改查功能，支持手动上传与 TTS 语音合成两种方式。
 * TTS 语音合成支持多种引擎（阿里云/腾讯云/讯飞），通过 type 字段选择合成引擎。</p>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li><b>策略模式</b>：TTS 语音合成支持多种引擎（阿里云/腾讯云/讯飞），通过 type 字段动态选择合成策略，各引擎实现可互换</li>
 *   <li><b>模板方法</b>：继承 IBaseService，复用通用 CRUD 模板</li>
 * </ul>
 *
 * @author danmo
 * @since 2023-11-01 14:35:02
 */
public interface IVoiceFileService extends IBaseService<VoiceFile> {

    void add(VoiceFileAddQuery query);

    void edit(VoiceFileAddQuery query);

    void delete(VoiceFileQuery query);

    VoiceFileVo getDetail(Long id);

    List<VoiceFileVo> getPageList(VoiceFileQuery query);

    List<VoiceFileVo> getList(VoiceFileQuery query);
}

