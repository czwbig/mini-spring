package com.caozhihu.spring.controller;

import com.caozhihu.spring.bean.AutoWired;
import com.caozhihu.spring.service.serviceImpl.SalaryService;
import com.caozhihu.spring.web.mvc.Controller;
import com.caozhihu.spring.web.mvc.RequestMapping;
import com.caozhihu.spring.web.mvc.RequestParam;

@Controller
public class SalaryController {
    @AutoWired
    private SalaryService salaryService;

    @RequestMapping("/")
    public String index() {
        return "This is Index.这是首页";
    }

    @RequestMapping("/getSalary")
    public Integer getSalary(@RequestParam("name") String name, @RequestParam("experience") String experience) {
        if (experience == null || experience.isEmpty()) {
            return 0;
        }
        return salaryService.calSalary(Integer.parseInt(experience));
    }
}
