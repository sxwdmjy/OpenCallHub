package com.och.mrcp;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class MrcpSessionManager {

    private static final MrcpSessionManager INSTANCE = new MrcpSessionManager();
    private final Map<String, MrcpSession> sessions = new ConcurrentHashMap<>();
    
    // 统计信息
    private final AtomicInteger totalSessions = new AtomicInteger(0);
    private final AtomicInteger activeSessions = new AtomicInteger(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "mrcp-session-cleanup");
        t.setDaemon(true);
        return t;
    });

    public static MrcpSessionManager getInstance() {
        return INSTANCE;
    }

    private MrcpSessionManager() {
        // 每5分钟清理一次无效会话
        scheduler.scheduleAtFixedRate(this::cleanupInactiveSessions, 5, 5, TimeUnit.MINUTES);
        
        // 添加JVM关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("JVM关闭，正在清理MRCP会话...");
            shutdown();
        }));
    }

    public MrcpSession createSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("尝试创建无效的MRCP会话，sessionId: {}", sessionId);
            return null;
        }
        
        if (sessions.containsKey(sessionId)) {
            log.info("MRCP会话已存在: {}", sessionId);
            return getSession(sessionId);
        }
        
        try {
            MrcpSession mrcpSession = new MrcpSession(sessionId);
            sessions.put(sessionId, mrcpSession);
            totalSessions.incrementAndGet();
            activeSessions.incrementAndGet();
            
            log.info("创建MRCP会话成功: {}, 当前活跃会话数: {}", sessionId, activeSessions.get());
            return mrcpSession;
        } catch (Exception e) {
            log.error("创建MRCP会话失败: {}", sessionId, e);
            return null;
        }
    }

    private void cleanupInactiveSessions() {
        int cleanedCount = 0;
        long startTime = System.currentTimeMillis();
        
        try {
            for (Map.Entry<String, MrcpSession> entry : sessions.entrySet()) {
                MrcpSession session = entry.getValue();
                if (session != null && session.getState().get() == MrcpSession.State.CLOSED) {
                    sessions.remove(entry.getKey());
                    activeSessions.decrementAndGet();
                    cleanedCount++;
                }
            }
            
            if (cleanedCount > 0) {
                long cleanupTime = System.currentTimeMillis() - startTime;
                log.info("清理了{}个无效MRCP会话，耗时: {}ms，当前活跃会话数: {}", 
                    cleanedCount, cleanupTime, activeSessions.get());
            }
        } catch (Exception e) {
            log.error("清理无效MRCP会话时发生异常", e);
        }
    }

    // 显式销毁会话
    public void destroySession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        
        MrcpSession session = sessions.remove(sessionId);
        if (session != null) {
            try {
                session.close(); // 触发MRCP协议终止流程
                activeSessions.decrementAndGet();
                log.info("MRCP会话已销毁: {}, 当前活跃会话数: {}", sessionId, activeSessions.get());
            } catch (Exception e) {
                log.error("销毁MRCP会话时发生异常: {}", sessionId, e);
            }
        }
    }

    // 获取会话
    public MrcpSession getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        
        MrcpSession session = sessions.get(sessionId);
        if (session != null && session.getState().get() == MrcpSession.State.CLOSED) {
            // 自动清理已关闭的会话
            sessions.remove(sessionId);
            activeSessions.decrementAndGet();
            return null;
        }
        return session;
    }

    /**
     * 获取会话统计信息
     */
    public SessionStats getSessionStats() {
        return new SessionStats(
            totalSessions.get(),
            activeSessions.get(),
            sessions.size(),
            totalProcessingTime.get()
        );
    }

    /**
     * 关闭所有会话
     */
    public void shutdown() {
        log.info("开始关闭MRCP会话管理器，当前会话数: {}", sessions.size());
        
        try {
            // 关闭所有活跃会话
            sessions.values().forEach(session -> {
                try {
                    if (session != null) {
                        session.close();
                    }
                } catch (Exception e) {
                    log.warn("关闭MRCP会话时发生异常: {}", session.getSessionId(), e);
                }
            });
            
            sessions.clear();
            activeSessions.set(0);
            
            // 关闭调度器
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            }
            
            log.info("MRCP会话管理器已关闭");
        } catch (Exception e) {
            log.error("关闭MRCP会话管理器时发生异常", e);
        }
    }

    /**
     * 会话统计信息
     */
    public static class SessionStats {
        private final int totalSessions;
        private final int activeSessions;
        private final int currentSessions;
        private final long totalProcessingTime;

        public SessionStats(int totalSessions, int activeSessions, int currentSessions, long totalProcessingTime) {
            this.totalSessions = totalSessions;
            this.activeSessions = activeSessions;
            this.currentSessions = currentSessions;
            this.totalProcessingTime = totalProcessingTime;
        }

        public int getTotalSessions() { return totalSessions; }
        public int getActiveSessions() { return activeSessions; }
        public int getCurrentSessions() { return currentSessions; }
        public long getTotalProcessingTime() { return totalProcessingTime; }

        @Override
        public String toString() {
            return String.format("MRCP SessionStats{total=%d, active=%d, current=%d, processingTime=%dms}", 
                totalSessions, activeSessions, currentSessions, totalProcessingTime);
        }
    }
}
