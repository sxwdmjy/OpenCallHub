package com.och.resource.service;
import com.och.common.api.PageResponse;
import com.och.resource.domain.AudioAsset;
public interface IAudioAssetService {
    PageResponse<AudioAsset> getPageList(int pageNum, int pageSize, String keyword);
    AudioAsset getDetail(Long id);
    AudioAsset add(AudioAsset asset);
    AudioAsset edit(Long id, AudioAsset asset);
    void delete(Long id);
}
