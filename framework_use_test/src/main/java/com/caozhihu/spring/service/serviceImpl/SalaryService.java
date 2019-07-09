package com.caozhihu.spring.service.serviceImpl;

import com.caozhihu.spring.bean.Component;

/**
 * @author:czwbig
 * @date:2019/7/6 23:59
 * @description:
 */

@Component
public class SalaryService {
    public Integer calSalary(Integer experience) {
        return experience * 666;
    }
}
