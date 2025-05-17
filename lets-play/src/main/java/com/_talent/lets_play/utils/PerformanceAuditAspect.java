package com._talent.lets_play.utils;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class PerformanceAuditAspect {

    @Around("execution(* com._talent.lets_play.services.impl.*.*(..))")
    public Object logServiceMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            log.info("Exécution de {}.{} complétée en {} ms", className, methodName, stopWatch.getTotalTimeMillis());
            return result;
        } catch (Throwable e) {
            stopWatch.stop();
            log.error("Exception lors de l'exécution de {}.{} après {} ms: {}",
                    className, methodName, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
}
