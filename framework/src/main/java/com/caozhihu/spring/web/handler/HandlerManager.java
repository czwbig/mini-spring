package com.caozhihu.spring.web.handler;

import com.caozhihu.spring.web.mvc.Controller;
import com.caozhihu.spring.web.mvc.RequestMapping;
import com.caozhihu.spring.web.mvc.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:czwbig
 * @date:2019/7/6 18:18
 * @description: 为各个类中的各个方法分配好的 HandlerMappering 对象
 * 调用 HandlerManager.resolveMappingHandler() 方法并传入类集合，
 * 就可使用 HandlerManager.mappingHandlerList 获取到所有类下所有需要映射方法的 MappingHandler 对象
 */
public class HandlerManager {

    // 类中各 @RequestMapping 注解修饰的方法对应的 MappingHandler 的集合
    public static List<MappingHandler> mappingHandlerList = new ArrayList<>();

    public static void resolveMappingHandler(List<Class<?>> classList) {
        for (Class<?> aClass : classList) {
            // 只处理包含了 @Controller 注解的类
            if (aClass.isAnnotationPresent(Controller.class)) {
                parseHandlerFromController(aClass);
            }
        }
    }

    private static void parseHandlerFromController(Class<?> aClass) {
        Method[] methods = aClass.getDeclaredMethods();
        // 只处理包含了 @RequestMapping 注解的方法
        for (Method method : methods) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                // 获取赋值 @RequestMapping 注解的值，也就是客户端请求的路径，注意，不包括协议名和主机名
                String uri = method.getDeclaredAnnotation(RequestMapping.class).value();
                List<String> params = new ArrayList<>();
                for (Parameter parameter : method.getParameters()) {
                    if (parameter.isAnnotationPresent(RequestParam.class)) {
                        params.add(parameter.getAnnotation(RequestParam.class).value());
                    }
                }

                // List.toArray() 方法传入与 List.size() 恰好一样大的数组，可以提高效率
                String[] paramsStr = params.toArray(new String[params.size()]);

                MappingHandler mappingHandler = new MappingHandler(uri, aClass, method, paramsStr);
                HandlerManager.mappingHandlerList.add(mappingHandler);
            }

        }

    }

}
