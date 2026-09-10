package me.m0dii.extraenchants.framework.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class CustomEnchantDefinition {
    private final String id;
    @Setter
    private String displayName;
    @Setter
    private String description = "";
    @Setter
    private String rarity = "COMMON";
    @Setter
    private int maxLevel = 1;
    @Setter
    private boolean enabled = true;
    @Setter
    private boolean showInList = true;
    @Setter
    private int weight = 0;
    @Setter
    private String category = "GENERAL";
    @Setter
    private String icon = "ENCHANTED_BOOK";
    private final List<String> applicableItems = new ArrayList<>();
    private final List<String> conflictsWith = new ArrayList<>();
    private final Map<String, Object> metadata = new LinkedHashMap<>();
    private final Map<String, TriggerDefinition> triggers = new LinkedHashMap<>();

    public CustomEnchantDefinition(String id) {
        this.id = id;
        this.displayName = id;
    }

}

