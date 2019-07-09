package com.caozhihu.spring.bean;

import java.lang.annotation.*;

/**
 * @author:czwbig
 * @date:2019/7/6 23:23
 * @description:
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoWired {
}
