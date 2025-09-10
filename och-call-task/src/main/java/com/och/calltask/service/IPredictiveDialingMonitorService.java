package com.och.calltask.service;

import com.och.calltask.domain.vo.PredictiveDialingDashboard;
import com.och.calltask.domain.vo.PredictiveDialingMetrics;
import com.och.calltask.domain.vo.PredictiveDialingStats;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预测式外呼监控服务接口
 * 
 * @author danmo
 * @date 2025/01/15
 */
public interface IPredictiveDialingMonitorService {

    /**
     * 获取实时监控仪表板
     * 
     * @param taskId 任务ID
     * @return 监控仪表板数据
     */
    PredictiveDialingDashboard getRealtimeDashboard(Long taskId);

    /**
     * 获取任务统计信息
     * 
     * @param taskId 任务ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    PredictiveDialingStats getTaskStats(Long taskId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取所有活跃任务的监控数据
     * 
     * @return 活跃任务监控数据列表
     */
    List<PredictiveDialingDashboard> getAllActiveTaskDashboards();

    /**
     * 获取系统整体性能指标
     * 
     * @return 系统性能指标
     */
    PredictiveDialingMetrics getSystemPerformanceMetrics();

    /**
     * 获取预测准确率趋势
     * 
     * @param taskId 任务ID
     * @param days 天数
     * @return 准确率趋势数据
     */
    List<Double> getPredictionAccuracyTrend(Long taskId, int days);

    /**
     * 获取坐席利用率趋势
     * 
     * @param taskId 任务ID
     * @param hours 小时数
     * @return 利用率趋势数据
     */
    List<Double> getAgentUtilizationTrend(Long taskId, int hours);

    /**
     * 获取客户接通率分析
     * 
     * @param taskId 任务ID
     * @return 接通率分析数据
     */
    PredictiveDialingStats getContactAnswerRateAnalysis(Long taskId);

    /**
     * 获取风险预警信息
     * 
     * @param taskId 任务ID
     * @return 风险预警列表
     */
    List<String> getRiskAlerts(Long taskId);

    /**
     * 获取性能优化建议
     * 
     * @param taskId 任务ID
     * @return 优化建议列表
     */
    List<String> getPerformanceOptimizationSuggestions(Long taskId);

    /**
     * 记录实时事件
     * 
     * @param taskId 任务ID
     * @param eventType 事件类型
     * @param eventData 事件数据
     */
    void recordRealtimeEvent(Long taskId, String eventType, Object eventData);

    /**
     * 获取历史性能数据
     * 
     * @param taskId 任务ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史性能数据
     */
    List<PredictiveDialingMetrics> getHistoricalPerformanceData(Long taskId, LocalDateTime startTime, LocalDateTime endTime);
}
