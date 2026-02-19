package com.example.demo.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    @Pointcut("execution(* com.example.demo.services.*.*(..))")
    public void serviceMethods(){}

    @Around("serviceMethods()")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable{
        long startTime = System.currentTimeMillis();
        log.debug("Starting execution: {}", joinPoint.getSignature().getName());

        try {
            return joinPoint.proceed();
        } finally {
            long timeTaken = System.currentTimeMillis() - startTime;
            log.info("Finished execution of {} - took {} ms", joinPoint.getSignature().getName(), timeTaken);
        }
    }
}