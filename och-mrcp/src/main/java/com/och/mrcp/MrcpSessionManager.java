package com.och.mrcp;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MrcpSessionManager {

    private static final MrcpSessionManager INSTANCE = new MrcpSessionManager();
    private final Map<String, MrcpSession> sessions = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static MrcpSessionManager getInstance() {
        return INSTANCE;
    }

    public MrcpSessionManager() {
        // 每5分钟清理一次无效会话
        scheduler.scheduleAtFixedRate(this::cleanupInactiveSessions, 5, 5, TimeUnit.MINUTES);
    }

    public void createSession(String sessionId, MrcpSession session) {
        sessions.put(sessionId, session);
    }

    public void createSession(String channelId, String serverIp, int port) {
        if (sessions.containsKey(channelId)) {
            log.info("MRCP session already exists: {}", channelId);
            return;
        }
        MrcpSession mrcpSession = new MrcpSession(channelId, serverIp, port);
        sessions.put(channelId, mrcpSession);
    }



    private void cleanupInactiveSessions() {
        sessions.entrySet().removeIf(entry ->
                entry.getValue().getState().get() == MrcpSession.State.CLOSED
        );
    }

    // 显式销毁会话
    public void destroySession(String sessionId) {
        MrcpSession session = sessions.remove(sessionId);
        if (session != null) {
            session.close(); // 触发MRCP协议终止流程
            log.info("MRCP session {} terminated", sessionId);
        }
    }

    // 新增会话检索和销毁方法
    public MrcpSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }




}
