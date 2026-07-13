package com.och.security.audit;

import com.och.common.trace.TraceContext;
import com.och.security.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

/**
 * 审计切面
 * <p>
 * 拦截标注了 {@link Audited} 注解的方法，在方法执行前后记录审计事件。
 * 无论方法是否抛异常，都会在 finally 块中写入审计记录，确保操作不被遗漏。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditSink auditSink;

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        long startTime = System.currentTimeMillis();
        Integer responseStatus = 200;

        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            responseStatus = 500;
            throw e;
        } finally {
            try {
                long duration = System.currentTimeMillis() - startTime;
                AuditEvent event = buildAuditEvent(audited, joinPoint, duration, responseStatus);
                auditSink.write(event);
            } catch (Exception e) {
                log.error("[AuditAspect] Failed to write audit event", e);
            }
        }
    }

    private AuditEvent buildAuditEvent(Audited audited, ProceedingJoinPoint joinPoint, long duration, Integer responseStatus) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        AuditEvent.AuditEventBuilder builder = AuditEvent.builder()
                .traceId(TraceContext.getTraceId())
                .action(audited.action())
                .targetType(audited.targetType())
                .createdAt(Instant.now())
                .durationMs(duration)
                .responseStatus(responseStatus);

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            builder.requestUri(request.getRequestURI())
                    .requestMethod(request.getMethod())
                    .clientIp(IpUtils.getIpAddr(request));
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        builder.requestParams(signature.getMethod().getName());

        return builder.build();
    }
}
