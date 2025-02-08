package com.och.sip.sdp;

import java.util.concurrent.ConcurrentHashMap;

public class MediaSessionManager {

    private static final MediaSessionManager INSTANCE = new MediaSessionManager();
    private final ConcurrentHashMap<String, MediaSession> sessions = new ConcurrentHashMap<>();

    public static MediaSessionManager getInstance() {
        return INSTANCE;
    }

    public void createSession(String remoteIp, int remotePort, String codec, int clockRate) {
        String sessionId = generateSessionId(remoteIp, remotePort);
        MediaSession session = new MediaSession(remoteIp, remotePort, codec, clockRate);
        sessions.put(sessionId, session);
        session.start(); // 启动媒体传输
    }

    private String generateSessionId(String ip, int port) {
        return ip + ":" + port;
    }

    // 媒体会话封装类
    public static class MediaSession {
        private final String remoteIp;
        private final int remotePort;
        private final String codec;
        private final int clockRate;

        public MediaSession(String remoteIp, int remotePort, String codec, int clockRate) {
            this.remoteIp = remoteIp;
            this.remotePort = remotePort;
            this.codec = codec;
            this.clockRate = clockRate;
        }

        public void start() {
            // 实际媒体传输逻辑（如启动RTP流）
            System.out.printf("Media session started: %s:%d, codec=%s/%dHz\n",
                    remoteIp, remotePort, codec, clockRate);
        }
    }
}
