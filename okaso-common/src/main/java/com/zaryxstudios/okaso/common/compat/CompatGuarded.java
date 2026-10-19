package com.zaryxstudios.okaso.common.compat;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks code that references Bukkit API newer than the oldest supported version (1.8.8)
 * and is only reached behind a version check or a catch of LinkageError
 * (NoSuchMethodError / NoSuchFieldError / NoClassDefFoundError) with a fallback,
 * or a field of a newer type that stays null on older servers.
 * The Animal Sniffer build check skips annotated members.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
public @interface CompatGuarded {

    /** First Minecraft version that provides the referenced API, e.g. "1.13". */
    String since();
}
