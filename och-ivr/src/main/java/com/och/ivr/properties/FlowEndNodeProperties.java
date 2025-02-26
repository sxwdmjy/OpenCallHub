package com.och.ivr.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FlowEndNodeProperties extends FlowNodeProperties{


    private Boolean hangUp;
}
