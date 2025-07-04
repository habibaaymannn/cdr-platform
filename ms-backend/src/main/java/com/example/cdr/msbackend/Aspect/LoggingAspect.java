package com.example.cdr.msbackend.Aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.example.cdr.msbackend.Service..*(..)))")
    public void serviceMethods(){}

    @Before("serviceMethods()")
    public void before(JoinPoint joinPoint) {
        log.info("Entering method: " + joinPoint.getSignature().getName());
    }

    @AfterReturning(pointcut = "serviceMethods()",returning = "result")
    public void after(JoinPoint joinPoint, Object result) {
        log.info("Exiting method: " + joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = "serviceMethods()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Throwable e) {
        log.info("Exception for method: " + joinPoint.getSignature().getName());
    }
}
