package me.m0dii.extraenchants.framework.runtime;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.framework.config.ConditionParser;
import me.m0dii.extraenchants.framework.config.ConfigValidationIssue;
import me.m0dii.extraenchants.framework.config.CustomEnchantConfigLoader;
import me.m0dii.extraenchants.framework.config.CustomEnchantLoadResult;
import me.m0dii.extraenchants.framework.hooks.NoopRegionHook;
import me.m0dii.extraenchants.framework.hooks.RegionHook;
import me.m0dii.extraenchants.framework.hooks.ResidenceRegionHook;
import me.m0dii.extraenchants.framework.model.CustomEnchantDefinition;
import me.m0dii.extraenchants.framework.model.ItemApplicability;
import me.m0dii.extraenchants.framework.model.TriggerDefinition;
import me.m0dii.extraenchants.framework.registry.ConditionRegistry;
import me.m0dii.extraenchants.framework.registry.EffectRegistry;
import me.m0dii.extraenchants.framework.registry.PlaceholderRegistry;
import me.m0dii.extraenchants.framework.registry.TargetRegistry;
import me.m0dii.extraenchants.framework.registry.TriggerRegistry;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Public facade for custom enchant persistence and execution.
 *
 * Trigger dispatch, targeting, conditions, effects, scheduling and mutable
 * runtime state live in dedicated services. This class owns lifecycle and the
 * item/configuration boundary only.
 */
public final class CustomEnchantFramework {
    private final ExtraEnchants plugin;
    private final Exp4jFormulaEngine formulaEngine = new Exp4jFormulaEngine();
    private final EffectRegistry effectRegistry = new EffectRegistry();
    private final TargetRegistry targetRegistry = new TargetRegistry();
    private final ConditionRegistry conditionRegistry = new ConditionRegistry();
    private final PlaceholderRegistry placeholderRegistry = new PlaceholderRegistry();
    private final TriggerRegistry triggerRegistry = new TriggerRegistry();
    private final CooldownService cooldownService = new CooldownService();
    private final ActivationService activationService = new ActivationService();
    private final EconomyService economyService;
    private final StatusStackService statusStackService = new StatusStackService();
    private final ComboStateService comboStateService = new ComboStateService();
    private final RegionHook regionHook;
    private final CustomEnchantItemStore itemStore;
    private final TriggerTaskScheduler taskScheduler;
    private final ConditionService conditionService;
    private final TargetingService targetingService;
    private final EffectService effectService;
    private final TriggerDispatcher triggerDispatcher;
    private final File enchantDirectory;

    private volatile Map<String, CustomEnchantDefinition> definitions = Map.of();
    private volatile CustomEnchantLoadResult lastLoadResult = new CustomEnchantLoadResult(
            Map.of(), List.of(), 0, 0, false);

    public CustomEnchantFramework(ExtraEnchants plugin) {
        this.plugin = plugin;
        this.economyService = new EconomyService(plugin);
        this.itemStore = new CustomEnchantItemStore(plugin);
        this.regionHook = resolveRegionHook();
        this.enchantDirectory = new File(plugin.getDataFolder(), "custom-enchants");
        this.taskScheduler = new TriggerTaskScheduler(plugin);

        registerDefaultPlaceholders();
        this.conditionService = new ConditionService(
                conditionRegistry, formulaEngine, regionHook, economyService, statusStackService, comboStateService);
        this.targetingService = new TargetingService(targetRegistry, formulaEngine);
        this.effectService = new EffectService(
                plugin, formulaEngine, economyService, statusStackService, comboStateService,
                effectRegistry, placeholderRegistry, this::debug);
        this.triggerDispatcher = new TriggerDispatcher(
                plugin, formulaEngine, conditionService, targetingService, effectService,
                cooldownService, activationService, comboStateService, statusStackService,
                economyService, regionHook, taskScheduler, this::debug);
    }

    /** Validates and atomically commits the complete custom-enchant directory. */
    public synchronized void reload() {
        if (!isConfigDrivenEnabled()) {
            definitions = Map.of();
            triggerRegistry.rebuild(List.of());
            clearRuntimeState();
            lastLoadResult = new CustomEnchantLoadResult(Map.of(), List.of(), 0, 0, true);
            plugin.getLogger().info("Config-driven enchants are disabled (config-driven-enchants=false)");
            return;
        }

        saveDefaultExamples();
        CustomEnchantLoadResult result = loadConfiguration();
        lastLoadResult = result;
        if (!result.isCommitted()) {
            plugin.getLogger().severe("Custom-enchant reload rejected; keeping the previous configuration. "
                    + result.getErrorCount() + " error(s)");
            logValidationIssues(result);
            return;
        }

        definitions = Collections.unmodifiableMap(new LinkedHashMap<>(result.getDefinitions()));
        triggerRegistry.rebuild(definitions.values());
        clearRuntimeState();
        plugin.getLogger().info("Loaded " + definitions.size()
                + " custom enchants from custom-enchants/*.yml");
        logValidationIssues(result);
    }

    /** Runs validation against disk without changing the active runtime registry. */
    public synchronized CustomEnchantLoadResult validateConfiguration() {
        if (!isConfigDrivenEnabled()) {
            CustomEnchantLoadResult result = new CustomEnchantLoadResult(Map.of(), List.of(), 0, 0, true);
            lastLoadResult = result;
            return result;
        }

        CustomEnchantLoadResult result = loadConfiguration();
        lastLoadResult = result;
        return result;
    }

    public CustomEnchantLoadResult getLastLoadResult() {
        return lastLoadResult;
    }

    public Map<String, CustomEnchantDefinition> getDefinitions() {
        return definitions;
    }

    public CustomEnchantDefinition getDefinition(String id) {
        if (!isConfigDrivenEnabled() || id == null) {
            return null;
        }
        return definitions.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean isApplicable(ItemStack item, String enchantId) {
        CustomEnchantDefinition definition = getDefinition(enchantId);
        return definition != null && ItemApplicability.matches(item, definition.getApplicableItems());
    }

    public void applyEnchant(ItemStack item, String enchantId, int level) {
        if (!isConfigDrivenEnabled() || item == null || item.getType().isAir()) {
            return;
        }

        CustomEnchantDefinition definition = getDefinition(enchantId);
        if (definition == null || !ItemApplicability.matches(item, definition.getApplicableItems())) {
            return;
        }

        int clamped = Math.clamp(level, 1, definition.getMaxLevel());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        NamespacedKey key = NamespacedKey.fromString(CustomEnchantItemStore.namespaced(definition.getId()));
        if (key != null) {
            Enchantment enchantment = Enchantment.getByKey(key);
            if (enchantment != null) {
                meta.addEnchant(enchantment, clamped, true);
            }
        }

        String loreLine = renderLoreLine(definition, clamped);
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());
        String plainName = org.bukkit.ChatColor.stripColor(Utils.format(definition.getDisplayName()));
        lore.removeIf(line -> {
            String plainLine = org.bukkit.ChatColor.stripColor(line);
            return plainLine != null && plainName != null && plainLine.contains(plainName);
        });
        lore.add(loreLine);
        meta.setLore(lore);
        item.setItemMeta(meta);
        itemStore.setLevel(item, definition.getId(), clamped);
    }

    public ItemStack createBook(String enchantId, int level) {
        if (!isConfigDrivenEnabled()) {
            return null;
        }

        CustomEnchantDefinition definition = getDefinition(enchantId);
        if (definition == null) {
            return null;
        }

        int clamped = Math.clamp(level, 1, definition.getMaxLevel());
        ItemStack book = new ItemStack(org.bukkit.Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        NamespacedKey key = NamespacedKey.fromString(CustomEnchantItemStore.namespaced(definition.getId()));
        Enchantment enchantment = key == null ? null : Enchantment.getByKey(key);

        if (meta instanceof EnchantmentStorageMeta storageMeta && enchantment != null) {
            storageMeta.addStoredEnchant(enchantment, clamped, true);
            storageMeta.displayName(Utils.colorize(definition.getDisplayName() + " "
                    + Utils.arabicToRoman(clamped)));
            storageMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            book.setItemMeta(storageMeta);
        } else if (meta != null) {
            if (enchantment != null) {
                meta.addEnchant(enchantment, clamped, true);
            }
            meta.displayName(Utils.colorize(definition.getDisplayName() + " "
                    + Utils.arabicToRoman(clamped)));
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            book.setItemMeta(meta);
        }

        itemStore.setLevel(book, definition.getId(), clamped);
        return book;
    }

    public int getConfigEnchantLevel(ItemStack item, String enchantId) {
        if (!isConfigDrivenEnabled() || item == null || enchantId == null) {
            return 0;
        }
        return itemStore.getLevel(item, enchantId);
    }

    /** Removes one config enchant while preserving every other enchant and PDC entry. */
    public boolean removeConfigEnchant(ItemStack item, String enchantId) {
        if (!isConfigDrivenEnabled() || item == null || enchantId == null) {
            return false;
        }

        String id = enchantId.toLowerCase(Locale.ROOT);
        if (itemStore.getLevel(item, id) <= 0) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        NamespacedKey key = NamespacedKey.fromString(CustomEnchantItemStore.namespaced(id));
        if (key != null) {
            Enchantment enchantment = Enchantment.getByKey(key);
            if (enchantment != null) {
                meta.removeEnchant(enchantment);
            }
        }

        CustomEnchantDefinition definition = getDefinition(id);
        if (definition != null && meta.getLore() != null) {
            String plainBase = org.bukkit.ChatColor.stripColor(Utils.format(definition.getDisplayName()));
            List<String> lore = new ArrayList<>(meta.getLore());
            lore.removeIf(line -> {
                String plain = org.bukkit.ChatColor.stripColor(line);
                return plain != null && plainBase != null && plain.contains(plainBase);
            });
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        itemStore.removeLevel(item, id);
        return true;
    }

    public List<String> removeConfigEnchants(ItemStack item) {
        if (!isConfigDrivenEnabled() || item == null || item.getType().isAir()) {
            return List.of();
        }

        Map<String, Integer> existing = itemStore.getEnchantMap(item);
        if (existing.isEmpty()) {
            return List.of();
        }

        List<String> removed = new ArrayList<>();
        for (String namespaced : new ArrayList<>(existing.keySet())) {
            String id = namespaced.startsWith("custom:")
                    ? namespaced.substring("custom:".length()) : namespaced;
            if (removeConfigEnchant(item, id)) {
                removed.add(id);
            }
        }
        return removed;
    }

    public void execute(
            TriggerType triggerType,
            Event event,
            Player owner,
            LivingEntity attacker,
            LivingEntity victim,
            org.bukkit.Location location,
            int level,
            String enchantId
    ) {
        if (!isConfigDrivenEnabled() || triggerType == null) {
            return;
        }

        CustomEnchantDefinition definition = getDefinition(enchantId);
        if (definition == null || !definition.isEnabled()) {
            debug("Skip trigger " + triggerType + " for enchant=" + enchantId + " (missing or disabled)");
            return;
        }

        TriggerDefinition trigger = findTrigger(definition, toConfigTrigger(triggerType));
        if (trigger == null) {
            debug("Skip trigger " + triggerType + " for enchant=" + enchantId + " (no trigger section)");
            return;
        }

        triggerDispatcher.dispatch(new ExecutionContext(
                plugin, definition, trigger, triggerType, event, attacker, victim, owner, level, location));
    }

    public void executeForItem(
            TriggerType triggerType,
            Event event,
            Player owner,
            LivingEntity attacker,
            LivingEntity victim,
            org.bukkit.Location location,
            ItemStack item
    ) {
        if (!isConfigDrivenEnabled() || owner == null || item == null || item.getType().isAir()) {
            return;
        }

        for (Map.Entry<String, Integer> entry : itemStore.getEnchantMap(item).entrySet()) {
            String id = entry.getKey().startsWith("custom:")
                    ? entry.getKey().substring("custom:".length()) : entry.getKey();
            if (isApplicable(item, id)) {
                execute(triggerType, event, owner, attacker, victim, location, entry.getValue(), id);
            }
        }
    }

    public void executeForArmor(
            TriggerType triggerType,
            Event event,
            Player owner,
            LivingEntity attacker,
            LivingEntity victim,
            org.bukkit.Location location
    ) {
        if (!isConfigDrivenEnabled() || owner == null) {
            return;
        }

        for (ItemStack armor : owner.getInventory().getArmorContents()) {
            executeForItem(triggerType, event, owner, attacker, victim, location, armor);
        }
    }

    public void clearPlayer(UUID playerId) {
        cooldownService.clearPlayer(playerId);
        activationService.clearPlayer(playerId);
        statusStackService.clearPlayer(playerId);
        comboStateService.clearPlayer(playerId);
    }

    public void shutdown() {
        taskScheduler.cancelAll();
        clearRuntimeState();
    }

    private CustomEnchantLoadResult loadConfiguration() {
        ConditionParser conditionParser = new ConditionParser(conditionRegistry, formulaEngine);
        CustomEnchantConfigLoader loader = new CustomEnchantConfigLoader(plugin, conditionParser);
        return loader.loadResult(enchantDirectory);
    }

    private void clearRuntimeState() {
        taskScheduler.cancelAll();
        cooldownService.clear();
        activationService.clear();
        statusStackService.clear();
        comboStateService.clear();
    }

    private TriggerDefinition findTrigger(CustomEnchantDefinition definition, String name) {
        if (name == null) {
            return null;
        }
        TriggerDefinition direct = definition.getTriggers().get(name);
        if (direct != null) {
            return direct;
        }
        return definition.getTriggers().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private void registerDefaultPlaceholders() {
        placeholderRegistry.register("player", context -> context.getOwner() == null ? "" : context.getOwner().getName());
        placeholderRegistry.register("attacker", context -> context.getAttacker() == null ? "" : context.getAttacker().getName());
        placeholderRegistry.register("victim", context -> context.getVictim() == null ? "" : context.getVictim().getName());
        placeholderRegistry.register("world", context -> context.getLocation() == null
                || context.getLocation().getWorld() == null ? "" : context.getLocation().getWorld().getName());
        placeholderRegistry.register("x", context -> context.getLocation() == null
                ? "0" : String.valueOf(context.getLocation().getBlockX()));
        placeholderRegistry.register("y", context -> context.getLocation() == null
                ? "0" : String.valueOf(context.getLocation().getBlockY()));
        placeholderRegistry.register("z", context -> context.getLocation() == null
                ? "0" : String.valueOf(context.getLocation().getBlockZ()));
        placeholderRegistry.register("level", context -> String.valueOf(context.getLevel()));
    }

    private RegionHook resolveRegionHook() {
        return Bukkit.getPluginManager().isPluginEnabled("Residence")
                ? new ResidenceRegionHook() : new NoopRegionHook();
    }

    private void saveDefaultExamples() {
        if (!enchantDirectory.exists()) {
            enchantDirectory.mkdirs();
        }

        List.of(
                "venom", "leeching", "freeze", "soulrend", "thunderclap", "executioner", "hex",
                "bloodrush", "momentum", "windstep", "foothold", "adrenaline", "warding", "retaliate",
                "bulwark", "overcharge", "quarry", "prospect", "overgrowth", "anglerluck", "battletrance",
                "skirmisher", "duskcloak"
        ).forEach(id -> copyDefaultIfMissing("custom-enchants/" + id + ".yml"));
    }

    private void copyDefaultIfMissing(String resourcePath) {
        File output = new File(plugin.getDataFolder(), resourcePath);
        if (!output.exists()) {
            plugin.saveResource(resourcePath, false);
        }
    }

    private String renderLoreLine(CustomEnchantDefinition definition, int level) {
        String baseName = definition.getDisplayName() == null || definition.getDisplayName().isBlank()
                ? definition.getId() : definition.getDisplayName();
        String format = plugin.getCfg().getString("custom-enchants.lore-format",
                "&7%enchant_name% %level_roman%");
        return Utils.format(format
                .replace("%enchant_name%", Utils.format(baseName))
                .replace("%level_roman%", Utils.arabicToRoman(level))
                .replace("%level%", String.valueOf(level)));
    }

    private String toConfigTrigger(TriggerType type) {
        String[] parts = type.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
            }
        }
        return result.toString();
    }

    private void logValidationIssues(CustomEnchantLoadResult result) {
        for (ConfigValidationIssue issue : result.getIssues()) {
            plugin.getLogger().log(issue.isError() ? Level.SEVERE : Level.WARNING, issue.format());
        }
    }

    private void debug(String message) {
        plugin.debug("[CustomEnchantFramework] " + message);
    }

    private boolean isConfigDrivenEnabled() {
        return plugin.getCfg().getBoolean("config-driven-enchants", true);
    }
}
