package com.och.websocket.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket会话管理工具类
 * @author danmo
 * @date 2023年09月22日 10:53
 */
@Slf4j
public class WsSessionUtil {

    public static final Map<String, WebSocketSession> sessionPool = new ConcurrentHashMap<>(16);
    private static final AtomicInteger totalSessions = new AtomicInteger(0);
    private static final AtomicInteger activeSessions = new AtomicInteger(0);
    
    // 会话清理调度器
    private static final ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "ws-session-cleanup");
        t.setDaemon(true);
        return t;
    });

    static {
        // 启动定期清理任务
        startCleanupTask();
        
        // 添加JVM关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("JVM关闭，正在清理WebSocket会话...");
            cleanupAllSessions();
            cleanupScheduler.shutdown();
        }));
    }

    /**
     * 添加session
     * @param key sessionId
     * @param session session
     */
    public static void add(String key, WebSocketSession session) {
        if (key == null || session == null) {
            log.warn("尝试添加无效的WebSocket会话，key: {}, session: {}", key, session);
            return;
        }
        
        WebSocketSession oldSession = sessionPool.put(key, session);
        if (oldSession != null) {
            // 如果存在旧会话，先关闭
            closeSession(oldSession);
            log.info("替换WebSocket会话，key: {}", key);
        } else {
            totalSessions.incrementAndGet();
        }
        
        activeSessions.incrementAndGet();
        log.debug("添加WebSocket会话成功，key: {}, 当前活跃会话数: {}", key, activeSessions.get());
    }

    /**
     * 移除session
     * @param key sessionId
     */
    public static void remove(String key) {
        if (key == null) {
            return;
        }
        
        WebSocketSession session = sessionPool.remove(key);
        if (session != null) {
            activeSessions.decrementAndGet();
            log.debug("移除WebSocket会话，key: {}, 当前活跃会话数: {}", key, activeSessions.get());
        }
    }

    /**
     * 移除session并关闭连接
     * @param key sessionId
     */
    public static void removeAndClose(String key) {
        if (key == null) {
            return;
        }
        
        WebSocketSession session = sessionPool.remove(key);
        if (session != null) {
            activeSessions.decrementAndGet();
            closeSession(session);
            log.debug("移除并关闭WebSocket会话，key: {}, 当前活跃会话数: {}", key, activeSessions.get());
        }
    }

    /**
     * 获取session
     * @param key sessionId
     * @return WebSocketSession
     */
    public static WebSocketSession get(String key) {
        if (key == null) {
            return null;
        }
        
        WebSocketSession session = sessionPool.get(key);
        if (session != null && !session.isOpen()) {
            // 如果会话已关闭，自动清理
            removeAndClose(key);
            return null;
        }
        return session;
    }

    /**
     * 获取所有活跃会话
     */
    public static Map<String, WebSocketSession> getAllSessions() {
        return new ConcurrentHashMap<>(sessionPool);
    }

    /**
     * 获取会话统计信息
     */
    public static SessionStats getSessionStats() {
        return new SessionStats(
            totalSessions.get(),
            activeSessions.get(),
            sessionPool.size()
        );
    }

    /**
     * 关闭所有会话
     */
    public static void cleanupAllSessions() {
        log.info("开始清理所有WebSocket会话，总数: {}", sessionPool.size());
        
        sessionPool.values().forEach(WsSessionUtil::closeSession);
        sessionPool.clear();
        activeSessions.set(0);
        
        log.info("所有WebSocket会话清理完成");
    }

    /**
     * 启动定期清理任务
     */
    private static void startCleanupTask() {
        cleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupInactiveSessions();
            } catch (Exception e) {
                log.error("清理无效WebSocket会话时发生异常", e);
            }
        }, 1, 5, TimeUnit.MINUTES); // 每5分钟清理一次
    }

    /**
     * 清理无效会话
     */
    private static void cleanupInactiveSessions() {
        int cleanedCount = 0;
        
        for (Map.Entry<String, WebSocketSession> entry : sessionPool.entrySet()) {
            WebSocketSession session = entry.getValue();
            if (session == null || !session.isOpen()) {
                sessionPool.remove(entry.getKey());
                activeSessions.decrementAndGet();
                cleanedCount++;
                
                if (session != null) {
                    closeSession(session);
                }
            }
        }
        
        if (cleanedCount > 0) {
            log.info("清理了{}个无效WebSocket会话，当前活跃会话数: {}", cleanedCount, activeSessions.get());
        }
    }

    /**
     * 安全关闭会话
     */
    private static void closeSession(WebSocketSession session) {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                log.warn("关闭WebSocket会话时发生异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 会话统计信息
     */
    public static class SessionStats {
        private final int totalSessions;
        private final int activeSessions;
        private final int currentPoolSize;

        public SessionStats(int totalSessions, int activeSessions, int currentPoolSize) {
            this.totalSessions = totalSessions;
            this.activeSessions = activeSessions;
            this.currentPoolSize = currentPoolSize;
        }

        public int getTotalSessions() { return totalSessions; }
        public int getActiveSessions() { return activeSessions; }
        public int getCurrentPoolSize() { return currentPoolSize; }

        @Override
        public String toString() {
            return String.format("SessionStats{total=%d, active=%d, pool=%d}", 
                totalSessions, activeSessions, currentPoolSize);
        }
    }
}
