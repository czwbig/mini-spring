# mini-spring

讲道理，感觉自己有点菜。Spring 源码看不懂，不想强行解释，等多积累些项目经验之后再看吧，但是 Spring 中的控制反转（IOC）和面向切面编程（AOP）思想很重要，为了更好的使用 Spring 框架，有必要理解这两个点，为此，我使用 JDK API 实现了一个玩具级的简陋 IOC/AOP 框架 mini-spring，话不多说，直接开干。

# 环境搭建&快速使用

全部代码已上传 GitHub：[https://github.com/czwbig/mini-spring](https://github.com/czwbig/mini-spring)  

1. 将代码弄到本地并使用 IDE 打开，这里我们用 IDEA；
2. 使用 Gradle 构建项目，可以使用 IDEA 提供的 GUI 操作，也可以直接使用 `gradle build` 命令；

![](https://upload-images.jianshu.io/upload_images/14923529-d777a3d60ab478bd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

3. 如下图，右击 `mini-spring\framework_use_test\build\libs\framework_use_test-1.0-SNAPSHOT.jar` ，点击 Run，当然也可以直接使用 `java -jar jarPath.jar` 命令来运行此 jar 包；

![](https://upload-images.jianshu.io/upload_images/14923529-c1fc604fa2cdecd8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

4. 浏览器打开 `localhost:8080/rap` 即可观察到显示 CXK 字母，同时 IDE 控制台会输出：  

```text
first,singing <chicken is too beautiful>.
and the chicken monster is dancing now.
CXK rapping...
oh! Don't forget my favorite basketball.
```  
  
下面开始框架的讲解。  

**注意调式的时候也一定要运行 Jar 包，因为 Jar 包中才包含所有类，否则扫描类会出问题**   

# 简介

本项目使用 Java API 以及内嵌 Tomcat 服务器写了一个玩具级 IOC/AOP web 框架。实现了 `@Controller`、`@AutoWired`、`@Component` 、`@Pointcut`、`@Aspect`、`@Before`、`@After` 等 Spring 常用注解。可实现简单的访问 uri 映射，控制反转以及不侵入原代码的面向切面编程。  

讲解代码实现之前，假设读者已经掌握了基础的项目构建、反射、注解，以及 JDK 动态代理知识，项目精简，注释详细，并且总代码 + 注释不足 1000 行，适合用来学习。其中构建工具 Gradle 没用过也不要紧，我也是第一次使用，当成没有 xml 的 Maven 来看就行，下面我会详细解读其构建配置文件。

### 模块组成

项目由两个模块组成，一个是框架本身的模块，实现了框架的 IOC/AOP 等功能，如下图：  

![](https://upload-images.jianshu.io/upload_images/14923529-51a3dad295968b43.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

类比较多，但是大部分都是代码很少的，特别是注解定义接口，不要怕。  

- `aop` 包中是 `After` 等注解的定义接口，以及动态代理辅助类；  
- `bean` 包中是两个注解定义，以及 `BeanFactory` 这个 Bean 工厂，其中包含了类扫描和 Bean 的初始化的代码；
- `core` 包是一个 `ClassScanner` 类扫描工具类；
- `starter` 包是一个框架的启动与初始化类；
- `web/handler` 包中是 uri 请求的处理器的收集与管理，如查找 `@Controller` 注解修饰的类中的 `@RequestMapping` 注解修饰的方法，用来响应对应 uri 请求。
- `web/mvc` 包定义了与 webMVC 有关的三个注解；
- `web/server` 包中是一个嵌入式 Tomcat 服务器的初始化类；
- `web/servlet` 包中是一个请求分发器，重写的 `service()` 方法定义使用哪个请求处理器来响应浏览器请求；


另一个模块是用来测试（使用）框架的模块，如下图：

![](https://upload-images.jianshu.io/upload_images/14923529-e6afc2bee1e0b85b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

就像我们使用 Spring 框架一样，定义 Controller 等来响应请求，代码很简单，就不解释了。  

### 项目构建

根目录下有 `setting.gradle`、`build.gradle` 项目构建文件，其中 `setting.gradle` 指定了项目名以及模块名。  

```text
rootProject.name = 'mini-spring'
include 'framework'
include 'framework_use_test'
```

`build.gradle` 是项目构建设置，主要代码如下：

```
plugins {
    id 'java'
}

group 'com.caozhihu.spring'
version '1.0-SNAPSHOT'

sourceCompatibility = 1.8

repositories {
    repositories { maven { url 'http://maven.aliyun.com/nexus/content/groups/public/' } }
//    mavenCentral()
}

dependencies {
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```

引入了 gradle 的 java 插件，因为 gradle 不仅仅可以用于 java 项目，也可以用于其他项目，引入了 java 插件定义了项目的文件目录结构等。  

然后就是项目的版本以及 java 源代码适配级别，这里是 JDK 1.8，在后面是指定了依赖仓库，gradle 可以直接使用 maven 仓库。  

最后就是引入项目具体依赖，这里和 maven 一样。  

  
每个模块也有单独的 `build.gradle` 文件来指定模块的构建设置，这里以 `framework_use_test` 模块的 `build.gradle` 文件来说明：

```text
dependencies {
    // 只在单元测试时候引入此依赖
    testCompile group: 'junit', name: 'junit', version: '4.12'
    // 项目依赖
    compile(project(':framework'))
}

jar {
    manifest {
        attributes "Main-Class": "com.caozhihu.spring.Application"
    }
    // 固定打包句式
    from {
        configurations.runtime.asFileTree.files.collect { zipTree(it) }
    }
}
```

除去和项目根目录下构建文件相同部分，其他的构建代码如上，这里的 dependencies 除了添加 Junit 单元测试依赖之外，还指定了 `framework` 模块。  

下面指定了 jar 包的打包设置，首先使用 manifest 设置主类，否则生成的 jar 包找不到主类清单，会无法运行。还使用了 from 语句来设置打包范围，这是固定句式，用来收集所有的 java 类文件。  

# framework 实现流程

如下图：

![](https://upload-images.jianshu.io/upload_images/14923529-71c8218afa692f36.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 启动 tomcat 服务

```java
public void startServer() throws LifecycleException {
        tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.start();

        // new 一个标准的 context 容器并设置访问路径；
        // 同时为 context 设置生命周期监听器。
        Context context = new StandardContext();
        context.setPath("");
        context.addLifecycleListener(new Tomcat.FixContextListener());
        // 新建一个 DispatcherServlet 对象，这个是我们自己写的 Servlet 接口的实现类，
        // 然后使用 `Tomcat.addServlet()` 方法为 context 设置指定名字的 Servlet 对象，
        // 并设置为支持异步。
        DispatcherServlet servlet = new DispatcherServlet();
        Tomcat.addServlet(context, "dispatcherServlet", servlet)
                .setAsyncSupported(true);

        // Tomcat 所有的线程都是守护线程，
        // 如果某一时刻所有的线程都是守护线程，那 JVM 会退出，
        // 因此，需要为 tomcat 新建一个非守护线程来保持存活，
        // 避免服务到这就 shutdown 了
        context.addServletMappingDecoded("/", "dispatcherServlet");
        tomcat.getHost().addChild(context);

        Thread tomcatAwaitThread = new Thread("tomcat_await_thread") {
            @Override
            public void run() {
                TomcatServer.this.tomcat.getServer().await();
            }
        };

        tomcatAwaitThread.setDaemon(false);
        tomcatAwaitThread.start();
    }
```

这里看代码注释，结合下面这张 tomcat 架构图就可以理解了。  

![](https://upload-images.jianshu.io/upload_images/14923529-c0450cd489145b96.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

图片来自 http://click.aliyun.com/m/1000014411/  

如果暂时不理解也没关系，不影响框架学习，我只是为了玩一玩内嵌 tomcat，完全可以自己实现一个乞丐版的网络服务器的。    

这里使用的是我们自定义的 Servlet 子类 DispatcherServlet 对象，该类重写了 `service()` 方法，代码如下：

```java
@Override
    public void service(ServletRequest req, ServletResponse res) throws IOException {
        for (MappingHandler mappingHandler : HandlerManager.mappingHandlerList) {
            // 从所有的 MappingHandler 中逐一尝试处理请求，
            // 如果某个 handler 可以处理(返回true)，则返回即可
            try {
                if (mappingHandler.handle(req, res)) {
                    return;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        res.getWriter().println("failed!");
    }
```

HandlerManager 和 MappingHandler 处理器后面会讲，这里先不展开。至此，tomcat 服务器启动完成；

### 扫描类

扫描类是通过这句代码完成的：

```java
// 扫描类
List<Class<?>> classList = ClassScanner.scannerCLasses(cls.getPackage().getName());
```

`ClassScanner.scannerCLasses` 方法实现如下：

```java
public static List<Class<?>> scannerCLasses(String packageName)
            throws IOException, ClassNotFoundException {
        List<Class<?>> classList = new ArrayList<>();
        String path = packageName.replace(".", "/");
        // 线程上下文类加载器默认是应用类加载器，即 ClassLoader.getSystemClassLoader();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // 使用类加载器对象的 getResources(ResourceName) 方法获取资源集
        // Enumeration 是古老的迭代器版本，可当成 Iterator 使用
        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            // 获取协议类型，判断是否为 jar 包
            if (url.getProtocol().contains("jar")) {
                // 将打开的 url 返回的 URLConnection 转换成其子类 JarURLConnection 包连接
                JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                String jarFilePath = jarURLConnection.getJarFile().getName();
                // getClassesFromJar 工具类获取指定 Jar 包中指定资源名的类；
                classList.addAll(getClassesFromJar(jarFilePath, path));
            } else {
                // 简单起见，我们暂时仅实现扫描 jar 包中的类
                // todo
            }
        }
        return classList;
    }

    private static List<Class<?>> getClassesFromJar(String jarFilePath, String path) throws IOException, ClassNotFoundException {
         // 为减少篇幅，这里完整代码就不放出来了
    }
```

注释很详细，就不多废话了。

### 初始化Bean工厂

这部分是最重要的，IOC 和 AOP 都在这里实现。  

代码请到在 `BeanFactory` 类中查看，[GitHub 在线查看 BeanFactory](https://github.com/czwbig/mini-spring/blob/master/framework/src/main/java/com/caozhihu/spring/bean/BeanFactory.java)  

注释已经写的非常详细。这里简单说下处理逻辑。   

首先通过遍历上一步类扫描获得类的 Class 对象集合，将被 `@Aspect` 注解的类保存起来，然后初始化其他被 `@Component` 和 `@Controller` 注解的类，并处理类中被 `@AutoWired` 注解的属性，将目标引用对象注入（设置属性的值）到类中，然后将初始化好的对象保存到 Bean 工厂。到这里，控制反转就实现好了。  

接下来是处理被 `@Aspect` 注解的类，并解析他们中被 `@Pointcut`、`@Before` 和 `@After` 注解的方法，使用 JDK 动态代理生成代理对象，并更新 Bean 工厂。  

注意，在处理被 `@Aspect` 注解的类之前，Bean 工厂中的对象依赖已经设置过了就旧的 Bean，更新了 Bean 工厂中的对象后，需要通知依赖了被更新对象的对象重新初始化。  

例如对象 A 依赖对象 B，即 A 的类中有一句

```java
@AutoWired
B b;
```

同时，一个切面类中的切点 `@Pointcut` 的值指向了 B 类对象，然后他像 Bean 工厂更新了 B 对象，但这时 A 中引用的 B 对象，还是之前的旧 B 对象。  

这里我的解决方式是，将带有 `@AutoWired` 属性的类保存起来，处理好 AOP 关系之后，再次初始化这些类，这样他们就能从 Bean 工厂获得新的已经被代理过的对象了。    

至于如何使用 JDK 动态代理处理 AOP 关系的，请参考 [GitHub ProxyDyna 类](https://github.com/czwbig/mini-spring/blob/master/framework/src/main/java/com/caozhihu/spring/aop/ProxyDyna.java)
中代码，总的来说是，定义一个 `ProxyDyna` 类实现 `InvocationHandler` 接口，然后实现 `invoke()` 方法即可，在 `invoke()` 方法中处理代理增强逻辑。  

然后获取对象的时候，使用 `Proxy.newProxyInstance()` 方法而不是直接 new，如下：  

```java
Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(), this);
```  


### 初始化Handler

HandlerManager 类中调用 `parseHandlerFromController()` 方法来遍历处理所有的已扫描到的类，来初始化 MappingHandler 对象，方法代码如下：  

```java
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
```

MappingHandler 对象表示如何处理一次请求，包括请求 uri，应该调用的类，应该调用的方法以及方法参数。  

如此，在 MappingHandler 的 `handle()` 方法中处理请求，直接从 Bean 工厂获取指定类对象，从 response 对象中获取请求参数值，使用反射调用对应方法，并接收方法返回值输出给浏览器即可。  

再回顾我们启动 tomcat 服务器时指定运行的 servlet：  

```java
@Override
    public void service(ServletRequest req, ServletResponse res) throws IOException {
        for (MappingHandler mappingHandler : HandlerManager.mappingHandlerList) {
            // 从所有的 MappingHandler 中逐一尝试处理请求，
            // 如果某个 handler 可以处理(返回true)，则返回即可
            try {
                if (mappingHandler.handle(req, res)) {
                    return;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        res.getWriter().println("failed!");
    }
```

一目了然，其 `service()` 方法只是遍历所有的 MappingHandler 对象来处理请求而已。

# 框架使用

测试使用 IOC 和 AOP 功能。这里以定义一个 /rap 路径举例，

**1. 定义Controller**  


```java
@Controller
public class RapController {
    @AutoWired
    private Rap rapper;

    @RequestMapping("/rap")
    public String rap() {
        rapper.rap();
        return "CXK";
    }
}
```

RapController 从 Bean 工厂获取一个 Rap 对象，访问 /rap 路径是，会先执行该对象的 `rap()` 方法，然后返回 "CXK" 给浏览器。  

**2. 定义 Rap 接口及其实现类**

```java
public interface Rap {
    void rap();
}
// ----another file----
@Component
public class Rapper implements Rap {
    public void rap() {
        System.out.println("CXK rapping...");
    }
}
```

接口一定要定义，否则无法使用 AOP，因为我们使用的是 JDK 动态代理，只能代理实现了接口的类（原理是生成一个该接口的增强带向）。Spring 使用的是 JDK 动态代理和 CGLIB 两种方式，CGLIB 可以直接使用 ASM 等字节码生成框架，来生成一个被代理对象的增强子类。  

使用浏览器访问 `http://localhost:8080/rap` ，即可看到 IDE 控制台输出 `CXK rapping...`，可以看到，`@AutoWired` 注解成功注入了对象。  

但如果我们想在 rap 前面先 唱、跳，并且在 rap 后面打篮球，那么就需要定义织面类来面向切面编程。  

定义一个 `RapAspect` 类如下：

```java
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
        // 在 rap 之前要先唱、跳
        System.out.println("first,singing <chicken is too beautiful>.");
        System.out.println("and the chicken monster is dancing now.");
    }

    @After("rapPoint()")
    public void basketball() {
        // 在 rap 之后别忘记了篮球
        System.out.println("oh! Don't forget my favorite basketball.");
    }
}
```

织面类 RapAspect 定义了切入点以及前置后置通知等，这样 RapController 中使用 `@AutoWired` 注解引入的 Rap 对象，会被替换为增强的 Rap 代理对象，如此，我们无需改动 RapController 中任何一处代码，就实现了在 `rap()` 方法前后执行额外的代码（通知）。

增加 RapAspect 后，再次访问会在 IDE 控制台输出：  

```text
first,singing <chicken is too beautiful>.
and the chicken monster is dancing now.
CXK rapping...
oh! Don't forget my favorite basketball.
```  


# 总结与参考

没啥好说的了

### 参考
tomcat 使用与框架图：[手写一个简化版Tomcat](https://yq.aliyun.com/articles/630266?utm_content=m_1000014411)
gradle 配置与 DI 部分实现：[慕课网](https://s.imooc.com/AjZGHfE)
Spring 常用注解 [how2j SPRING系列教材](http://how2j.cn/k/spring/spring-annotation-ioc-di/1067.html?p=55563)
