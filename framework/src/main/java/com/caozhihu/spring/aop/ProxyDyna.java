package com.caozhihu.spring.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyDyna implements InvocationHandler {

    // 织面
    private Object aspect;
    // 前置方法
    private Method before;
    // 后置方法
    private Method after;
    // 被代理对象 (目标对象)
    private Object target;
    // 被代理的方法
    private String targetMethod;

    public Object createProxy(Object aspect, Method before, Method after,
                              Object target, String targerMethod) {
        this.aspect = aspect;
        this.before = before;
        this.after = after;
        this.target = target;
        this.targetMethod = targerMethod;
        return Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!method.getName().equals(targetMethod)) {
            return method.invoke(this.target, args);
        }

        Object result;
        if (before != null) {
            // before() 简单起见，方法没有参数
            before.invoke(this.aspect);
        }
        result = method.invoke(target);
        if (after != null) {
            after.invoke(this.aspect);
        }
        return result;
    }
}
