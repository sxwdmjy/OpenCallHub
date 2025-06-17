package com.och.common.domain;

import lombok.Data;

@Data
public class DetectedSpeech {

    private Interpretation interpretation;

    @Data
    public static class Interpretation {
        private String input;
        private Instance instance;
    }

    @Data
    public static class Instance {
        private String result;
        private String beginTime;
        private String endTime;
        private String waveformUri;
        private String taskId;
    }
}
