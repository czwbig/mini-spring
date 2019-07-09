package com.caozhihu.spring.web.server;

import com.caozhihu.spring.web.servlet.DispatcherServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;

/**
 * @author:czwbig
 * @date:2019/7/6 15:08
 * @description: Tomcat服务器初始化，这里调用的 DispatcherServlet
 */
public class TomcatServer {
    private Tomcat tomcat;
    private String[] args;

    public TomcatServer(String[] args) {
        this.args = args;
    }

    public void startServer() throws LifecycleException {
        tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.start();

        Context context = new StandardContext();
        context.setPath("");
        context.addLifecycleListener(new Tomcat.FixContextListener());
        DispatcherServlet servlet = new DispatcherServlet();
        Tomcat.addServlet(context, "dispatcherServlet", servlet)
                .setAsyncSupported(true);
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
}
