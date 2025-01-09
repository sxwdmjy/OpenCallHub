package com.och.ivr.properties;

public interface FlowNodeProperties {

    default Long getAsrEngine(){return null;};
    default Long getTtsEngine(){return null;};

    default Integer getRouteType(){return null;};


    default String getRouteValue(){return null;}

    default Boolean getHangUp(){return null;}

    default Boolean getInterrupt(){return null;}
    default Long getFileId(){return null;}
    default String getFile(){return null;}
    default Integer getPlaybackType(){return null;}
    default String getContent(){return null;}
}
