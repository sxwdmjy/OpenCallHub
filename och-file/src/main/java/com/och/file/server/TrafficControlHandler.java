package com.och.file.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.TimeUnit;

public class TrafficControlHandler extends ChannelDuplexHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 根据当前内存压力调整读取速度
        if (isHighMemoryUsage()) {
            ctx.channel().config().setAutoRead(false);
            scheduleResumeRead(ctx);
        }
        super.channelRead(ctx, msg);
    }

    private void scheduleResumeRead(ChannelHandlerContext ctx) {
        ctx.channel().eventLoop().schedule(() -> {
            if (!isHighMemoryUsage()) {
                ctx.channel().config().setAutoRead(true);
                ctx.channel().read();
            }
        }, 1, TimeUnit.SECONDS);
    }

    private boolean isHighMemoryUsage() {
        // 获取当前内存使用量
        long usedMemory = getUsedMemory();
        // 设置最高内存使用量
        long maxMemory = getMaxMemory();
        // 计算内存使用率
        double memoryUsage = (double) usedMemory / maxMemory;
        // 判断内存使用率是否超过阈值
        return memoryUsage > 0.8;
    }

    private long getUsedMemory() {
        // 获取当前内存使用量
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private long getMaxMemory() {
        // 获取最高内存使用量
        Runtime runtime = Runtime.getRuntime();
        return runtime.maxMemory();
    }
}
