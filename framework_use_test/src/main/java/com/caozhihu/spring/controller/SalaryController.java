package com.caozhihu.spring.controller;

import com.caozhihu.spring.bean.AutoWired;
import com.caozhihu.spring.service.serviceImpl.SalaryService;
import com.caozhihu.spring.web.mvc.Controller;
import com.caozhihu.spring.web.mvc.RequestMapping;
import com.caozhihu.spring.web.mvc.RequestParam;

/**
 * @author:czwbig
 * @date:2019/7/6 16:48
 * @description:
 */
@Controller
public class SalaryController {
    @AutoWired
    private SalaryService salaryService;

    @RequestMapping("/getSalary")
    public Integer getSalary(@RequestParam("name") String name, @RequestParam("experience") String experience) {
        if (experience == null || experience.isEmpty()) {
            return 0;
        }
        return salaryService.calSalary(Integer.parseInt(experience));
    }
}
