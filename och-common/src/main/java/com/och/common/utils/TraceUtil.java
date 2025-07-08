package com.och.common.utils;


import com.alibaba.ttl.TransmittableThreadLocal;
import org.slf4j.MDC;

/**
 * @author danmo
 * @date 2025/7/2 16:32
 */
public class TraceUtil {


    private static final TransmittableThreadLocal<String> CONTEXT_TRACE_ID = new TransmittableThreadLocal<>();

    public static final String TRACE_ID = "traceId";

    public static void setTraceId(String traceId) {
        CONTEXT_TRACE_ID.set(traceId);
        MDC.put(TRACE_ID, traceId);
    }

    public static String getTraceId() {
        return CONTEXT_TRACE_ID.get();
    }

    public static void clear() {
        CONTEXT_TRACE_ID.remove();
        MDC.clear();
    }

}
