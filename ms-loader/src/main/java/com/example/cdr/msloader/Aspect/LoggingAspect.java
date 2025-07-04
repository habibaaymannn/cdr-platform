package com.example.cdr.msloader.Aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.example.cdr.msloader.Service..*(..)) || execution(* com.example.cdr.msloader.Processor..*(..))")
    public void serviceAndProcessorMethods(){}

    @Before("serviceAndProcessorMethods()")
    public void before(JoinPoint joinPoint) {
        log.info("Entering method: " + joinPoint.getSignature().getName());
    }

    @AfterReturning(pointcut = "serviceAndProcessorMethods()",returning = "result")
    public void after(JoinPoint joinPoint, Object result) {
        log.info("Exiting method: " + joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = "serviceAndProcessorMethods()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Throwable e) {
        log.info("Exception for method: " + joinPoint.getSignature().getName());
    }
}
