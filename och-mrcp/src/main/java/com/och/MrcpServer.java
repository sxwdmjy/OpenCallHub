package com.och;

import com.och.sip.transport.SipServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MrcpServer {

    public static void main(String[] args) throws InterruptedException {
        SipServer sipServer = new SipServer();
        sipServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down MrcpServer");
            sipServer.stop();
        }));
    }
}
