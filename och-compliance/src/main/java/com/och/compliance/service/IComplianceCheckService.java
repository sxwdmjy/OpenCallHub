package com.och.compliance.service;

import com.och.compliance.service.impl.ComplianceCheckServiceImpl.ComplianceResult;

public interface IComplianceCheckService {
    ComplianceResult check(String phoneNumber);
}
