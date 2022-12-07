package me.m0dii.extraenchants.enchants;

import io.papermc.paper.enchantments.EnchantmentRarity;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.utils.Enchantables;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum EEnchant {
    TELEPATHY,
    PLOW,
    SMELT,
    BEHEADING,
    BONDED,
    LAVA_WALKER,
    HASTE_MINER,
    VEIN_MINER,
    ANTI_THORNS,
    EXPERIENCE_MINER,
    LIFESTEAL,
    ASSASSIN,
    REPLANTER,
    BERSERK,
    DISPOSER,
    TUNNEL,
    WITHERING,
    BARBARIAN,
    ARMOR_BREAKER,
    COLD_STEEL,
    BAT_VISION,
    DEATH_SIPHON;

    private final ExtraEnchants instance = ExtraEnchants.getInstance();

    private Enchantment enchant;


    public String getDisplayName() {
        if(!this.name().contains("_")) {
            return StringUtils.capitalize(this.name().toLowerCase());
        }

        String[] split = this.name().split("_");

        return Arrays.stream(split)
                .map(s -> StringUtils.capitalize(s.toLowerCase()))
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }

    public String getConfigName() {
        return name().toLowerCase().replace("_", "");
    }

    public Enchantment getEnchantment() {
        return this.enchant;
    }

    public void setEnchantment(Enchantment enchant) {
        this.enchant = enchant;
    }

    public int getTriggerChance() {
        return instance.getCfg().getInt("enchants." + getConfigName() + ".trigger-chance", -1);
    }

    public boolean isDisabled() {
        Messenger.debug("Is disabled, path: 'enchants." + getConfigName() + ".enabled'");
        return !instance.getCfg().getBoolean("enchants." + getConfigName() + ".enabled", true);
    }

    public int getEnchantChance() {
        return instance.getCfg().getInt("enchants." + getConfigName() + ".table-chance", -1);
    }

    public boolean isCursed() {
        return instance.getCfg().getBoolean("enchants." + getConfigName() + ".cursed", false);
    }

    public boolean isTreasure() {
        return instance.getCfg().getBoolean("enchants." + getConfigName() + ".treasure", false);
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.valueOf(instance.getCfg().getString("enchants." + getConfigName() + ".rarity", "COMMON").toUpperCase());
    }

    public String getDisplayInLore(int level, boolean formatted) {
        String name = instance.getCfg().getString("enchants." + getConfigName() + ".enchanted-item-lore-name", getDisplayName())
                .replace("%level%", Utils.arabicToRoman(level));

        return formatted ? ChatColor.translateAlternateColorCodes('&', name) : name;
    }


    public boolean conflictsWith(Enchantment enchant) {
        return this.enchant.conflictsWith(enchant);
    }

    public boolean defaultConflictsEnabled() {
        return instance.getCfg().getBoolean("enchants." + getConfigName() + ".default-conflicts");
    }

    public boolean canEnchantItem(ItemStack item) {
        if (!getEnchantableTypes().isEmpty()) {
        for(Enchantables.ItemType type : getEnchantableTypes()) {
            switch (type) {
                case ALL:
                    return true;
                case ARMOR:
                    if(Enchantables.isArmor(item)) {
                        return true;
                    }
                    break;
                case SWORD:
                    if(Enchantables.isSword(item)) {
                        return true;
                    }
                    break;
                case AXE:
                    if(Enchantables.isAxe(item)) {
                        return true;
                    }
                    break;
                case PICKAXE:
                    if(Enchantables.isPickaxe(item)) {
                        return true;
                    }
                    break;
                case SHOVEL:
                    if(Enchantables.isShovel(item)) {
                        return true;
                    }
                    break;
                case HOE:
                    if(Enchantables.isHoe(item)) {
                        return true;
                    }
                    break;
                case BOW:
                    if(Enchantables.isBow(item)) {
                        return true;
                    }
                    break;
                case FISHING_ROD:
                    if(Enchantables.isFishingRod(item)) {
                        return true;
                    }
                    break;
                case TRIDENT:
                    if(Enchantables.isTrident(item)) {
                        return true;
                    }
                    break;
                case CROSSBOW:
                    if(Enchantables.isCrossbow(item)) {
                        return true;
                    }
                    break;
                case SHEARS:
                    if(Enchantables.isShears(item)) {
                        return true;
                    }
                    break;
                case SHIELD:
                    if(Enchantables.isShield(item)) {
                        return true;
                    }
                    break;
                case ELYTRA:
                    if(Enchantables.isElytra(item)) {
                        return true;
                    }
                    break;
                case TOOL:
                    if(Enchantables.isTool(item)) {
                        return true;
                    }
                    break;
                case WEAPON:
                    if(Enchantables.isWeapon(item)) {
                        return true;
                    }
                    break;
                case HELMET:
                    if(Enchantables.isHelmet(item)) {
                        return true;
                    }
                    break;
                case CHESTPLATE:
                    if(Enchantables.isChestplate(item)) {
                        return true;
                    }
                    break;
                case LEGGINGS:
                    if(Enchantables.isLeggings(item)) {
                        return true;
                    }
                    break;
                case BOOTS:
                    if(Enchantables.isBoots(item)) {
                        return true;
                    }
                    break;
                }
            }
        }

        return this.enchant.canEnchantItem(item);
    }

    public boolean equals(Enchantment other) {
        return this.enchant.equals(other);
    }

    public void enchant(ItemStack item) {
        enchant(item, 1);
    }

    public void enchant(ItemStack item, int level) {
        item.addUnsafeEnchantment(enchant, level);
    }

    public List<Enchantables.ItemType> getEnchantableTypes() {
        return instance.getCfg().getStringList("enchants." + getConfigName() + ".enchantable-items")
                .stream()
                .map(Enchantables.ItemType::parse)
                .collect(Collectors.toList());
    }

    public List<Enchantment> getCustomConflicts() {
        return instance.getCfg().getStringList("enchants." + getConfigName() + ".conflicts")
                .stream()
                .map(s -> {
                    Enchantment byName = Enchantment.getByName(s.toUpperCase());

                    if(byName != null) {
                        return byName;
                    }

                    EEnchant parsed = EEnchant.parse(s);

                    if(parsed == null || parsed.getEnchantment() == null) {
                        return null;
                    }

                    return parsed.getEnchantment();
                })
                .collect(Collectors.toList());
    }

    public static EEnchant parse(String value) {
        return Arrays.stream(EEnchant.values())
                .filter(v -> v.getDisplayName().equalsIgnoreCase(value.trim())
                        || v.getDisplayName().replace(" ", "").equalsIgnoreCase(value.trim())
                        || v.getDisplayName().replace(" ", "_").equalsIgnoreCase(value.trim())
                        || v.getConfigName().equalsIgnoreCase(value.trim())
                )
                .findFirst()
                .orElse(null);
    }

    public static Enchantment toEnchant(String value) {
        return Arrays.stream(EEnchant.values())
                .filter(v -> v.getDisplayName().equalsIgnoreCase(value.trim())
                        || v.getDisplayName().replace(" ", "").equalsIgnoreCase(value.trim())
                        || v.getDisplayName().replace(" ", "_").equalsIgnoreCase(value.trim())
                        || v.getConfigName().equalsIgnoreCase(value.trim())
                )
                .map(EEnchant::getEnchantment)
                .findFirst()
                .orElse(null);
    }

    public static EEnchant fromEnchant(Enchantment enchant) {
        return Arrays.stream(EEnchant.values())
                .filter(v -> v.getEnchantment().equals(enchant))
                .findFirst()
                .orElse(null);
    }
}
