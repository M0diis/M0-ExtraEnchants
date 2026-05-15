package me.m0dii.extraenchants.framework.runtime;

import me.clip.placeholderapi.PlaceholderAPI;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.framework.api.Condition;
import me.m0dii.extraenchants.framework.api.EffectExecutor;
import me.m0dii.extraenchants.framework.api.TargetSelector;
import me.m0dii.extraenchants.framework.config.ConditionParser;
import me.m0dii.extraenchants.framework.config.CustomEnchantConfigLoader;
import me.m0dii.extraenchants.framework.hooks.NoopRegionHook;
import me.m0dii.extraenchants.framework.hooks.RegionHook;
import me.m0dii.extraenchants.framework.hooks.ResidenceRegionHook;
import me.m0dii.extraenchants.framework.model.CustomEnchantDefinition;
import me.m0dii.extraenchants.framework.model.EffectDefinition;
import me.m0dii.extraenchants.framework.model.TriggerDefinition;
import me.m0dii.extraenchants.framework.registry.*;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Animals;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class CustomEnchantFramework {
    private final ExtraEnchants plugin;
    private final Exp4jFormulaEngine formulaEngine = new Exp4jFormulaEngine();
    private final EffectRegistry effectRegistry = new EffectRegistry();
    private final TargetRegistry targetRegistry = new TargetRegistry();
    private final ConditionRegistry conditionRegistry = new ConditionRegistry();
    private final PlaceholderRegistry placeholderRegistry = new PlaceholderRegistry();
    private final TriggerRegistry triggerRegistry = new TriggerRegistry();
    private final CooldownService cooldownService = new CooldownService();
    private final EconomyService economyService;
    private final StatusStackService statusStackService = new StatusStackService();
    private final ComboStateService comboStateService = new ComboStateService();
    private final RegionHook regionHook;
    private final CustomEnchantItemStore itemStore;
    private final Map<String, Integer> activationCounts = new HashMap<>();

    private final File enchantDirectory;
    private Map<String, CustomEnchantDefinition> definitions = new HashMap<>();

    public CustomEnchantFramework(ExtraEnchants plugin) {
        this.plugin = plugin;
        this.economyService = new EconomyService(plugin);
        this.itemStore = new CustomEnchantItemStore(plugin);
        this.regionHook = resolveRegionHook();
        this.enchantDirectory = new File(plugin.getDataFolder(), "custom-enchants");
        registerBuiltIns();
    }

    public void reload() {
        if (!isConfigDrivenEnabled()) {
            this.definitions = new HashMap<>();
            this.triggerRegistry.rebuild(definitions.values());
            this.cooldownService.clear();
            this.activationCounts.clear();
            plugin.getLogger().info("Config-driven enchants are disabled (config-driven-enchants=false)");
            return;
        }

        saveDefaultExamples();
        ConditionParser conditionParser = new ConditionParser(conditionRegistry, formulaEngine);
        CustomEnchantConfigLoader loader = new CustomEnchantConfigLoader(plugin, conditionParser);
        this.definitions = loader.load(enchantDirectory);
        this.triggerRegistry.rebuild(definitions.values());
        this.cooldownService.clear();
        this.activationCounts.clear();
        plugin.getLogger().info("Loaded " + definitions.size() + " custom enchants from custom-enchants/*.yml");
    }

    public Map<String, CustomEnchantDefinition> getDefinitions() {
        return definitions;
    }

    public CustomEnchantDefinition getDefinition(String id) {
        if (!isConfigDrivenEnabled()) {
            return null;
        }

        return definitions.get(id.toLowerCase());
    }

    public void applyEnchant(ItemStack item, String enchantId, int level) {
        if (!isConfigDrivenEnabled()) {
            return;
        }

        if (item == null || item.getType().isAir()) {
            return;
        }

        CustomEnchantDefinition definition = getDefinition(enchantId);
        if (definition == null) {
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
            storageMeta.displayName(Utils.colorize(definition.getDisplayName() + " " + Utils.arabicToRoman(clamped)));
            storageMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            book.setItemMeta(storageMeta);
        } else if (meta != null) {
            if (enchantment != null) {
                meta.addEnchant(enchantment, clamped, true);
            }
            meta.displayName(Utils.colorize(definition.getDisplayName() + " " + Utils.arabicToRoman(clamped)));
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            book.setItemMeta(meta);
        }

        itemStore.setLevel(book, definition.getId(), clamped);
        return book;
    }

    public List<String> removeConfigEnchants(ItemStack item) {
        if (!isConfigDrivenEnabled()) {
            return List.of();
        }

        if (item == null || item.getType().isAir()) {
            return List.of();
        }

        Map<String, Integer> existing = itemStore.getEnchantMap(item);
        if (existing.isEmpty()) {
            return List.of();
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return List.of();
        }

        List<String> removed = new ArrayList<>();
        for (String namespaced : new ArrayList<>(existing.keySet())) {
            String id = namespaced.replace("custom:", "");
            CustomEnchantDefinition definition = getDefinition(id);
            NamespacedKey key = NamespacedKey.fromString(namespaced);
            if (key != null) {
                Enchantment enchantment = Enchantment.getByKey(key);
                if (enchantment != null) {
                    meta.removeEnchant(enchantment);
                }
            }

            if (definition != null && meta.getLore() != null) {
                String plainBase = org.bukkit.ChatColor.stripColor(Utils.format(definition.getDisplayName()));
                List<String> lore = new ArrayList<>(meta.getLore());
                lore.removeIf(line -> {
                    String plain = org.bukkit.ChatColor.stripColor(line);
                    return plain != null && plainBase != null && plain.contains(plainBase);
                });
                meta.setLore(lore);
            }

            itemStore.removeLevel(item, id);
            removed.add(id);
        }

        item.setItemMeta(meta);
        return removed;
    }

    public void execute(
            TriggerType triggerType,
            Event event,
            Player owner,
            LivingEntity attacker,
            LivingEntity victim,
            Location location,
            int level,
            String enchantId
    ) {
        if (!isConfigDrivenEnabled()) {
            return;
        }

        CustomEnchantDefinition single = getDefinition(enchantId);
        if (single == null || !single.isEnabled()) {
            debug("Skip trigger " + triggerType + " for enchant=" + enchantId + " (missing or disabled)");
            return;
        }

        TriggerDefinition trigger = single.getTriggers().get(toConfigTrigger(triggerType));
        if (trigger == null) {
            debug("Skip trigger " + triggerType + " for enchant=" + enchantId + " (no trigger section)");
            return;
        }

        debug("Run trigger " + triggerType + " for enchant=" + enchantId + " level=" + level);
        runTrigger(single, trigger, triggerType, event, owner, attacker, victim, location, level);
    }

    public void executeForItem(
            TriggerType triggerType,
            Event event,
            Player owner,
            LivingEntity attacker,
            LivingEntity victim,
            Location location,
            ItemStack item
    ) {
        if (!isConfigDrivenEnabled()) {
            return;
        }

        if (item == null || owner == null) {
            return;
        }

        Map<String, Integer> map = itemStore.getEnchantMap(item);
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String id = entry.getKey().replace("custom:", "");
            execute(triggerType, event, owner, attacker, victim, location, entry.getValue(), id);
        }
    }

    public void executeForArmor(
            TriggerType triggerType,
            Event event,
            Player owner,
            LivingEntity attacker,
            LivingEntity victim,
            Location location
    ) {
        if (!isConfigDrivenEnabled()) {
            return;
        }

        if (owner == null) {
            return;
        }

        for (ItemStack armor : owner.getInventory().getArmorContents()) {
            executeForItem(triggerType, event, owner, attacker, victim, location, armor);
        }
    }

    private void runTrigger(
            CustomEnchantDefinition enchant,
            TriggerDefinition trigger,
            TriggerType triggerType,
            Event event,
            Player owner,
            LivingEntity attacker,
            LivingEntity victim,
            Location location,
            int level
    ) {
        ExecutionContext context = new ExecutionContext(plugin, enchant, trigger, triggerType, event, attacker, victim, owner, level, location);
        String comboKey = comboKey(context);
        if (triggerType == TriggerType.ON_ATTACK) {
            long comboWindow = Math.max(250L, formulaEngine.evaluateInt("40", context.getVariables()) * 50L);
            int combo = comboStateService.registerHit(comboKey, comboWindow);
            context.getVariables().put("combo", (double) combo);
        }

        fillVariables(context);

        double chance = formulaEngine.evaluate(trigger.getChance(), context.getVariables());
        if (ThreadLocalRandom.current().nextDouble(0D, 100D) > chance) {
            debug("Chance check failed for " + enchant.getId() + " " + triggerType + " chance=" + chance);
            return;
        }

        String cooldownKey = enchant.getId() + ":" + triggerType.name();
        long playerCooldown = Math.max(0, formulaEngine.evaluateInt(trigger.getCooldown(), context.getVariables())) * 1000L;
        long globalCooldown = Math.max(0, formulaEngine.evaluateInt(trigger.getGlobalCooldown(), context.getVariables())) * 1000L;

        if (cooldownService.isOnPlayerCooldown(context.getOwnerId(), cooldownKey) || cooldownService.isOnGlobalCooldown(cooldownKey)) {
            debug("Cooldown active for " + enchant.getId() + " key=" + cooldownKey);
            return;
        }

        if (!conditionsPass(trigger.getConditions(), context, victim)) {
            debug("Conditions failed for " + enchant.getId() + " trigger=" + triggerType);
            return;
        }

        if (trigger.getActivationLimit() > -1) {
            int activated = activationCounts.getOrDefault(cooldownKey, 0);
            if (activated >= trigger.getActivationLimit()) {
                return;
            }
            activationCounts.put(cooldownKey, activated + 1);
        }

        cooldownService.putPlayerCooldown(context.getOwnerId(), cooldownKey, playerCooldown);
        cooldownService.putGlobalCooldown(cooldownKey, globalCooldown);

        if (trigger.isCancelEvent() && event instanceof Cancellable cancellable) {
            cancellable.setCancelled(true);
        }

        Runnable task = () -> {
            Collection<LivingEntity> targets = resolveTargets(trigger.getTarget(), context, trigger.getRadius());
            for (EffectDefinition effect : trigger.getEffects()) {
                EffectExecutor executor = effectRegistry.get(effect.type());
                if (executor == null) {
                    plugin.getLogger().warning("Unknown custom-enchant effect: " + effect.type());
                    continue;
                }

                try {
                    executor.execute(context, targets, effect.value());
                    debug("Executed effect " + effect.type() + " for " + enchant.getId() + " targets=" + targets.size());
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to execute effect " + effect.type() + " for " + enchant.getId(), ex);
                }
            }

            if (context.getChainDepth() > 4) {
                return;
            }

            for (String chained : trigger.getChainTriggers()) {
                TriggerDefinition chainedTrigger = enchant.getTriggers().get(chained);
                if (chainedTrigger == null) {
                    continue;
                }

                context.incrementChainDepth();
                runTrigger(enchant, chainedTrigger, TriggerType.fromKey(chained), event, owner, attacker, victim, location, level);
            }
        };

        int delay = Math.max(0, trigger.getDelayTicks());
        int repeat = Math.max(1, trigger.getRepeatCount());
        int interval = Math.max(1, trigger.getRepeatIntervalTicks());

        for (int i = 0; i < repeat; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay + (long) i * interval);
        }
    }

    private Collection<LivingEntity> resolveTargets(String targetType, ExecutionContext context, String radiusExpression) {
        TargetSelector selector = targetRegistry.get(targetType);
        if (selector == null) {
            return List.of();
        }

        double radius = Math.max(1D, formulaEngine.evaluate(radiusExpression, context.getVariables()));
        context.getVariables().put("radius", radius);

        return selector.select(context, radius);
    }

    private boolean conditionsPass(List<Condition> conditions, ExecutionContext context, LivingEntity target) {
        for (Condition condition : conditions) {
            if (!condition.test(context, target)) {
                return false;
            }
        }

        return true;
    }

    private void fillVariables(ExecutionContext context) {
        context.getVariables().put("level", (double) context.getLevel());
        context.getVariables().put("random", ThreadLocalRandom.current().nextDouble(100D));
        context.getVariables().put("stack", (double) statusStackService.get(stackKey(context)));
        context.getVariables().put("combo", (double) comboStateService.getHits(comboKey(context), 40L * 50L));
        context.getVariables().put("balance", context.getOwner() == null ? 0D : economyService.balance(context.getOwner()));
        context.getVariables().put("region_allowed", regionHook.isAllowed(context.getOwner(), context.getLocation()) ? 1D : 0D);

        if (context.getAttacker() != null) {
            context.getVariables().put("attacker_health", context.getAttacker().getHealth());
            if (context.getAttacker().getAttribute(Attribute.MAX_HEALTH) != null) {
                context.getVariables().put("attacker_max_health", context.getAttacker().getAttribute(Attribute.MAX_HEALTH).getValue());
            }
        }

        if (context.getVictim() != null) {
            context.getVariables().put("victim_health", context.getVictim().getHealth());
            if (context.getVictim().getAttribute(Attribute.MAX_HEALTH) != null) {
                context.getVariables().put("victim_max_health", context.getVictim().getAttribute(Attribute.MAX_HEALTH).getValue());
            }
        }

        if (context.getAttacker() != null && context.getVictim() != null) {
            context.getVariables().put("distance", context.getAttacker().getLocation().distance(context.getVictim().getLocation()));
        }
    }

    private void registerBuiltIns() {
        registerDefaultPlaceholders();
        registerDefaultTargets();
        registerDefaultConditions();
        registerDefaultEffects();
    }

    private void registerDefaultPlaceholders() {
        placeholderRegistry.register("player", c -> c.getOwner() == null ? "" : c.getOwner().getName());
        placeholderRegistry.register("attacker", c -> c.getAttacker() == null ? "" : c.getAttacker().getName());
        placeholderRegistry.register("victim", c -> c.getVictim() == null ? "" : c.getVictim().getName());
        placeholderRegistry.register("world", c -> c.getLocation() == null ? "" : c.getLocation().getWorld().getName());
        placeholderRegistry.register("x", c -> c.getLocation() == null ? "0" : String.valueOf(c.getLocation().getBlockX()));
        placeholderRegistry.register("y", c -> c.getLocation() == null ? "0" : String.valueOf(c.getLocation().getBlockY()));
        placeholderRegistry.register("z", c -> c.getLocation() == null ? "0" : String.valueOf(c.getLocation().getBlockZ()));
        placeholderRegistry.register("level", c -> String.valueOf(c.getLevel()));
    }

    private void registerDefaultTargets() {
        targetRegistry.register("SELF", (context, radiusObj) -> context.getOwner() == null ? List.of() : List.of(context.getOwner()));
        targetRegistry.register("ATTACKER", (context, radiusObj) -> context.getAttacker() == null ? List.of() : List.of(context.getAttacker()));
        targetRegistry.register("VICTIM", (context, radiusObj) -> context.getVictim() == null ? List.of() : List.of(context.getVictim()));

        targetRegistry.register("PLAYERS", (context, radiusObj) -> nearby(context, radiusObj).stream()
                .filter(e -> e instanceof Player)
                .toList());

        targetRegistry.register("MONSTERS", (context, radiusObj) -> nearby(context, radiusObj).stream()
                .filter(e -> e instanceof Monster)
                .toList());

        targetRegistry.register("ANIMALS", (context, radiusObj) -> nearby(context, radiusObj).stream()
                .filter(e -> e instanceof Animals)
                .toList());

        targetRegistry.register("ALL_ENTITIES", this::nearby);
        targetRegistry.register("NEARBY_ENTITIES", this::nearby);
        targetRegistry.register("ENEMIES", (context, radiusObj) -> nearby(context, radiusObj).stream()
                .filter(e -> !(e instanceof Animals))
                .filter(e -> !(context.getOwner() != null && e.getUniqueId().equals(context.getOwner().getUniqueId())))
                .toList());
    }

    private Collection<LivingEntity> nearby(ExecutionContext context, Object radiusObj) {
        if (context.getLocation() == null || context.getLocation().getWorld() == null) {
            return List.of();
        }

        double radius = radiusObj instanceof Number n ? n.doubleValue() : 6D;
        return context.getLocation().getWorld().getNearbyLivingEntities(context.getLocation(), radius, radius, radius)
                .stream()
                .sorted(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(context.getLocation())))
                .toList();
    }

    private void registerDefaultConditions() {
        conditionRegistry.register("victim_not_poisoned", ignored -> (context, target) ->
                context.getVictim() == null || !context.getVictim().hasPotionEffect(PotionEffectType.POISON));

        conditionRegistry.register("attacker_sneaking", ignored -> (context, target) ->
                context.getAttacker() instanceof Player player && player.isSneaking());

        conditionRegistry.register("attacker_sprinting", ignored -> (context, target) ->
                context.getAttacker() instanceof Player player && player.isSprinting());

        conditionRegistry.register("permission", value -> (context, target) ->
                context.getOwner() != null && context.getOwner().hasPermission(String.valueOf(value)));

        conditionRegistry.register("world", value -> {
            List<String> worlds = value instanceof List<?> list
                    ? list.stream().map(String::valueOf).map(String::toLowerCase).toList()
                    : List.of(String.valueOf(value).toLowerCase());

            return (context, target) -> context.getLocation() != null
                    && worlds.contains(context.getLocation().getWorld().getName().toLowerCase());
        });

        conditionRegistry.register("region_allowed", ignored -> (context, target) ->
                regionHook.isAllowed(context.getOwner(), context.getLocation()));

        conditionRegistry.register("region_name", value -> {
            List<String> regions = value instanceof List<?> list
                    ? list.stream().map(String::valueOf).map(String::toLowerCase).toList()
                    : List.of(String.valueOf(value).toLowerCase());

            return (context, target) -> {
                String current = regionHook.getRegionName(context.getLocation());
                return !current.isBlank() && regions.contains(current.toLowerCase());
            };
        });

        conditionRegistry.register("balance_at_least", value -> (context, target) -> {
            if (context.getOwner() == null) {
                return false;
            }

            double required = value instanceof Number number
                    ? number.doubleValue()
                    : formulaEngine.evaluate(String.valueOf(value), context.getVariables());
            return economyService.has(context.getOwner(), required);
        });

        conditionRegistry.register("stack_at_least", value -> {
            int amount = value instanceof Number number
                    ? number.intValue()
                    : (int) Math.round(Double.parseDouble(String.valueOf(value)));
            return (context, target) -> statusStackService.get(stackKey(context)) >= amount;
        });

        conditionRegistry.register("combo_at_least", value -> {
            int amount = value instanceof Number number
                    ? number.intValue()
                    : (int) Math.round(Double.parseDouble(String.valueOf(value)));
            return (context, target) -> comboStateService.getHits(comboKey(context), 40L * 50L) >= amount;
        });
    }

    private void registerDefaultEffects() {
        effectRegistry.register("damage", (context, targets, value) -> {
            String expression = String.valueOf(value);
            for (LivingEntity target : targets) {
                double damage = formulaEngine.evaluate(expression, context.getVariables());
                context.getVariables().put("damage", damage);
                target.damage(Math.max(0D, damage), context.getAttacker());
            }
        });

        effectRegistry.register("true-damage", (context, targets, value) -> {
            String expression = String.valueOf(value);
            for (LivingEntity target : targets) {
                double amount = Math.max(0D, formulaEngine.evaluate(expression, context.getVariables()));
                target.setHealth(Math.max(0D, target.getHealth() - amount));
            }
        });

        effectRegistry.register("heal", (context, targets, value) -> {
            String expression = String.valueOf(value);
            for (LivingEntity target : targets) {
                if (target.getAttribute(Attribute.MAX_HEALTH) == null) {
                    continue;
                }

                double max = target.getAttribute(Attribute.MAX_HEALTH).getValue();
                double amount = Math.max(0D, formulaEngine.evaluate(expression, context.getVariables()));
                target.setHealth(Math.min(max, target.getHealth() + amount));
            }
        });

        effectRegistry.register("lifesteal", (context, targets, value) -> {
            if (context.getAttacker() == null || context.getAttacker().getAttribute(Attribute.MAX_HEALTH) == null) {
                return;
            }

            String expression = String.valueOf(value);
            double healed = Math.max(0D, formulaEngine.evaluate(expression, context.getVariables()));
            double max = Objects.requireNonNull(context.getAttacker().getAttribute(Attribute.MAX_HEALTH)).getValue();
            context.getAttacker().setHealth(Math.min(max, context.getAttacker().getHealth() + healed));
        });

        effectRegistry.register("potion", (context, targets, value) -> {
            if (!(value instanceof Map<?, ?> map)) {
                return;
            }

            String typeName = getMapString(map, "type", "SPEED");
            String durationExp = getMapString(map, "duration", "60");
            String ampExp = getMapString(map, "amplifier", "0");

            PotionEffectType type = PotionEffectType.getByName(typeName.toUpperCase());
            if (type == null) {
                return;
            }

            int duration = Math.max(1, formulaEngine.evaluateInt(durationExp, context.getVariables()));
            int amplifier = Math.max(0, formulaEngine.evaluateInt(ampExp, context.getVariables()));

            PotionEffect effect = new PotionEffect(type, duration, amplifier);
            for (LivingEntity target : targets) {
                target.addPotionEffect(effect);
            }
        });

        effectRegistry.register("particle", (context, targets, value) -> {
            if (!(value instanceof Map<?, ?> map)) {
                return;
            }

            String particleName = getMapString(map, "type", "CRIT");
            Particle particle;

            try {
                particle = Particle.valueOf(particleName.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return;
            }

            int count = Integer.parseInt(getMapString(map, "count", "12"));
            for (LivingEntity target : targets) {
                target.getWorld().spawnParticle(particle, target.getLocation().add(0, 1, 0), count, 0.25, 0.25, 0.25, 0.01);
            }
        });

        effectRegistry.register("sound", (context, targets, value) -> {
            Sound sound;
            try {
                sound = Sound.valueOf(String.valueOf(value));
            } catch (IllegalArgumentException ex) {
                return;
            }

            for (LivingEntity target : targets) {
                target.getWorld().playSound(target.getLocation(), sound, 1f, 1f);
            }
        });

        effectRegistry.register("command", (context, targets, value) -> {
            String command = resolvePlaceholders(String.valueOf(value), context);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.startsWith("/") ? command.substring(1) : command);
        });

        effectRegistry.register("message", (context, targets, value) -> {
            String message = Utils.format(resolvePlaceholders(String.valueOf(value), context));
            for (LivingEntity target : targets) {
                if (target instanceof Player player) {
                    player.sendMessage(message);
                }
            }
        });

        effectRegistry.register("withdraw", (context, targets, value) -> {
            if (context.getOwner() == null) {
                return;
            }

            double amount = value instanceof Number number
                    ? number.doubleValue()
                    : formulaEngine.evaluate(String.valueOf(value), context.getVariables());
            economyService.withdraw(context.getOwner(), Math.max(0D, amount));
            context.getVariables().put("balance", economyService.balance(context.getOwner()));
        });

        effectRegistry.register("deposit", (context, targets, value) -> {
            if (context.getOwner() == null) {
                return;
            }

            double amount = value instanceof Number number
                    ? number.doubleValue()
                    : formulaEngine.evaluate(String.valueOf(value), context.getVariables());
            economyService.deposit(context.getOwner(), Math.max(0D, amount));
            context.getVariables().put("balance", economyService.balance(context.getOwner()));
        });

        effectRegistry.register("stack_add", (context, targets, value) -> {
            int amount = 1;
            long durationMillis = 10_000L;

            if (value instanceof Map<?, ?> map) {
                amount = (int) Math.round(formulaEngine.evaluate(getMapString(map, "amount", "1"), context.getVariables()));
                durationMillis = Math.max(0L, formulaEngine.evaluateInt(getMapString(map, "duration", "200"), context.getVariables()) * 50L);
            } else if (value != null) {
                amount = (int) Math.round(formulaEngine.evaluate(String.valueOf(value), context.getVariables()));
            }

            int stacks = statusStackService.add(stackKey(context), amount, durationMillis);
            context.getVariables().put("stack", (double) stacks);
        });

        effectRegistry.register("stack_clear", (context, targets, value) -> {
            statusStackService.clear(stackKey(context));
            context.getVariables().put("stack", 0D);
        });

        effectRegistry.register("combo_reset", (context, targets, value) -> {
            comboStateService.reset(comboKey(context));
            context.getVariables().put("combo", 0D);
        });
    }

    private RegionHook resolveRegionHook() {
        if (Bukkit.getPluginManager().isPluginEnabled("Residence")) {
            return new ResidenceRegionHook();
        }

        return new NoopRegionHook();
    }

    private String stackKey(ExecutionContext context) {
        String ownerId = context.getOwner() == null ? "none" : context.getOwner().getUniqueId().toString();
        return context.getEnchant().getId() + ":" + ownerId;
    }

    private String comboKey(ExecutionContext context) {
        String attackerId = context.getAttacker() == null ? "none" : context.getAttacker().getUniqueId().toString();
        String victimId = context.getVictim() == null ? "none" : context.getVictim().getUniqueId().toString();
        return context.getEnchant().getId() + ":" + attackerId + ":" + victimId;
    }

    private String resolvePlaceholders(String input, ExecutionContext context) {
        String resolved = placeholderRegistry.resolveAll(input, context);

        if (context.getOwner() != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            resolved = PlaceholderAPI.setPlaceholders(context.getOwner(), resolved);
        }

        return resolved;
    }

    private void saveDefaultExamples() {
        if (!enchantDirectory.exists()) {
            enchantDirectory.mkdirs();
        }

        copyDefaultIfMissing("custom-enchants/venom.yml");
        copyDefaultIfMissing("custom-enchants/leeching.yml");
        copyDefaultIfMissing("custom-enchants/freeze.yml");
        copyDefaultIfMissing("custom-enchants/soulrend.yml");
        copyDefaultIfMissing("custom-enchants/thunderclap.yml");
        copyDefaultIfMissing("custom-enchants/executioner.yml");
        copyDefaultIfMissing("custom-enchants/hex.yml");
        copyDefaultIfMissing("custom-enchants/bloodrush.yml");
        copyDefaultIfMissing("custom-enchants/momentum.yml");
        copyDefaultIfMissing("custom-enchants/windstep.yml");
        copyDefaultIfMissing("custom-enchants/foothold.yml");
        copyDefaultIfMissing("custom-enchants/adrenaline.yml");
        copyDefaultIfMissing("custom-enchants/warding.yml");
        copyDefaultIfMissing("custom-enchants/retaliate.yml");
        copyDefaultIfMissing("custom-enchants/bulwark.yml");
        copyDefaultIfMissing("custom-enchants/overcharge.yml");
        copyDefaultIfMissing("custom-enchants/quarry.yml");
        copyDefaultIfMissing("custom-enchants/prospect.yml");
        copyDefaultIfMissing("custom-enchants/overgrowth.yml");
        copyDefaultIfMissing("custom-enchants/anglerluck.yml");
        copyDefaultIfMissing("custom-enchants/battletrance.yml");
        copyDefaultIfMissing("custom-enchants/skirmisher.yml");
        copyDefaultIfMissing("custom-enchants/duskcloak.yml");
    }

    private void copyDefaultIfMissing(String resourcePath) {
        File out = new File(plugin.getDataFolder(), resourcePath);
        if (!out.exists()) {
            plugin.saveResource(resourcePath, false);
        }
    }

    private String getMapString(Map<?, ?> map, String key, String def) {
        Object value = map.get(key);
        return value == null ? def : String.valueOf(value);
    }

    private String renderLoreLine(CustomEnchantDefinition definition, int level) {
        String baseName = definition.getDisplayName() == null || definition.getDisplayName().isBlank()
                ? definition.getId()
                : definition.getDisplayName();

        String format = plugin.getCfg().getString("custom-enchants.lore-format", "&7%enchant_name% %level_roman%");

        return Utils.format(format
                .replace("%enchant_name%", Utils.format(baseName))
                .replace("%level_roman%", Utils.arabicToRoman(level))
                .replace("%level%", String.valueOf(level)));
    }

    private String toConfigTrigger(TriggerType type) {
        String[] split = type.name().toLowerCase().split("_");
        if (split.length == 0) {
            return "";
        }

        StringBuilder out = new StringBuilder(split[0]);
        for (int i = 1; i < split.length; i++) {
            if (split[i].isEmpty()) {
                continue;
            }

            out.append(Character.toUpperCase(split[i].charAt(0)));
            if (split[i].length() > 1) {
                out.append(split[i], 1, split[i].length());
            }
        }

        return out.toString();
    }

    private void debug(String message) {
        plugin.debug("[CustomEnchantFramework] " + message);
    }

    private boolean isConfigDrivenEnabled() {
        return plugin.getCfg().getBoolean("config-driven-enchants", true);
    }
}



