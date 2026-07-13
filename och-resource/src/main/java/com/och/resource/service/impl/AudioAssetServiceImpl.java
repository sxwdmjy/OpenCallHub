package com.och.resource.service.impl;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.common.api.PageResponse;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.resource.domain.AudioAsset;
import com.och.resource.domain.ResourceType;
import com.och.resource.mapper.AudioAssetMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 音频素材应用服务
 * <p>统一处理 IVR 播放音、排队音、提示音等音频资源的查询、变更审计和删除保护。</p>
 *
 * @author danmo
 * @date 2025-01-01
 */
@Service
@RequiredArgsConstructor
public class AudioAssetServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.resource.mapper.AudioAssetMapper, com.och.resource.domain.AudioAsset> implements com.och.resource.service.IAudioAssetService {

    private final ResourceDependencyServiceImpl dependencyService;
    private final ResourceAuditServiceImpl auditService;

    public PageResponse<AudioAsset> getPageList(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<AudioAsset> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(AudioAsset::getAssetName, keyword)
                    .or().like(AudioAsset::getAssetCode, keyword));
        }
        wrapper.orderByDesc(AudioAsset::getCreatedAt);
        IPage<AudioAsset> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    public AudioAsset getDetail(Long id) {
        return baseMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public AudioAsset add(AudioAsset asset) {
        baseMapper.insert(asset);
        auditService.recordCreate(ResourceType.AUDIO_ASSET, asset.getId(), safeSummary(asset));
        return asset;
    }

    @Transactional(rollbackFor = Exception.class)
    public AudioAsset edit(Long id, AudioAsset asset) {
        AudioAsset existing = getRequired(id);
        asset.setId(id);
        baseMapper.updateById(asset);
        auditService.recordUpdate(ResourceType.AUDIO_ASSET, id, safeSummary(existing), safeSummary(asset));
        return asset;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        AudioAsset existing = getRequired(id);
        dependencyService.checkDeletable(ResourceType.AUDIO_ASSET, id);
        baseMapper.deleteById(id);
        auditService.recordDelete(ResourceType.AUDIO_ASSET, id, safeSummary(existing));
    }

    private AudioAsset getRequired(Long id) {
        AudioAsset asset = baseMapper.selectById(id);
        if (asset == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "音频素材不存在: " + id);
        }
        return asset;
    }

    private String safeSummary(AudioAsset asset) {
        if (asset == null) {
            return null;
        }
        return "assetName=" + asset.getAssetName()
                + ",assetCode=" + asset.getAssetCode()
                + ",fileKey=" + asset.getFileKey()
                + ",formatType=" + asset.getFormatType()
                + ",status=" + asset.getStatus();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
