package com.caozhihu.spring.aspect;


import com.caozhihu.spring.aop.After;
import com.caozhihu.spring.aop.Aspect;
import com.caozhihu.spring.aop.Before;
import com.caozhihu.spring.aop.Pointcut;
import com.caozhihu.spring.bean.Component;

@Aspect
@Component
public class RapAspect {

    // 定义切点，spring的实现中，
    // 此注解可以使用表达式 execution() 通配符匹配切点，
    // 简单起见，我们先实现明确到方法的切点
    @Pointcut("com.caozhihu.spring.service.serviceImpl.Rapper.rap()")
    public void rapPoint() {
    }

    @Before("rapPoint()")
    public void singAndDance() {
        System.out.println(" before rap");
    }

    @After("rapPoint()")
    public void basketball() {
        System.out.println("after rap");
    }

    /*// @Around 是环绕通知，简单起见，我们选择实现 @Before 和 @After
    @Around("execution(* com.caozhihu.demo..*(..))")
    public Object rapPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        // 在 rap 之前要先唱、跳
        System.out.println("prepare to :" + joinPoint.getSignature().getName());
        System.out.println("singing!");
        Object object = joinPoint.proceed();
        // 在 rap 之后要篮球
        System.out.println("basketball!");
        return object;
    }*/

}
