package com.och.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.error.BizException;
import com.och.file.server.FileTransferServer;
import com.och.file.service.IFileTtsService;
import com.och.system.domain.entity.SysFile;
import com.och.system.domain.entity.VoiceFile;
import com.och.system.domain.query.file.VoiceFileAddQuery;
import com.och.system.domain.query.file.VoiceFileQuery;
import com.och.system.domain.vo.file.VoiceFileVo;
import com.och.system.mapper.VoiceFileMapper;
import com.och.system.service.ISysFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * VoiceFileServiceImpl unit tests.
 */
@ExtendWith(MockitoExtension.class)
class VoiceFileServiceTest {

    @Mock
    private VoiceFileMapper voiceFileMapper;

    @Mock
    private IFileTtsService iFileTtsService;

    @Mock
    private ISysFileService iSysFileService;

    @Mock
    private FileTransferServer fileTransferServer;

    @Spy
    @InjectMocks
    private VoiceFileServiceImpl voiceFileService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(voiceFileService, "baseMapper", voiceFileMapper);
    }

    // ── add ───────────────────────────────────────────────────────────────

    @Test
    void add_withManualUpload_shouldSaveAndSendFile() {
        VoiceFileAddQuery query = new VoiceFileAddQuery();
        query.setName("greeting.wav");
        query.setType(1);
        query.setFileId(100L);

        when(voiceFileMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(voiceFileMapper.insert(any(VoiceFile.class))).thenReturn(1);

        SysFile sysFile = SysFile.builder().id(100L).fileName("greeting.wav").filePath("/upload/greeting.wav").build();
        when(iSysFileService.getById(100L)).thenReturn(sysFile);

        voiceFileService.add(query);

        verify(voiceFileMapper).insert(any(VoiceFile.class));
        verify(iSysFileService).getById(100L);
        verify(fileTransferServer).sendFileToClient("greeting.wav", "/upload/greeting.wav");
    }

    @Test
    void add_withDuplicateName_shouldThrowBizException() {
        VoiceFileAddQuery query = new VoiceFileAddQuery();
        query.setName("duplicate.wav");
        query.setType(1);

        when(voiceFileMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThatThrownBy(() -> voiceFileService.add(query))
                .isInstanceOf(BizException.class);

        verify(voiceFileMapper, never()).insert(any());
    }

    // ── delete ────────────────────────────────────────────────────────────

    @Test
    void delete_byId_shouldSoftDelete() {
        VoiceFileQuery query = new VoiceFileQuery();
        query.setId(1L);

        doReturn(true).when(voiceFileService).updateBatchById(anyList());

        voiceFileService.delete(query);

        verify(voiceFileService).updateBatchById(anyList());
    }

    @Test
    void delete_byIds_shouldSoftDeleteMultiple() {
        VoiceFileQuery query = new VoiceFileQuery();
        query.setIds(List.of(1L, 2L, 3L));

        doReturn(true).when(voiceFileService).updateBatchById(anyList());

        voiceFileService.delete(query);

        verify(voiceFileService).updateBatchById(anyList());
    }

    // ── getDetail ─────────────────────────────────────────────────────────

    @Test
    void getDetail_shouldReturnVoiceFileVo_whenExists() {
        Long id = 1L;
        VoiceFileVo vo = new VoiceFileVo();
        vo.setId(id);
        vo.setName("test.wav");
        vo.setType(1);

        when(voiceFileMapper.getDetail(id)).thenReturn(vo);

        VoiceFileVo result = voiceFileService.getDetail(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("test.wav");
        verify(voiceFileMapper).getDetail(id);
    }

    // ── getList ───────────────────────────────────────────────────────────

    @Test
    void getList_shouldReturnVoList() {
        VoiceFileQuery query = new VoiceFileQuery();
        VoiceFileVo vo1 = new VoiceFileVo();
        vo1.setId(1L);
        vo1.setName("file1.wav");
        VoiceFileVo vo2 = new VoiceFileVo();
        vo2.setId(2L);
        vo2.setName("file2.wav");

        when(voiceFileMapper.getList(query)).thenReturn(List.of(vo1, vo2));

        List<VoiceFileVo> result = voiceFileService.getList(query);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(VoiceFileVo::getName).containsExactly("file1.wav", "file2.wav");
        verify(voiceFileMapper).getList(query);
    }
}
