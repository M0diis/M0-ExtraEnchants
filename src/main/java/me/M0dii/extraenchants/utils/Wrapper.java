package me.m0dii.extraenchants.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Wrapper {
    String name();
    int maxLvl();
}
