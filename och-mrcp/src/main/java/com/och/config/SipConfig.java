package com.och.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.time.Duration;

public class SipConfig {

    private static final Config config = ConfigFactory.load("sip.conf");

    public static String getServerAddress() {
        return config.getString("sip.server.address");

    }
    public static int getUdpPort() {
        return config.getInt("sip.transport.udp.port");
    }

    // 获取 TCP 端口
    public static int getTcpPort() {
        return config.getInt("sip.transport.tcp.port");
    }

    public static Duration getTimerA() {
        return config.getDuration("sip.transaction.timer-a");
    }

    // 获取 INVITE 事务超时时间
    public static Duration getInviteTimeout() {
        return config.getDuration("sip.transaction.invite-timeout");
    }

    // 获取非 INVITE 事务超时时间
    public static Duration getNonInviteTimeout() {
        return config.getDuration("sip.transaction.non-invite-timeout");
    }
}
