package com.och.ivr.properties;

public interface FlowNodeProperties {

    default Long geAsrEngine(){return null;};
    default Long geTtsEngine(){return null;};

    default Integer getRouteType(){return null;};

    default String getValue(){
        return null;
    };

    default String getRouteValue(){return null;}
}
