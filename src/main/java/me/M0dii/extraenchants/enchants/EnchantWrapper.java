package me.m0dii.extraenchants.enchants;

import org.intellij.lang.annotations.Subst;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EnchantWrapper {
    @Subst("") String name();

    int maxLevel();
}
