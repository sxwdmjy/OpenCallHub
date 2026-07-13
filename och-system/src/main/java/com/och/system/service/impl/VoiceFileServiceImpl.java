package com.och.system.service.impl;

import lombok.RequiredArgsConstructor;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.base.BaseServiceImpl;
import com.och.common.domain.file.FileUploadVo;
import com.och.common.enums.DeleteStatusEnum;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.common.utils.StringUtils;
import com.och.file.server.FileTransferServer;
import com.och.file.service.IFileTtsService;
import com.och.system.domain.entity.SysFile;
import com.och.system.domain.entity.VoiceFile;
import com.och.system.domain.query.file.VoiceFileAddQuery;
import com.och.system.domain.query.file.VoiceFileQuery;
import com.och.system.domain.vo.file.VoiceFileVo;
import com.och.system.mapper.VoiceFileMapper;
import com.och.system.service.ISysFileService;
import com.och.system.service.IVoiceFileService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 语音文件管理服务实现
 * <p>实现语音文件的增删改查功能，支持手动上传与 TTS 语音合成两种处理路径：
 * 手动上传路径（type=1）直接使用已上传的文件并通过 FileTransferServer 分发至客户端；
 * TTS 合成路径（type=2）调用 TTS 引擎将文本转为语音后自动上传并分发，实现自动化的语音文件生成与管理。</p>
 *
 * @author danmo
 * @since 2023-11-01 14:35:02
 */
@RequiredArgsConstructor
@Service
public class VoiceFileServiceImpl extends BaseServiceImpl<VoiceFileMapper, VoiceFile> implements IVoiceFileService {

    private final IFileTtsService iFileTtsService;
    private final ISysFileService iSysFileService;
    private final FileTransferServer fileTransferServer;

    @Override
    public void add(VoiceFileAddQuery query) {
        checkName(query.getName());
        VoiceFile voiceFile = VoiceFile.builder()
                .name(query.getName())
                .type(query.getType())
                .tts(query.getTts())
                .speechText(query.getSpeechText())
                .fileId(query.getFileId())
                .build();
        if(save(voiceFile)){
            if(Objects.nonNull(query.getType()) && query.getType() == 1){
                SysFile sysFile = iSysFileService.getById(voiceFile.getFileId());
                fileTransferServer.sendFileToClient(sysFile.getFileName(), sysFile.getFilePath());
            }
            if(Objects.nonNull(query.getType()) && query.getType() == 2){
                iFileTtsService.textToSpeech(query.getSpeechText(), query.getType(), file -> {
                    FileUploadVo fileUploadVo = iSysFileService.uploadFile(file, null);
                    voiceFile.setFileId(fileUploadVo.getId());
                    updateById(voiceFile);
                    fileTransferServer.sendFileToClient(file);
                });
            }
        }
    }

    @Override
    public void edit(VoiceFileAddQuery query) {
        VoiceFile voiceFile = getById(query.getId());
        if(Objects.isNull(voiceFile)){
            throw new BizException(ErrorCode.BIZ_RESOURCE_ID_REQUIRED);
        }
        VoiceFile updateVoiceFile = new VoiceFile();
        if(!StringUtils.equals(voiceFile.getName(), query.getName())){
            checkName(query.getName());
        }
        Long currentFileId = voiceFile.getFileId();
        updateVoiceFile.setId(query.getId());
        updateVoiceFile.setName(query.getName());
        updateVoiceFile.setType(query.getType());
        updateVoiceFile.setTts(query.getTts());
        updateVoiceFile.setSpeechText(query.getSpeechText());
        updateVoiceFile.setFileId(query.getFileId());
        boolean update = updateById(updateVoiceFile);
        if(update){
            if(Objects.nonNull(query.getType()) && query.getType() == 1 &&  !Objects.equals(currentFileId, query.getFileId())){
                SysFile sysFile = iSysFileService.getById(query.getFileId());
                fileTransferServer.sendFileToClient(sysFile.getFileName(), sysFile.getFilePath());
            }
            if(Objects.nonNull(query.getType()) && query.getType() == 2){
                if(!StringUtils.equals(voiceFile.getSpeechText(), query.getSpeechText())){
                    iFileTtsService.textToSpeech(query.getSpeechText(), query.getType(), file -> {
                        FileUploadVo fileUploadVo = iSysFileService.uploadFile(file, null);
                        updateVoiceFile.setFileId(fileUploadVo.getId());
                        updateById(updateVoiceFile);
                        fileTransferServer.sendFileToClient(file);
                    });
                }
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
            throw new BizException(ErrorCode.BIZ_NAME_DUPLICATE);
        }
    }
}

