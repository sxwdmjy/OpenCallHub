package com.och.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.domain.file.FileUploadVo;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.exception.CommonException;
import com.och.common.utils.StringUtils;
import com.och.file.service.IFileTtsService;
import com.och.system.domain.entity.VoiceFile;
import com.och.system.domain.query.file.VoiceFileAddQuery;
import com.och.system.domain.query.file.VoiceFileQuery;
import com.och.system.domain.vo.file.VoiceFileVo;
import com.och.system.mapper.VoiceFileMapper;
import com.och.system.service.ISysFileService;
import com.och.system.service.IVoiceFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 语音文件表(VoiceFile)表服务实现类
 *
 * @author danmo
 * @since 2023-11-01 14:35:02
 */
@RequiredArgsConstructor
@Service
public class VoiceFileServiceImpl extends BaseServiceImpl<VoiceFileMapper, VoiceFile> implements IVoiceFileService {

    private final IFileTtsService iFileTtsService;
    private final ISysFileService iSysFileService;

    @Override
    public void add(VoiceFileAddQuery query) {
        checkName(query.getName());
        VoiceFile voiceFile = new VoiceFile();
        voiceFile.setQuery2Entity(query);
        if(save(voiceFile) && Objects.nonNull(query.getType()) && query.getType() == 2){
            iFileTtsService.textToSpeech(query.getSpeechText(), query.getType(), file -> {
                FileUploadVo fileUploadVo = iSysFileService.uploadFile(file, null);
                voiceFile.setFileId(fileUploadVo.getId());
                updateById(voiceFile);
            });
        }
    }

    @Override
    public void edit(VoiceFileAddQuery query) {
        VoiceFile voiceFile = getById(query.getId());
        if(Objects.isNull(voiceFile)){
            throw new CommonException("无效ID");
        }
        VoiceFile updateVoiceFile = new VoiceFile();
        if(!StringUtils.equals(voiceFile.getName(), query.getName())){
            checkName(query.getName());
        }
        updateVoiceFile.setQuery2Entity(query);
        boolean update = updateById(updateVoiceFile);
        if(update && Objects.nonNull(query.getType()) && query.getType() == 2){
            if(!StringUtils.equals(voiceFile.getSpeechText(), query.getSpeechText())){
                iFileTtsService.textToSpeech(query.getSpeechText(), query.getType(), file -> {
                    FileUploadVo fileUploadVo = iSysFileService.uploadFile(file, null);
                    updateVoiceFile.setFileId(fileUploadVo.getId());
                    updateById(updateVoiceFile);
                });
            }
        }
    }

    @Override
    public void delete(VoiceFileQuery query) {
        List<Long> ids = new LinkedList<>();
        if (Objects.nonNull(query.getId())) {
            ids.add(query.getId());
        }
        if (CollectionUtil.isNotEmpty(query.getIds())) {
            ids.addAll(query.getIds());
        }
        if (CollectionUtil.isEmpty(ids)) {
            return;
        }
        List<VoiceFile> list = ids.stream().map(id -> {
            VoiceFile voiceFile = new VoiceFile();
            voiceFile.setId(id);
            voiceFile.setDelFlag(DeleteStatusEnum.DELETE_YES.getIndex());
            return voiceFile;
        }).collect(Collectors.toList());
        updateBatchById(list);
    }

    @Override
    public VoiceFileVo getDetail(Long id) {
        return this.baseMapper.getDetail(id);
    }

    @Override
    public List<VoiceFileVo> getPageList(VoiceFileQuery query) {
        startPage(query.getPageIndex(), query.getPageSize(), query.getSortField(), query.getSort());
        return getList(query);
    }

    @Override
    public List<VoiceFileVo> getList(VoiceFileQuery query) {
        return this.baseMapper.getList(query);
    }

    private void checkName(String name){
        long count = count(new LambdaQueryWrapper<VoiceFile>().eq(VoiceFile::getName, name).eq(VoiceFile::getDelFlag, DeleteStatusEnum.DELETE_NO.getIndex()));
        if (count > 0){
            throw new CommonException("名称已存在！");
        }
    }
}

