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
        System.out.println("TRIGGERED!!!");
        log.info("Starting execution: {}", joinPoint.getSignature().getName());

        try {
            Object result = joinPoint.proceed();
            return result;
        }finally {
            long timeTaken = System.currentTimeMillis() - startTime;
            log.info("Finished {}. Performance: {} ms", joinPoint.getSignature().getName(), timeTaken);
        }
    }
}
