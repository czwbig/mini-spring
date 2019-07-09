package com.caozhihu.spring.service.serviceImpl;

import com.caozhihu.spring.bean.Component;
import com.caozhihu.spring.service.Rap;

@Component
public class Rapper implements Rap {
    public void rap() {
        System.out.println("CXK rapping...");
    }
}
