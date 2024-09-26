package com.blog.sun.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})  // 可以注解在方法和类上
@Retention(RetentionPolicy.RUNTIME)
public @interface HandleFrontMvcException {
}
