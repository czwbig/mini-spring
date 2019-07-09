package com.caozhihu.spring.web.servlet;

import com.caozhihu.spring.web.handler.HandlerManager;
import com.caozhihu.spring.web.handler.MappingHandler;

import javax.servlet.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author:czwbig
 * @date:2019/7/6 15:30
 * @description: 请求分发器，所有的请求都会在这里处理
 */
public class DispatcherServlet implements Servlet {

    @Override
    public void init(ServletConfig config) {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

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

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
