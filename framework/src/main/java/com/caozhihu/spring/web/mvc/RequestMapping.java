package com.caozhihu.spring.web.mvc;

import java.lang.annotation.*;

/**
 * @author:czwbig
 * @date:2019/7/6 16:00
 * @description:
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestMapping {
    // 要映射的URL
    String value();
}
