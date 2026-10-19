package com.zaryxstudios.okaso.common.compat;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks code that references Bukkit API newer than the oldest supported version (1.8.8)
 * and is only reached behind a version check or a catch of
 * NoSuchMethodError / NoSuchFieldError / NoClassDefFoundError with a fallback.
 * The Animal Sniffer build check skips annotated members.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface CompatGuarded {

    /** First Minecraft version that provides the referenced API, e.g. "1.13". */
    String since();
}
