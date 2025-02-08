package com.och.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EngineFactory {

    // TTS引擎映射表
    private static final Map<String, TtsEngine> ttsEngines = Map.of(
            "aliyun", new AliyunTtsEngine(),
            "tencent", new TencentTtsEngine()
    );

    // ASR引擎映射表
    private static final Map<String, AsrEngine> asrEngines = Map.of(
            "aliyun", new AliyunAsrEngine(),
            "tencent", new TencentAsrEngine()
    );

    private static final  Map<String,AsrEngine> mrcpAsrEngine = new ConcurrentHashMap<>(16);


    public static TtsEngine getTtsEngine(String platform) {
        return ttsEngines.get(platform);
    }

    public static AsrEngine getAsrEngine(String platform) {
        return asrEngines.get(platform);
    }


    public static AsrEngine getMrcpAsrEngine(String mrcpSessionId) {
    	return mrcpAsrEngine.get(mrcpSessionId);
    }

    public static void addMrcpAsrEngine(String mrcpSessionId, AsrEngine asrEngine) {
    	mrcpAsrEngine.put(mrcpSessionId, asrEngine);
    }

}
