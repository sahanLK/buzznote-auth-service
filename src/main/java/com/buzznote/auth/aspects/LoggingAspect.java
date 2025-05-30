package com.buzznote.auth.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class.getName());

    @Before("execution(* com.buzznote.auth.controller..*(..))")
    public void beforeAdvice(JoinPoint joinpoint) {
        LOGGER.info("Called: {}", joinpoint.getSignature());
    }

    @AfterReturning("execution(* com.buzznote.auth.controller..*(..))")
    public void afterAdvice(JoinPoint joinPoint) {
        LOGGER.info("Finished: {}", joinPoint.getSignature());
    }

}
