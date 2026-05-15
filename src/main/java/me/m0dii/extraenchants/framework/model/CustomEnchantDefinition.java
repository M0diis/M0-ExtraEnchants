package me.m0dii.extraenchants.framework.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomEnchantDefinition {
    private final String id;
    private String displayName;
    private String description = "";
    private String rarity = "COMMON";
    private int maxLevel = 1;
    private boolean enabled = true;
    private boolean showInList = true;
    private int weight = 0;
    private String category = "GENERAL";
    private String icon = "ENCHANTED_BOOK";
    private final List<String> applicableItems = new ArrayList<>();
    private final List<String> conflictsWith = new ArrayList<>();
    private final Map<String, Object> metadata = new HashMap<>();
    private final Map<String, TriggerDefinition> triggers = new HashMap<>();

    public CustomEnchantDefinition(String id) {
        this.id = id;
        this.displayName = id;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isShowInList() {
        return showInList;
    }

    public void setShowInList(boolean showInList) {
        this.showInList = showInList;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<String> getApplicableItems() {
        return applicableItems;
    }

    public List<String> getConflictsWith() {
        return conflictsWith;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Map<String, TriggerDefinition> getTriggers() {
        return triggers;
    }
}

