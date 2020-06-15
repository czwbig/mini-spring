package com.caozhihu.spring.service.serviceImpl;

import com.caozhihu.spring.bean.Component;

@Component
public class SalaryService {
    public Integer calSalary(Integer experience) {
        return experience * 666;
    }
}
