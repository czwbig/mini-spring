package com.caozhihu.spring.controller;

import com.caozhihu.spring.bean.AutoWired;
import com.caozhihu.spring.service.Rap;
import com.caozhihu.spring.web.mvc.Controller;
import com.caozhihu.spring.web.mvc.RequestMapping;

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