package com.caozhihu.spring.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Pointcut {
    /**
     * 定义切点的名字，spring中这里还会用到 execution() 等表达式来解析
     * 简单起见，我们只能定义一个字符串表示切点位置，
     * 如 com.caozhihu.demo.Rapper.rap()
     */
    String value() default "";
}
