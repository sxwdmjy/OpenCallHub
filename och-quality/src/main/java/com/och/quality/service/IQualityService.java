package com.och.quality.service;
import com.och.quality.domain.QualityAppeal;
import com.och.quality.domain.QualityScore;
import com.och.quality.domain.QualityTemplate;
import java.util.List;
public interface IQualityService {
    List<QualityTemplate> listTemplates();
    QualityTemplate createTemplate(QualityTemplate template);
    QualityScore submitScore(QualityScore score);
    QualityAppeal submitAppeal(QualityAppeal appeal);
    void reviewAppeal(Long id, Boolean approved, String comment);
}
