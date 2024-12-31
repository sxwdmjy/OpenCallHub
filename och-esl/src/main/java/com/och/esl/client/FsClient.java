package com.och.esl.client;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.och.common.constant.EslConstant;
import com.och.common.enums.EslEventFormat;
import com.och.common.enums.GatewayTypeEnum;
import com.och.common.thread.ThreadFactoryImpl;
import com.och.esl.FsEslMsg;
import com.och.esl.propeties.FsClientProperties;
import com.och.esl.service.IFsEslEventService;
import com.och.system.domain.entity.FsConfig;
import com.och.system.domain.entity.FsSipGateway;
import com.och.system.domain.query.fsconfig.FsConfigQuery;
import com.och.system.service.IFsConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.freeswitch.esl.client.IEslEventListener;
import org.freeswitch.esl.client.inbound.Client;
import org.freeswitch.esl.client.inbound.InboundConnectionFailure;
import org.freeswitch.esl.client.transport.SendMsg;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.freeswitch.esl.client.transport.message.EslMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author danmo
 * @date 2023年06月29日 9:49
 */
@Slf4j
public class FsClient {

    //呼叫编码
    @Value("${freeswitch.codec:^^:G722:PCMU:PCMA}")
    private String codec;

    @Value("${freeswitch.sample.rate:8000}")
    protected String sampleRate;

    @Value("${freeswitch.group:}")
    private String groupName;


    private Map<String, Client> fsClientMap = new ConcurrentHashMap<>();

    private ScheduledExecutorService checkFsThread = new ScheduledThreadPoolExecutor(1, new ThreadFactoryImpl("check-fs-pool-%d"));


    private final FsClientProperties clientProperties;
    private final IFsConfigService iFsConfigService;


    @Autowired
    private IFsEslEventService ILfsEslEventService;

    public FsClient(IFsConfigService iFsConfigService, FsClientProperties clientProperties) {
        this.iFsConfigService = iFsConfigService;
        this.clientProperties = clientProperties;
    }


    public void init() {
        FsConfigQuery query = new FsConfigQuery();
        query.setGroup(groupName);
        List<FsConfig> fsConfigs = iFsConfigService.getList(query);
        for (FsConfig fsConfig : fsConfigs) {
            connect(fsConfig);
        }
        checkFsThread.scheduleAtFixedRate(() -> {
            try {
                checkConnect();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }, 2, 1, TimeUnit.MINUTES);

    }

    private void connect(FsConfig server) {
        String ipAndPort = server.getIp() + EslConstant.CO + server.getPort();
        Client client = new Client();
        //监听事件
        client.addEventListener(new IEslEventListener() {

            @Override
            public void eventReceived(EslEvent event) {
                sendRocketMqMsg(event);
            }

            @Override
            public void backgroundJobResultReceived(EslEvent event) {
                sendRocketMqMsg(event);
            }

            private void sendRocketMqMsg(EslEvent event) {
                FsEslMsg lfsEslMsg = FsEslMsg.builder().eslEvent(event).address(ipAndPort).build();
                ILfsEslEventService.eslEventPublisher(lfsEslMsg);
            }
        });
        //创建连接
        try {
            client.connect(server.getIp(), server.getPort(), server.getPassword(), server.getOutTime());
            client.setEventSubscriptions(EslEventFormat.PLAIN.getText(), "all");
            fsClientMap.put(ipAndPort, client);
            if (server.getStatus() == 1) {
                server.setStatus(0);
                updateFsConfigStatus(server);
            }
        } catch (InboundConnectionFailure e) {
            log.error("Connect failed msg:{}", e.getMessage(), e);
            if (server.getStatus() == 0) {
                server.setStatus(1);
                updateFsConfigStatus(server);
            }
        }
    }

    /**
     * fs连接状态检查
     */
    private void checkConnect() {
        FsConfigQuery query = new FsConfigQuery();
        query.setGroup(groupName);
        List<FsConfig> fsConfigs = iFsConfigService.getList(query);
        for (FsConfig server : fsConfigs) {
            String clientUrl = server.getIp() + ":" + server.getPort();
            if (fsClientMap.containsKey(clientUrl)) {
                Client client = fsClientMap.get(clientUrl);
                if (!client.canSend()) {
                    fsClientMap.remove(clientUrl);
                    client.close();

                    server.setStatus(1);
                    updateFsConfigStatus(server);
                }
            } else {
                connect(server);
            }
        }
    }

    private void updateFsConfigStatus(FsConfig server) {
        iFsConfigService.updateStatusById(server.getId(), server.getStatus());
    }


    public void destroy() {
        log.info("fsClient 实例关闭链接");
        fsClientMap.forEach((path, client) -> {
            if (client.canSend()) {
                client.close();
            }
        });
        checkFsThread.shutdown();
        fsClientMap = null;
        ILfsEslEventService.destroyThreadPool();
    }


    /**
     * 发送消息
     */
    public void sendMsg(String address, SendMsg msg) {
        if (fsClientMap.containsKey(address) && fsClientMap.get(address).canSend()) {
            fsClientMap.get(address).sendMessage(msg);
        }
    }

    /**
     * 异步发送消息
     */
    public String sendAsyncMsg(String address, String cmd, String args) {
        if (fsClientMap.containsKey(address) && fsClientMap.get(address).canSend()) {
            log.info("fs 发送异步消息 {}+{}", cmd, args);
            return fsClientMap.get(address).sendAsyncApiCommand(cmd, args);
        }
        return null;
    }

    /**
     * 发送消息
     */
    public EslMessage sendSyncMsg(String address, String cmd, String args) {
        if (fsClientMap.containsKey(address) && fsClientMap.get(address).canSend()) {
            return fsClientMap.get(address).sendSyncApiCommand(cmd, args);
        }
        return null;
    }

    /**
     * uuid应答
     *
     * @param address
     * @param uniqueId
     */
    public void answer(String address, String uniqueId) {
        SendMsg answer = new SendMsg(uniqueId);
        answer.addCallCommand(EslConstant.EXECUTE);
        answer.addExecuteAppName(EslConstant.ANSWER);
        sendMsg(address, answer);
    }

    /**
     * 挂机
     *
     * @param address
     * @param callId
     * @param uniqueId
     */
    public void hangupCall(String address, Long callId, String uniqueId) {
        if (StringUtils.isBlank(address) || StringUtils.isBlank(uniqueId)) {
            log.info("address:{} or uniqueId:{} is null", address, uniqueId);
            return;
        }

        SendMsg hangupMsg = new SendMsg(uniqueId);
        hangupMsg.addCallCommand(EslConstant.EXECUTE);
        hangupMsg.addExecuteAppName(EslConstant.HANGUP);
        hangupMsg.addExecuteAppArg(EslConstant.NORMAL_CLEARING);
        log.info("hangup call:{}, uniqueId:{}", callId, uniqueId);
        this.sendMsg(address, hangupMsg);
    }

    /**
     * 录音
     *
     * @param address
     * @param callId
     * @param uniqueId
     * @param filePath
     */
    public void record(String address, Long callId, String uniqueId, String filePath) {
        try {
            //设置8kHz采样率
            sendArgs(address, uniqueId, EslConstant.SET, EslConstant.RECORD_SAMPLE_RATE + sampleRate);
            //双声道录音
            sendSyncMsg(address, EslConstant.SETVAR, uniqueId + EslConstant.RECORD_STEREO);
            //关闭缓存
            //sendSyncMsg(address, EslConstant.SETVAR, uniqueId + EslConstant.ENABLE_FILE_WRITE_BUFFERING);
            //桥接后录音
            sendSyncMsg(address, EslConstant.SETVAR, uniqueId + EslConstant.RECORD_CHECK_BRIDGE);
            //开启线程录音
            //sendSyncMsg(address, EslConstant.SETVAR, uniqueId + EslConstant.RECORD_USE_THREAD);
            sendSyncMsg(address, EslConstant.RECORD, uniqueId + " " + EslConstant.START + " " + filePath);
            log.info("FS开始录音 callId:{}, uniqueId:{}, record:{}", callId, uniqueId, filePath);
        } catch (Exception e) {
            log.error("FS开始录音异常 callId:{}, uniqueId:{}, ex:{}", callId, uniqueId, e.getMessage(), e);
        }
    }

    /**
     * 桥接
     *
     * @param address
     * @param callId
     * @param uniqueId
     * @param otherUniqueId
     */
    public void bridgeCall(String address, Long callId, String uniqueId, String otherUniqueId) {
        sendArgs(address, uniqueId, EslConstant.SET, EslConstant.PARK_AFTER_BRIDGE);
        sendArgs(address, uniqueId, EslConstant.SET, EslConstant.HANGUP_AFTER_BRIDGE);
        sendArgs(address, otherUniqueId, EslConstant.SET, EslConstant.HANGUP_AFTER_BRIDGE);
        sendArgs(address, otherUniqueId, EslConstant.SET, EslConstant.PARK_AFTER_BRIDGE);
        sendAsyncMsg(address, EslConstant.BRIDGE, uniqueId + EslConstant.SPACE + otherUniqueId);
    }

    /**
     * update call
     *
     * @param address
     * @param uniqueId
     * @param name
     * @param arg
     */
    public void sendArgs(String address, String uniqueId, String name, String arg) {
        SendMsg msg = new SendMsg(uniqueId);
        msg.addCallCommand(EslConstant.EXECUTE);
        msg.addExecuteAppName(name);
        msg.addExecuteAppArg(arg);
        msg.addGenericLine("async", "true");
        sendMsg(address, msg);
    }


    /**
     * 创建呼叫
     *
     * @param callId        呼叫ID
     * @param called        呼叫号码
     * @param calledDisplay 呼叫显号
     * @param uniqueId      通道ID
     * @param timeOut       超时时间
     * @param callRoute     路由信息
     */
    public void makeCall(Long callId, String called, String calledDisplay, String uniqueId, Integer timeOut, FsSipGateway callRoute) {
        if (StringUtils.isBlank(called)) {
            log.warn("called:{} is null ", called);
            return;
        }
        String address = RandomUtil.randomEle(new ArrayList<>(fsClientMap.keySet()));
        makeCall(address, callId, called, calledDisplay, uniqueId, timeOut, callRoute);
    }

    public void makeCall(String address, Long callId, String called, String calledDisplay, String uniqueId, Integer timeOut, FsSipGateway callRoute) {
        if (StringUtils.isBlank(called)) {
            log.warn("called:{} is null ", called);
            return;
        }

        StringBuilder builder = new StringBuilder();
        //获取路由网关地址
        String destination = called + Constants.AT + callRoute.getRealm();
        builder.append("{return_ring_ready=true").append(",")
                .append("sip_contact_user=").append(calledDisplay).append(",")
                .append("ring_asr=true").append(",")
                .append("fire_asr_events=true").append(",")
                .append("absolute_codec_string=").append(codec).append(",")
                .append("origination_caller_id_number=").append(calledDisplay).append(",")
                .append("origination_caller_id_name=").append(calledDisplay).append(",")
                .append("origination_uuid=").append(uniqueId);
        if (timeOut != null) {
            builder.append(",").append("originate_timeout=").append(timeOut);
        }
        builder.append("}")
                .append(EslConstant.SOFIA + "/")
                .append("gateway")
                //.append(GatewayTypeEnum.getByType(lfsCallRouteRelVo.getGatewayType()).getDesc())
                .append("/")
                .append(callRoute.getName())
                .append("/")
                .append(called)
                .append(EslConstant.PARK);
        sendAsyncMsg(address, EslConstant.ORIGINATE, builder.toString());
    }

    public void makeCall(String address, Long callId, String called, String calledDisplay, String uniqueId, Integer timeOut, String gatewayAddress) {
        if (StringUtils.isBlank(called)) {
            log.warn("called:{} is null ", called);
            return;
        }

        StringBuilder builder = new StringBuilder();
        //获取路由网关地址
        String destination = called + Constants.AT + gatewayAddress;
        builder.append("{return_ring_ready=true").append(",")
                .append("sip_contact_user=").append(calledDisplay).append(",")
                .append("ring_asr=true").append(",")
                .append("fire_asr_events=true").append(",")
                .append("absolute_codec_string=").append(codec).append(",")
                .append("origination_caller_id_number=").append(calledDisplay).append(",")
                .append("origination_caller_id_name=").append(calledDisplay).append(",")
                .append("origination_uuid=").append(uniqueId);
        if (timeOut != null) {
            builder.append(",").append("originate_timeout=").append(timeOut);
        }
        builder.append("}").append(EslConstant.SOFIA + "/").append(GatewayTypeEnum.EXTERNAL.getDesc()).append("/").append(destination).append(EslConstant.PARK);
        sendAsyncMsg(address, EslConstant.ORIGINATE, builder.toString());
    }

    /**
     * 停止放音
     *
     * @param address
     * @param uniqueId
     */
    public void playBreak(String address, String uniqueId) {
        SendMsg playback = new SendMsg(uniqueId);
        playback.addCallCommand(EslConstant.EXECUTE);
        playback.addExecuteAppName(EslConstant.BREAK_);
        this.sendMsg(address, playback);
    }

    /**
     * 播放音乐
     *
     * @param address
     * @param uniqueId
     * @param file
     */
    public void playFile(String address, String uniqueId, String file) {
        SendMsg playfile = new SendMsg(uniqueId);
        playfile.addCallCommand(EslConstant.EXECUTE);
        playfile.addExecuteAppName(EslConstant.PLAYBACK);
        playfile.addExecuteAppArg(file);
        this.sendMsg(address, playfile);
    }

    /**
     * 开启ASR
     *
     * @param address
     * @param callId
     * @param uniqueId
     */
    public void detectSpeech(String address, Long callId, String uniqueId) {
        SendMsg speech = new SendMsg(uniqueId);
        speech.addCallCommand(EslConstant.EXECUTE);
        speech.addExecuteAppName(EslConstant.DETECT_SPEECH);
        speech.addExecuteAppArg(EslConstant.UNIMRCP + "ali-mrcpserver alimrcp default");
        this.sendMsg(address, speech);
    }

    public void detectSpeechResume(String address, Long callId, String uniqueId) {
        SendMsg speech = new SendMsg(uniqueId);
        speech.addCallCommand(EslConstant.EXECUTE);
        speech.addExecuteAppName(EslConstant.DETECT_SPEECH);
        speech.addExecuteAppArg(EslConstant.RESUME);
        this.sendMsg(address, speech);
    }
}
