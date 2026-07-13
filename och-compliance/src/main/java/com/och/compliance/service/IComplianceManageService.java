package com.och.compliance.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.compliance.domain.BlacklistRecord;
import com.och.compliance.domain.DncRecord;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
public interface IComplianceManageService {
    IPage<DncRecord> pageDnc(int pageNum, int pageSize, String keyword);
    DncRecord addDnc(String phoneNumber, String reason, String source, LocalDateTime expireAt);
    void removeDnc(Long id);
    int batchAddDnc(List<DncRecord> records);
    int importDncCsv(InputStream inputStream, String source);
    IPage<BlacklistRecord> pageBlacklist(int pageNum, int pageSize, String keyword);
    BlacklistRecord addBlacklist(String phoneNumber, String reason, String level);
    void removeBlacklist(Long id);
    int batchAddBlacklist(List<BlacklistRecord> records);
    int importBlacklistCsv(InputStream inputStream);
}
