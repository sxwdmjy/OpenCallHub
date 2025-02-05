package com.och.rtp;

import com.och.config.MrcpConfig;

import java.util.BitSet;
import java.util.concurrent.locks.ReentrantLock;

public class PortPoolManager {

    private static PortPoolManager instance;
    private final BitSet portPool;   // 位图管理端口占用状态
    private final int minPort;
    private final int maxPort;
    private final ReentrantLock lock = new ReentrantLock();

    private PortPoolManager() {
        this.minPort = MrcpConfig.getRtpMinPort();
        this.maxPort = MrcpConfig.getRtpMaxPort();
        this.portPool = new BitSet(maxPort - minPort + 1);
    }

    public static synchronized PortPoolManager getInstance() {
        if (instance == null) {
            instance = new PortPoolManager();
        }
        return instance;
    }

    // 分配一个可用端口
    public int allocatePort() {
        lock.lock();
        try {
            int nextAvailable = portPool.nextClearBit(0);
            if (nextAvailable > (maxPort - minPort)) {
                throw new IllegalStateException("No available RTP ports in the configured range");
            }
            portPool.set(nextAvailable);
            return minPort + nextAvailable;
        } finally {
            lock.unlock();
        }
    }

    // 释放端口回池
    public void releasePort(int port) {
        if (port < minPort || port > maxPort) return;
        lock.lock();
        try {
            int index = port - minPort;
            portPool.clear(index);
        } finally {
            lock.unlock();
        }
    }
}
