package com.och.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.quality.domain.QualityAppeal;
import com.och.quality.domain.QualityScore;
import com.och.quality.domain.QualityTemplate;
import com.och.quality.mapper.QualityAppealMapper;
import com.och.quality.mapper.QualityScoreMapper;
import com.och.quality.mapper.QualityTemplateMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 质检中心服务。
 * <p>
 * 提供质检模板管理、人工/AI 评分提交、申诉复核等核心业务逻辑。
 * 评分时根据关联模板的 passScore 判定是否及格。
 * 申诉流程：坐席提交申诉 → 质检员复核（通过/驳回）→ 若通过则更新原评分。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QualityServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.quality.mapper.QualityTemplateMapper, com.och.quality.domain.QualityTemplate> implements com.och.quality.service.IQualityService {

    private final QualityScoreMapper scoreMapper;
    private final QualityAppealMapper appealMapper;

    private static final String APPEAL_PENDING = "PENDING";
    private static final String APPEAL_APPROVED = "APPROVED";
    private static final String APPEAL_REJECTED = "REJECTED";

    // ==================== 质检模板 ====================

    /**
     * 查询所有已发布的质检模板列表。
     *
     * @return 已发布的质检模板列表
     */
    public List<QualityTemplate> listTemplates() {
        log.info("查询质检模板列表");
        LambdaQueryWrapper<QualityTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QualityTemplate::getPublished, true)
               .orderByDesc(QualityTemplate::getUpdatedAt);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 创建质检模板。
     * <p>
     * 自动生成唯一模板编码（QT + UUID 前 6 位），默认未发布状态。
     * 创建后需手动发布方可使用。
     * </p>
     *
     * @param template 质检模板实体
     * @return 持久化后的质检模板
     */
    @Transactional
    public QualityTemplate createTemplate(QualityTemplate template) {
        template.setTemplateCode("QT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        if (template.getPublished() == null) {
            template.setPublished(false);
        }
        baseMapper.insert(template);
        log.info("质检模板创建成功：id={}, templateCode={}, templateName={}",
                template.getId(), template.getTemplateCode(), template.getTemplateName());
        return template;
    }

    // ==================== 质检评分 ====================

    /**
     * 提交质检评分。
     * <p>
     * 根据关联的质检模板判断评分是否及格：
     * 如果关联模板存在且 totalScore >= 模板的 passScore，则 passScore 设为 1（及格），否则为 0（不及格）。
     * 若未关联模板，则默认 passScore = 0。
     * </p>
     *
     * @param score 质检评分实体
     * @return 持久化后的评分记录
     */
    @Transactional
    public QualityScore submitScore(QualityScore score) {
        log.info("提交质检评分：templateId={}, callRecordId={}, scorerId={}",
                score.getTemplateId(), score.getCallRecordId(), score.getScorerId());
        // 根据关联模板判定是否及格
        if (score.getTemplateId() != null) {
            QualityTemplate template = baseMapper.selectById(score.getTemplateId());
            if (template != null && template.getPassScore() != null && score.getTotalScore() != null) {
                int passed = score.getTotalScore() >= template.getPassScore() ? 1 : 0;
                score.setPassScore(passed);
                log.info("评分及格判定：totalScore={}, passScore={}, passed={}",
                        score.getTotalScore(), template.getPassScore(), passed);
            } else {
                score.setPassScore(0);
            }
        } else {
            score.setPassScore(0);
        }
        scoreMapper.insert(score);
        log.info("质检评分提交成功：id={}, totalScore={}, passScore={}",
                score.getId(), score.getTotalScore(), score.getPassScore());
        return score;
    }

    // ==================== 质检申诉 ====================

    /**
     * 提交质检申诉。
     * <p>
     * 申诉初始状态为 PENDING，等待质检员复核。
     * 申诉时必须关联已有的评分记录，以便复核时回溯。
     * </p>
     *
     * @param appeal 申诉实体（需包含 scoreId、appellantId、reason）
     * @return 持久化后的申诉记录
     */
    @Transactional
    public QualityAppeal submitAppeal(QualityAppeal appeal) {
        log.info("提交质检申诉：scoreId={}, appellantId={}", appeal.getScoreId(), appeal.getAppellantId());
        // 校验关联评分存在
        QualityScore score = scoreMapper.selectById(appeal.getScoreId());
        if (score == null) {
            log.warn("申诉关联的评分不存在：scoreId={}", appeal.getScoreId());
            throw new IllegalArgumentException("申诉关联的评分不存在：" + appeal.getScoreId());
        }
        appeal.setState(APPEAL_PENDING);
        appealMapper.insert(appeal);
        log.info("质检申诉提交成功：id={}, state={}", appeal.getId(), appeal.getState());
        return appeal;
    }

    /**
     * 复核申诉。
     * <p>
     * 质检员审核申诉：通过则更新申诉状态为 APPROVED 并更新原评分备注；
     * 驳回则更新状态为 REJECTED，保留原评分不变。
     * 记录复核人 ID、复核意见和复核时间。
     * </p>
     *
     * @param id       申诉 ID
     * @param approved 是否通过（true 为通过，false 为驳回）
     * @param comment  复核意见
     * @throws IllegalArgumentException 如果申诉不存在
     */
    @Transactional
    public void reviewAppeal(Long id, Boolean approved, String comment) {
        log.info("复核申诉：id={}, approved={}, comment={}", id, approved, comment);
        QualityAppeal appeal = appealMapper.selectById(id);
        if (appeal == null) {
            log.warn("申诉不存在：id={}", id);
            throw new IllegalArgumentException("申诉不存在：" + id);
        }
        // 更新申诉状态
        QualityAppeal update = new QualityAppeal();
        update.setId(id);
        update.setState(Boolean.TRUE.equals(approved) ? APPEAL_APPROVED : APPEAL_REJECTED);
        update.setReviewComment(comment);
        update.setReviewedAt(LocalDateTime.now());
        appealMapper.updateById(update);

        // 若申诉通过，更新原评分的备注
        if (Boolean.TRUE.equals(approved)) {
            QualityScore score = scoreMapper.selectById(appeal.getScoreId());
            if (score != null) {
                QualityScore scoreUpdate = new QualityScore();
                scoreUpdate.setId(score.getId());
                scoreUpdate.setComment("【申诉通过】" + (comment != null ? comment : ""));
                scoreMapper.updateById(scoreUpdate);
            }
        }

        log.info("申诉复核完成：id={}, state={}", id, update.getState());
    }
}
