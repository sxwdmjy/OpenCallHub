package com.och.ivr.properties;

public interface FlowNodeProperties {

    default Long getAsrEngine(){return null;};
    default Long getTtsEngine(){return null;};

    default Integer getRouteType(){return null;};


    default String getRouteValue(){return null;}
}
