package com.och.websocket.domain;


import com.alibaba.fastjson2.JSONObject;
import lombok.Builder;
import lombok.Data;

/**
 * @author danmo
 * @date 2025/05/27 17:59
 */
@Builder
@Data
public class WsMsgPayload {

    //会话ID
    private String sessionId;

    //时间戳
    private Long timestamp;

    //事件类型
    private WsEventEnum event;

    //数据
    private String data;

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
