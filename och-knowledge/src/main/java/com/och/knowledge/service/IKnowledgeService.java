package com.och.knowledge.service;
import com.och.common.api.PageResponse;
import com.och.knowledge.domain.KnowledgeDocument;
import com.och.knowledge.domain.ScriptTemplate;
public interface IKnowledgeService {
    PageResponse<KnowledgeDocument> search(String keyword);
    PageResponse<KnowledgeDocument> listDocuments(int pageNum, int pageSize);
    KnowledgeDocument createDocument(KnowledgeDocument doc);
    PageResponse<ScriptTemplate> listScripts(String keyword);
    ScriptTemplate createScript(ScriptTemplate script);
}
