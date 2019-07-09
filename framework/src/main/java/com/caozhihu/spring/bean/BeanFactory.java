package com.caozhihu.spring.bean;

import com.caozhihu.spring.aop.*;
import com.caozhihu.spring.web.mvc.Controller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author:czwbig
 * @date:2019/7/6 23:25
 * @description:
 */
public class BeanFactory {
    // Component 工厂容器，存放类即对应对象，Map(类定义，类对象)
    private static Map<Class<?>, Object> beans = new ConcurrentHashMap<>();

    // 带有 @AutoWired 注解修饰的属性的类
    private static Set<Class<?>> hasAutoWiredBeans =
            Collections.synchronizedSet(new HashSet<>());


    public static Object getBean(Class<?> cls) {
        return beans.get(cls);
    }

    /**
     * @author:czwbig
     * @date:2019/7/6 23:47
     * @description: 根据类列表 classList 来查找所有需要初始化的类并放入 Component 工厂，
     * 并且处理类中所有带 @AutoWired 注解的属性的依赖问题。
     */
    public static void initBean(List<Class<?>> classList) throws Exception {
        // 因为类定义后续处理类中 @RequestMapping 注解生成处理器时还要使用，
        // 因此这里要创建新容器，不能修改原引用
        List<Class<?>> classesToCreate = new ArrayList<>(classList);
        // 被 @Aspect 注解的切面类
        List<Class<?>> aspectClasses = new ArrayList<>();


        while (classesToCreate.size() != 0) {
            int remainSize = classesToCreate.size();
            for (int i = 0; i < classesToCreate.size(); i++) {

                if (classesToCreate.get(i).isAnnotationPresent(Aspect.class)) {
                    aspectClasses.add(classesToCreate.get(i));
                }

                if (finishCreate(classesToCreate.get(i))) {
                    classesToCreate.remove(i);
                }
            }

            // 如果某趟遍历后，没有初始化成功任何一个类，则是类之间循环依赖，
            // 简单起见，这种情况暂不处理
            // Spring 的循环依赖的理论依据其实是基于 Java 的引用传递，
            // 当我们获取到对象的引用时，对象的属性是可以延后设置的(但是构造器必须是在获取引用之前)。
            if (remainSize == classesToCreate.size()) {
                String msg = "无法继续初始化！\n" +
                        "可能是bean依赖陷入死循环，暂无法处理！" +
                        "\n未初始化的类集合：";
                msg += classesToCreate;
                msg += "\n已初始化类集合：" + beans;
                throw new Exception(msg);
            }
        }

        // 使用动态代理更新 Bean 工厂
        resolveAOP(aspectClasses);

        // 更新 Bean 工厂之前，可能有的类的依赖
        // 已经通过 @AutoWired 注解依赖注入了旧的未代理的对象,
        // 因此需要再次更新 Bean 工厂
        for (Class<?> bean : BeanFactory.hasAutoWiredBeans) {
            if (!finishCreate(bean)) {
                throw new Exception("AOP 代理对象更新后，无法更新依赖对象");
            }
        }
    }

    /**
     * @author:czwbig
     * @description: 使用 Class 对象来初始化，
     * 使用依赖注入处理 @AutoWired 注解修饰的属性
     */
    private static boolean finishCreate(Class<?> aClass) throws
            IllegalAccessException, InstantiationException {

        // 如果不需要初始化，即既不是 @Component 注解的类，
        // 也不是 @Controller 注解的类，就直接返回 true
        if (!aClass.isAnnotationPresent(Component.class)
                && !aClass.isAnnotationPresent(Controller.class)) {
            return true;
        }

        // 初始化对象
        Object bean = aClass.newInstance();
        // 使用依赖注入解决依赖
        // 遍历类中所有定义的属性，如果属性带有 @AutoWired 注解，则需要依赖注入
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(AutoWired.class)) {
                // 将需要注入Bean属性的类保存起来，AOP代理更新Bean工厂之后，需要更新它们
                BeanFactory.hasAutoWiredBeans.add(aClass);

                // 获取属性类型，即使用了 @AutoWired 注解的属性的引用定义类型
                Class<?> fieldType = field.getType();

                // 从 bean 工厂获取此需要装配进来的依赖类型，
                // 如果获取到 null，判断是否为接口类型，
                // 如果是接口类型则查询其实现类
                // 则无法初始化当前类，返回 false
                Object reliantBean = BeanFactory.getBean(fieldType);
                if (reliantBean == null) {
                    if (!fieldType.isInterface()) {
                        return false;
                    }
                    Set<Class<?>> classSet = BeanFactory.beans.keySet();
                    for (Class<?> clz : classSet) {
                        if (fieldType.isAssignableFrom(clz)) {
                            reliantBean = BeanFactory.getBean(clz);
                            break;
                        }
                    }
                }
                if (reliantBean == null) {
                    return false;
                }
                field.setAccessible(true);
                // 相当于 bean.field = reliantBean;
                field.set(bean, reliantBean);
            }
        }
        beans.put(aClass, bean);
        return true;
    }

    /**
     * @author:czwbig
     * @description: 对于所有被 @Aspect 注解修饰的类，
     * 遍历他们定义的方法，处理 @Pointcur、@Before 以及 @After 注解
     */
    private static void resolveAOP(List<Class<?>> aspectClasses)
            throws Exception {
        if (aspectClasses.size() == 0) {
            return;
        }

        for (Class<?> aClass : aspectClasses) {
            Method before = null;
            Method after = null;
            String method = null;
            Object target = null;
            String pointcutName = null;

            // 初始化对象，简单起见，这里先假定每一个代理类，
            // 并且最多只有一个切点，一个前置以及一个后置处理器
            Object bean = aClass.newInstance();
            for (Method m : bean.getClass().getDeclaredMethods()) {
                if (m.isAnnotationPresent(Pointcut.class)) {
                    // com.caozhihu.demo.Rapper.rap()
                    String pointcut = m.getAnnotation(Pointcut.class).value();
                    String classStr = pointcut.substring(0, pointcut.lastIndexOf("."));
                    target = Thread.currentThread().getContextClassLoader()
                            .loadClass(classStr).newInstance();
                    method = pointcut.substring(pointcut.lastIndexOf(".") + 1);
                    pointcutName = m.getName();
                }
            }

            for (Method m : bean.getClass().getDeclaredMethods()) {
                if (m.isAnnotationPresent(Before.class)) {
                    String value = m.getAnnotation(Before.class).value();
                    value = value.substring(0, value.indexOf("("));
                    if (value.equals(pointcutName)) {
                        before = m;
                    }
                } else if (m.isAnnotationPresent(After.class)) {
                    String value = m.getAnnotation(After.class).value();
                    value = value.substring(0, value.indexOf("("));
                    if (value.equals(pointcutName)) {
                        after = m;
                    }
                }
            }

            // 获取代理对象并更新 bean 工厂
            Object proxy = new ProxyDyna().createProxy(bean, before, after,
                    target, method.substring(0, method.indexOf("(")));
            // 检查被代理的类，是否已经被加载到 Bean 工厂
            if (!BeanFactory.beans.containsKey(target.getClass())) {
                throw new Exception("无法设置指定的被代理类：" + target.getClass());
            }
            BeanFactory.beans.put(target.getClass(), proxy);
        }
    }
}
