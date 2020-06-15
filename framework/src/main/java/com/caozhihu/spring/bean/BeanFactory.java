package com.caozhihu.spring.bean;

import com.caozhihu.spring.aop.*;
import com.caozhihu.spring.web.mvc.Controller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author czwbig
 * @since 2020/3/1
 */
public class BeanFactory {
    private static Map<Class<?>, Object> beans = new ConcurrentHashMap<>();
    /**
     * 带有 @AutoWired 注解修饰的属性的类
     */
    private static Set<Class<?>> beansHasAutoWiredField = Collections.synchronizedSet(new HashSet<>());

    public static Object getBean(Class<?> cls) {
        return beans.get(cls);
    }

    /**
     * 根据类列表 classList 来查找所有需要初始化的类并放入 Component 工厂，
     * 并且处理类中所有带 @AutoWired 注解的属性的依赖问题。
     */
    public static void initBean(List<Class<?>> classList) throws Exception {
        // 因为类定义后续处理类中 @RequestMapping 注解生成处理器时还要使用，
        // 因此这里要创建新容器，不能修改原引用
        List<Class<?>> classesToCreate = new ArrayList<>(classList);
        // 被 @Aspect 注解的切面类
        List<Class<?>> aspectClasses = new ArrayList<>();

        for (Class<?> aClass : classesToCreate) {
            if (aClass.isAnnotationPresent(Aspect.class)) {
                aspectClasses.add(aClass);
            } else {
                createBean(aClass);
            }
        }
        // 使用动态代理处理AOP
        resolveAOP(aspectClasses);

        // 有的类中某个属性已经通过 @AutoWired 注入了旧的被代理的对象,重新创建它们
        for (Class<?> aClass : beansHasAutoWiredField) {
            createBean(aClass);
        }
    }

    /**
     * 通过 Class 对象创建实例
     *
     * @param aClass 需要创建实例的 Class 对象
     */
    private static void createBean(Class<?> aClass) throws IllegalAccessException, InstantiationException {
        // 只处理 @Component / @Controller 注解的类
        if (!aClass.isAnnotationPresent(Component.class)
                && !aClass.isAnnotationPresent(Controller.class)) {
            return;
        }
        // 初始化对象
        Object bean = aClass.newInstance();
        // 遍历类中所有定义的属性，如果属性带有 @AutoWired 注解，则需要注入对应依赖
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(AutoWired.class)) {
                continue;
            }
            // 将需要注入其他 Bean 的类保存起来，因为等 AOP 代理类生成之后，需要更新它们
            BeanFactory.beansHasAutoWiredField.add(aClass);
            Class<?> fieldType = field.getType();
            field.setAccessible(true);
            if (fieldType.isInterface()) {
                // 如果依赖的类型是接口，则查询其实现类,
                // class1.isAssignableFrom(class2) = true 代表class2是class1类型，可分配class2对象给class
                for (Class<?> key : BeanFactory.beans.keySet()) {
                    if (fieldType.isAssignableFrom(key)) {
                        fieldType = key;
                        break;
                    }
                }
            }
            field.set(bean, BeanFactory.getBean(fieldType));
        }
        // todo 这里可能AutoWired注入失败，例如存在循环依赖，或者bean工厂中根本不存在，目前暂时先不处理
        beans.put(aClass, bean);
    }

    /**
     * 对于所有被 @Aspect 注解修饰的类，
     * 遍历他们定义的方法，处理 @Pointcut、@Before 以及 @After 注解
     */
    private static void resolveAOP(List<Class<?>> aspectClasses)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
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
            // 并且最多只有一个切点，一个前置以及一个后置处理器，所以我们也必需先处理 pointcut，再解析before和after方法
            Object bean = aClass.newInstance();
            for (Method m : bean.getClass().getDeclaredMethods()) {
                if (m.isAnnotationPresent(Pointcut.class)) {
                    // com.caozhihu.demo.Rapper.rap()
                    String pointcut = m.getAnnotation(Pointcut.class).value();
                    String classStr = pointcut.substring(0, pointcut.lastIndexOf("."));
                    target = Thread.currentThread().getContextClassLoader().loadClass(classStr).newInstance();
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
            BeanFactory.beans.put(target.getClass(), proxy);
        }
    }
}
