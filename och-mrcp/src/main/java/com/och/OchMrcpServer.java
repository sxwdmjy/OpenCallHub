package com.och;

import com.och.config.EngineConfig;
import com.och.engine.AsrEngine;
import com.och.engine.CloudConfig;
import com.och.engine.CloudConfigManager;
import com.och.engine.EngineFactory;
import com.och.mrcp.MrcpServer;
import com.och.redis.RedissonManager;
import com.och.sip.transport.SipServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OchMrcpServer {

    public static void main(String[] args) throws InterruptedException {
        RedissonManager.getRedisson();
        SipServer sipServer = new SipServer();
        sipServer.start();
        MrcpServer mrcpServer = new MrcpServer();
        mrcpServer.start();
        CloudConfigManager.loadConfig(EngineConfig.getCloudConfigs());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down MrcpServer");
            sipServer.stop();
            mrcpServer.stop();
        }));
    }
}
