package com.finance.backend.aspect;

import com.finance.backend.entity.AuditLog;
import com.finance.backend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLoggingAspect {

    private final AuditLogService auditLogService;

    @Around("execution(* com.finance.backend.service.*.*(..)) && " +
            "(execution(* create*(..)) || execution(* update*(..)) || " +
            "execution(* delete*(..)) || execution(* patch*(..)))")
    public Object logAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Skip audit log service itself to prevent recursion
        if (className.equals("AuditLogService")) {
            return joinPoint.proceed();
        }

        String action = deriveAction(methodName);
        String entityType = deriveEntityType(className);

        // Extract current user
        String userEmail = "system";
        Long userId = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                userEmail = auth.getName();
            }
        } catch (Exception e) {
            log.debug("Could not extract user for audit log", e);
        }

        Object result;
        try {
            result = joinPoint.proceed();

            // Try to extract entity ID from result
            Long entityId = extractEntityId(result);

            try {
                AuditLog auditLog = AuditLog.builder()
                        .userId(userId)
                        .userEmail(userEmail)
                        .action(action)
                        .entityType(entityType)
                        .entityId(entityId)
                        .details("Method: " + methodName + " executed successfully")
                        .build();
                auditLogService.createAuditLog(auditLog);
            } catch (Exception e) {
                // Never block the main operation — catch logging errors silently
                log.error("Failed to save audit log", e);
            }

            return result;

        } catch (Throwable ex) {
            try {
                AuditLog auditLog = AuditLog.builder()
                        .userId(userId)
                        .userEmail(userEmail)
                        .action(action + "_FAILED")
                        .entityType(entityType)
                        .details("Method: " + methodName + " failed: " + ex.getMessage())
                        .build();
                auditLogService.createAuditLog(auditLog);
            } catch (Exception logEx) {
                log.error("Failed to save audit log for failed operation", logEx);
            }
            throw ex;
        }
    }

    private String deriveAction(String methodName) {
        // createTransaction → CREATE_TRANSACTION
        StringBuilder action = new StringBuilder();
        for (char c : methodName.toCharArray()) {
            if (Character.isUpperCase(c)) {
                action.append('_');
            }
            action.append(Character.toUpperCase(c));
        }
        return action.toString();
    }

    private String deriveEntityType(String serviceName) {
        // TransactionService → Transaction
        return serviceName.replace("Service", "");
    }

    private Long extractEntityId(Object result) {
        if (result == null) return null;
        try {
            Method getId = result.getClass().getMethod("getId");
            Object id = getId.invoke(result);
            if (id instanceof Long) return (Long) id;
        } catch (Exception e) {
            // Not all results have getId — that's fine
        }
        return null;
    }
}
