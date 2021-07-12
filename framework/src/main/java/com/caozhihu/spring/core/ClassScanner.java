package com.caozhihu.spring.core;

import java.io.IOException;
import java.net.JarURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassScanner {
    /**
     * 加载指定包名的类，例如 com.caozhihu.spring
     */
    public static List<Class<?>> scannerCLasses(String packageName) throws IOException {
        String path = packageName.replace(".", "/");
        // 线程上下文类加载器默认是应用类加载器，即 ClassLoader.getSystemClassLoader();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        /*
         使用类加载器对象的 getResources(ResourceName) 方法获取资源集
         如果是在 Jar 包中运行，这里可以获取到所有的类，因为 gradle 打包时会把两个模块打包到一起
         */
        return Collections.list(classLoader.getResources(path)).stream()
                .filter(url -> url.getProtocol().contains("jar"))
                .flatMap(url -> {
                    // 将打开的 url 返回的 URLConnection 转换成其子类 JarURLConnection 包连接
                    try {
                        JarURLConnection jarURLConnection = null;
                        jarURLConnection = (JarURLConnection) url.openConnection();
                        String jarFilePath = jarURLConnection.getJarFile().getName();
                        return getClassesFromJar(jarFilePath, path).stream();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return Stream.empty();
                }).collect(Collectors.toList());
    }

    private static List<Class<?>> getClassesFromJar(String jarFilePath, String path) throws IOException {
        List<String> classNames = Collections.list(new JarFile(jarFilePath).entries()).stream()
                .map(JarEntry::getName)
                .filter(entryName -> entryName.startsWith(path) && entryName.endsWith(".class"))
                .map(entryName -> entryName.replace("/", ".").substring(0, entryName.length() - 6))
                .collect(Collectors.toList());
        List<Class<?>> classes = new ArrayList<>();
        // 使用类的全限定类名初始化类，并将类对象保存
        try {
            for (String className : classNames) classes.add(Class.forName(className));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return classes;
    }
}
