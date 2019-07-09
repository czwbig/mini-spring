package com.caozhihu.spring.core;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author:czwbig
 * @date:2019/7/6 16:55
 * @description:
 */
public class ClassScanner {
    /**
     * @param packageName 需要被加载的类的包名
     * @author:czwbig
     * @date:2019/7/6 17:10
     * @description: 加载指定包名的类，例如 com.caozhihu.spring
     */
    public static List<Class<?>> scannerCLasses(String packageName) throws IOException, ClassNotFoundException {
        List<Class<?>> classList = new ArrayList<>();
        String path = packageName.replace(".", "/");
        // 线程上下文类加载器默认是应用类加载器，即 ClassLoader.getSystemClassLoader();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // 古老的迭代器版本，可当成 Iterator 使用
        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            // 获取协议类型，判断是否为 jar 包
            if (url.getProtocol().contains("jar")) {
                // 将打开的 url 返回的 URLConnection 转换成其子类 JarURLConnection 包连接
                JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                String jarFilePath = jarURLConnection.getJarFile().getName();
                // 获取到 jar 包中的所有类
                classList.addAll(getClassesFromJar(jarFilePath, path));
            } else {
                // todo
            }
        }
        return classList;
    }

    private static List<Class<?>> getClassesFromJar(String jarFilePath, String path) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        JarFile jarFile = new JarFile(jarFilePath);
        Enumeration<JarEntry> jarEntrs = jarFile.entries();
        while (jarEntrs.hasMoreElements()) {
            JarEntry jarEntry = jarEntrs.nextElement();
            // com/caozhihu/spring/test/Test.class
            String entryName = jarEntry.getName();
            if (entryName.startsWith(path) && entryName.endsWith(".class")) {
                // 全限定类名
                String classFullName = entryName.replace("/", ".")
                        .substring(0, entryName.length() - 6);
                classes.add(Class.forName(classFullName));
            }
        }
        return classes;
    }
}
