package com.och.fileClient.handler;

import cn.hutool.core.io.FileUtil;
import com.och.fileClient.protocol.TransferProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

@Slf4j
public class FileReceiveHandler extends SimpleChannelInboundHandler<TransferProtocol> {

    private final String filePath;

    public FileReceiveHandler(String filePath) {
        if (StringUtil.isNullOrEmpty(filePath)){
            this.filePath = "/usr/local/freeswitch/sound/";
        }else {
            this.filePath = filePath;
        }
        File dir = new File(filePath);
        if (!dir.exists()){
            dir.mkdirs();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TransferProtocol msg) throws Exception {
        switch (msg.getType()){
            case FILE_TRANSFER:
                log.info("Received file transfer message");
                fileTransfer(ctx,msg);
                break;
            case FILE_END:
                log.info("Received file end message");
                break;
            default:
                log.info("Received unknown message");
        }
    }

    private void fileTransfer(ChannelHandlerContext ctx, TransferProtocol msg) {
        String fileName = msg.getFileName();
        if (StringUtil.isNullOrEmpty(fileName)) {
            log.info("Received empty file name");
            return;
        }
        File file = new File(filePath, fileName);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rw");
            raf.write(msg.getData());
        }catch (IOException e){
            log.error("File transfer failed: {}", e.getMessage());
        }finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {

                }
            }
        }
    }
}
