package com.agileboot.infrastructure.log;

import cn.hutool.json.JSONUtil;
import com.agileboot.common.utils.jackson.JacksonUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;


/**
 * @author valarchie
 */
@Aspect
@Component
@Slf4j
public class MethodLogAspect {

    private static final int MAX_TEXT_LENGTH = 200;

    @Pointcut("execution(public * com.agileboot..db.*Service.*(..))")
    public void dbService() {
    }

    @Around("dbService()")
    public Object aroundDbService(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        if (log.isTraceEnabled()) {
            log.trace("DB SERVICE : {} ; ARGS: {} ; RESULT: {} ; COST: {}ms",
                joinPoint.getSignature().toShortString(),
                summarizeArgs(joinPoint.getArgs()),
                summarizeObject(proceed),
                System.currentTimeMillis() - start);
        }
        return proceed;
    }

    @AfterThrowing(value = "dbService()", throwing = "e")
    public void afterDbServiceThrow(JoinPoint joinPoint, Exception e) {
        log.error("DB SERVICE : {} ; ARGS: {} ; EXCEPTION: {} ; MESSAGE: {}",
            joinPoint.getSignature().toShortString(),
            summarizeArgs(joinPoint.getArgs()),
            e.getClass().getSimpleName(),
            truncate(e.getMessage()));
    }


    @Pointcut("bean(*ApplicationService)")
    public void applicationServiceLog() {
    }

    @Around("applicationServiceLog()")
    public Object aroundApplicationService(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        if (log.isTraceEnabled()) {
            log.trace("APPLICATION SERVICE : {} ; ARGS: {} ; RESULT: {} ; COST: {}ms",
                joinPoint.getSignature().toShortString(),
                summarizeArgs(joinPoint.getArgs()),
                summarizeObject(proceed),
                System.currentTimeMillis() - start);
        }
        return proceed;
    }

    @AfterThrowing(value = "applicationServiceLog()", throwing = "e")
    public void afterApplicationServiceThrow(JoinPoint joinPoint, Exception e) {
        log.error("APPLICATION SERVICE : {} ; ARGS: {} ; EXCEPTION: {} ; MESSAGE: {}",
            joinPoint.getSignature().toShortString(),
            summarizeArgs(joinPoint.getArgs()),
            e.getClass().getSimpleName(),
            truncate(e.getMessage()));
    }


    private String summarizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("arg").append(i).append("=").append(summarizeObject(args[i]));
        }
        return sb.append("]").toString();
    }

    private String summarizeObject(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof IPage<?>) {
            IPage<?> page = (IPage<?>) value;
            int recordsSize = page.getRecords() == null ? 0 : page.getRecords().size();
            return String.format("IPage(current=%d,size=%d,total=%d,pages=%d,records=%d)",
                page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), recordsSize);
        }
        if (value instanceof CharSequence) {
            return "String(len=" + ((CharSequence) value).length() + ")";
        }
        if (value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) {
            return String.valueOf(value);
        }
        if (value instanceof Collection<?>) {
            return value.getClass().getSimpleName() + "(size=" + ((Collection<?>) value).size() + ")";
        }
        if (value instanceof Map<?, ?>) {
            return value.getClass().getSimpleName() + "(size=" + ((Map<?, ?>) value).size() + ")";
        }
        if (value.getClass().isArray()) {
            return value.getClass().getComponentType().getSimpleName() + "[](len=" + Array.getLength(value) + ")";
        }
        String json = null;
        try {
            json = JacksonUtil.to(value);
        } catch (Exception e) {
            json = JSONUtil.toJsonStr(value);
        }
        if (json == null || json.trim().isEmpty()) {
            return value.getClass().getSimpleName();
        }
        return value.getClass().getSimpleName() + "(" + truncate(maskSensitive(json)) + ")";
    }

    private String maskSensitive(String text) {
        if (text == null) {
            return null;
        }
        return text
            .replaceAll("(?i)\\\"password\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\"password\":\"***\"")
            .replaceAll("(?i)\\\"pwd\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\"pwd\":\"***\"")
            .replaceAll("(?i)\\\"token\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\"token\":\"***\"")
            .replaceAll("(?i)\\\"authorization\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\"authorization\":\"***\"")
            .replaceAll("(?i)\\\"secret\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\"secret\":\"***\"")
            .replaceAll("(?i)\\\"email\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\"email\":\"***\"")
            .replaceAll("(?i)\\\"phone\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\"phone\":\"***\"")
            .replaceAll("(?i)\\\"mobile\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\"mobile\":\"***\"");
    }

    private String truncate(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() <= MAX_TEXT_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_TEXT_LENGTH) + "...(truncated)";
    }


}
