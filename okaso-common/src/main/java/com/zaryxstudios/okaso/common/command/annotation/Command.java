package com.zaryxstudios.okaso.common.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String name();
    String[] aliases() default {};
    String permission() default "";
    String description() default "";
    String usage() default "";
    boolean playerOnly() default false;
    boolean consoleOnly() default false;
    int cooldown() default 0;
}
