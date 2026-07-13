package com.och.security.audit;

import com.och.common.trace.TraceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 审计切面测试
 * <p>
 * 验证审计切面在正常执行和异常执行时都会写入审计事件，
 * 这是后续权限、资源、导出和敏感操作审计的基础保障。
 * </p>
 *
 * @author danmo
 */
class AuditAspectTest {

    private final List<AuditEvent> events = new ArrayList<>();
    private final AuditAspect auditAspect = new AuditAspect(events::add);

    @AfterEach
    void clearTraceContext() {
        TraceContext.remove();
    }

    @Test
    void auditWritesSuccessEvent() throws Throwable {
        TraceContext.setTraceId("trace-001");
        ProceedingJoinPoint joinPoint = joinPoint("createSkill", "ok");
        Audited audited = audited("createSkill");

        Object result = auditAspect.audit(joinPoint, audited);

        assertEquals("ok", result);
        assertEquals(1, events.size());
        AuditEvent event = events.get(0);
        assertEquals("CREATE", event.getAction());
        assertEquals("SKILL", event.getTargetType());
        assertEquals("trace-001", event.getTraceId());
        assertEquals(200, event.getResponseStatus());
    }

    @Test
    void auditWritesFailureEvent() throws Throwable {
        TraceContext.setTraceId("trace-002");
        ProceedingJoinPoint joinPoint = joinPoint("deleteSkill", new IllegalStateException("failed"));
        Audited audited = audited("deleteSkill");

        assertThrows(IllegalStateException.class, () -> auditAspect.audit(joinPoint, audited));

        assertEquals(1, events.size());
        AuditEvent event = events.get(0);
        assertEquals("DELETE", event.getAction());
        assertEquals("SKILL", event.getTargetType());
        assertEquals("trace-002", event.getTraceId());
        assertEquals(500, event.getResponseStatus());
    }

    private ProceedingJoinPoint joinPoint(String methodName, Object result) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = AuditTarget.class.getDeclaredMethod(methodName);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        if (result instanceof Throwable throwable) {
            when(joinPoint.proceed()).thenThrow(throwable);
        } else {
            when(joinPoint.proceed()).thenReturn(result);
        }
        return joinPoint;
    }

    private Audited audited(String methodName) throws NoSuchMethodException {
        return AuditTarget.class.getDeclaredMethod(methodName).getAnnotation(Audited.class);
    }

    private static class AuditTarget {

        @Audited(action = "CREATE", targetType = "SKILL")
        void createSkill() {
        }

        @Audited(action = "DELETE", targetType = "SKILL")
        void deleteSkill() {
        }
    }
}
