package com.caozhihu.spring.web.handler;

import com.caozhihu.spring.web.mvc.Controller;
import com.caozhihu.spring.web.mvc.RequestMapping;
import com.caozhihu.spring.web.mvc.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandlerManager {

    // 类中各 @RequestMapping 注解修饰的方法对应的 MappingHandler 的集合
    public static List<MappingHandler> mappingHandlerList = new ArrayList<>();

    public static void resolveMappingHandler(List<Class<?>> classList) {
        classList.stream().filter(aClass -> aClass.isAnnotationPresent(Controller.class))
                .forEach(HandlerManager::parseHandlerFromController);
    }

    private static void parseHandlerFromController(Class<?> aClass) {
        // 只处理包含了 @RequestMapping 注解的方法
        Arrays.stream(aClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .forEach(method -> {
                    // 获取赋值 @RequestMapping 注解的值，也就是客户端请求的路径，注意，不包括协议名和主机名
                    String uri = method.getDeclaredAnnotation(RequestMapping.class).value();
                    String[] paramsStr = Arrays.stream(method.getParameters())
                            .filter(parameter -> parameter.isAnnotationPresent(RequestParam.class))
                            .map(parameter -> parameter.getAnnotation(RequestParam.class).value())
                            .toArray(String[]::new);
                    MappingHandler mappingHandler = new MappingHandler(uri, aClass, method, paramsStr);
                    HandlerManager.mappingHandlerList.add(mappingHandler);
                });
    }

}
