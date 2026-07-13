package com.och.calltask.service;
import java.io.InputStream;
public interface ICustomerImportService {
    com.och.calltask.service.impl.CustomerImportServiceImpl.ImportResult importFromCsv(Long poolId, InputStream inputStream);
}
