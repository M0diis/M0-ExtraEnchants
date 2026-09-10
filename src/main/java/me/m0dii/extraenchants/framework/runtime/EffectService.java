package me.m0dii.extraenchants.framework.runtime;

import me.clip.placeholderapi.PlaceholderAPI;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.framework.api.EffectExecutor;
import me.m0dii.extraenchants.framework.model.EffectDefinition;
import me.m0dii.extraenchants.framework.registry.EffectRegistry;
import me.m0dii.extraenchants.framework.registry.PlaceholderRegistry;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.logging.Level;

/** Executes effects and owns the built-in effect implementations. */
public final class EffectService {
    private final ExtraEnchants plugin;
    private final Exp4jFormulaEngine formulaEngine;
    private final EconomyService economyService;
    private final StatusStackService statusStackService;
    private final ComboStateService comboStateService;
    private final EffectRegistry registry;
    private final PlaceholderRegistry placeholderRegistry;
    private final Consumer<String> debug;

    public EffectService(
            ExtraEnchants plugin,
            Exp4jFormulaEngine formulaEngine,
            EconomyService economyService,
            StatusStackService statusStackService,
            ComboStateService comboStateService,
            EffectRegistry registry,
            PlaceholderRegistry placeholderRegistry,
            Consumer<String> debug
    ) {
        this.plugin = plugin;
        this.formulaEngine = formulaEngine;
        this.economyService = economyService;
        this.statusStackService = statusStackService;
        this.comboStateService = comboStateService;
        this.registry = registry;
        this.placeholderRegistry = placeholderRegistry;
        this.debug = debug == null ? ignored -> { } : debug;
        registerDefaults();
    }

    public void executeAll(ExecutionContext context, Collection<LivingEntity> targets) {
        for (EffectDefinition effect : context.getTrigger().getEffects()) {
            EffectExecutor executor = registry.get(effect.type());
            if (executor == null) {
                plugin.getLogger().warning("Unknown custom-enchant effect: " + effect.type());
                continue;
            }

            try {
                executor.execute(context, targets, effect.value());
                debug.accept("Executed effect " + effect.type() + " for "
                        + context.getEnchant().getId() + " targets=" + targets.size());
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to execute effect " + effect.type()
                        + " for " + context.getEnchant().getId(), ex);
            }
        }
    }

    private void registerDefaults() {
        registry.register("damage", (context, targets, value) -> {
            String expression = String.valueOf(value);
            for (LivingEntity target : targets) {
                double damage = formulaEngine.evaluate(expression, context.getVariables());
                context.getVariables().put("damage", damage);
                target.damage(Math.max(0D, damage), context.getAttacker());
            }
        });
        registry.register("true-damage", (context, targets, value) -> {
            String expression = String.valueOf(value);
            for (LivingEntity target : targets) {
                double amount = Math.max(0D, formulaEngine.evaluate(expression, context.getVariables()));
                target.setHealth(Math.max(0D, target.getHealth() - amount));
            }
        });
        registry.register("heal", (context, targets, value) -> {
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
        registry.register("lifesteal", (context, targets, value) -> {
            if (context.getAttacker() == null || context.getAttacker().getAttribute(Attribute.MAX_HEALTH) == null) {
                return;
            }
            double healed = Math.max(0D, formulaEngine.evaluate(String.valueOf(value), context.getVariables()));
            double max = Objects.requireNonNull(context.getAttacker().getAttribute(Attribute.MAX_HEALTH)).getValue();
            context.getAttacker().setHealth(Math.min(max, context.getAttacker().getHealth() + healed));
        });
        registry.register("potion", (context, targets, value) -> {
            if (!(value instanceof Map<?, ?> map)) {
                return;
            }
            PotionEffectType type = PotionEffectType.getByName(getMapString(map, "type", "SPEED").toUpperCase());
            if (type == null) {
                return;
            }
            int duration = Math.max(1, formulaEngine.evaluateInt(getMapString(map, "duration", "60"), context.getVariables()));
            int amplifier = Math.max(0, formulaEngine.evaluateInt(getMapString(map, "amplifier", "0"), context.getVariables()));
            PotionEffect effect = new PotionEffect(type, duration, amplifier);
            for (LivingEntity target : targets) {
                target.addPotionEffect(effect);
            }
        });
        registry.register("particle", (context, targets, value) -> {
            if (!(value instanceof Map<?, ?> map)) {
                return;
            }
            Particle particle;
            try {
                particle = Particle.valueOf(getMapString(map, "type", "CRIT").toUpperCase());
            } catch (IllegalArgumentException ex) {
                return;
            }
            int count = Math.max(0, formulaEngine.evaluateInt(getMapString(map, "count", "12"), context.getVariables()));
            for (LivingEntity target : targets) {
                Location location = target.getLocation().add(0, 1, 0);
                if (particle.getDataType() == Color.class) {
                    target.getWorld().spawnParticle(
                            particle,
                            location,
                            count,
                            .25,
                            .25,
                            .25,
                            .01,
                            parseParticleColor(map.get("color")),
                            false
                    );
                } else {
                    target.getWorld().spawnParticle(particle, location, count, .25, .25, .25, .01);
                }
            }
        });
        registry.register("sound", (context, targets, value) -> {
            NamespacedKey soundKey = NamespacedKey.fromString(String.valueOf(value).toLowerCase(Locale.ROOT));
            Sound sound = soundKey == null ? null : Registry.SOUNDS.get(soundKey);
            if (sound == null) {
                return;
            }
            for (LivingEntity target : targets) {
                target.getWorld().playSound(target.getLocation(), sound, 1f, 1f);
            }
        });
        registry.register("command", (context, targets, value) -> {
            String command = resolvePlaceholders(String.valueOf(value), context);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.startsWith("/") ? command.substring(1) : command);
        });
        registry.register("message", (context, targets, value) -> {
            String message = Utils.format(resolvePlaceholders(String.valueOf(value), context));
            for (LivingEntity target : targets) {
                if (target instanceof Player player) {
                    player.sendMessage(message);
                }
            }
        });
        registry.register("withdraw", (context, targets, value) -> {
            if (context.getOwner() == null) {
                return;
            }
            double amount = value instanceof Number number
                    ? number.doubleValue()
                    : formulaEngine.evaluate(String.valueOf(value), context.getVariables());
            economyService.withdraw(context.getOwner(), Math.max(0D, amount));
            context.getVariables().put("balance", economyService.balance(context.getOwner()));
        });
        registry.register("deposit", (context, targets, value) -> {
            if (context.getOwner() == null) {
                return;
            }
            double amount = value instanceof Number number
                    ? number.doubleValue()
                    : formulaEngine.evaluate(String.valueOf(value), context.getVariables());
            economyService.deposit(context.getOwner(), Math.max(0D, amount));
            context.getVariables().put("balance", economyService.balance(context.getOwner()));
        });
        registry.register("stack_add", (context, targets, value) -> {
            int amount = 1;
            long durationMillis = 10_000L;
            if (value instanceof Map<?, ?> map) {
                amount = (int) Math.round(formulaEngine.evaluate(getMapString(map, "amount", "1"), context.getVariables()));
                durationMillis = Math.max(0L, formulaEngine.evaluateInt(
                        getMapString(map, "duration", "200"), context.getVariables()) * 50L);
            } else if (value != null) {
                amount = (int) Math.round(formulaEngine.evaluate(String.valueOf(value), context.getVariables()));
            }
            int stacks = statusStackService.add(context.getOwnerId(), stackKey(context), amount, durationMillis);
            context.getVariables().put("stack", (double) stacks);
        });
        registry.register("stack_clear", (context, targets, value) -> {
            statusStackService.clear(context.getOwnerId(), stackKey(context));
            context.getVariables().put("stack", 0D);
        });
        registry.register("combo_reset", (context, targets, value) -> {
            comboStateService.reset(context.getOwnerId(), comboKey(context));
            context.getVariables().put("combo", 0D);
        });
    }

    private String resolvePlaceholders(String input, ExecutionContext context) {
        String resolved = placeholderRegistry.resolveAll(input, context);
        if (context.getOwner() != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            resolved = PlaceholderAPI.setPlaceholders(context.getOwner(), resolved);
        }
        return resolved;
    }

    private String stackKey(ExecutionContext context) {
        String ownerId = context.getOwnerId() == null ? "none" : context.getOwnerId().toString();
        return context.getEnchant().getId() + ":" + ownerId;
    }

    private String comboKey(ExecutionContext context) {
        String attackerId = context.getAttacker() == null ? "none" : context.getAttacker().getUniqueId().toString();
        String victimId = context.getVictim() == null ? "none" : context.getVictim().getUniqueId().toString();
        return context.getEnchant().getId() + ":" + attackerId + ":" + victimId;
    }

    private String getMapString(Map<?, ?> map, String key, String def) {
        Object value = map.get(key);
        return value == null ? def : String.valueOf(value);
    }

    private Color parseParticleColor(Object raw) {
        if (raw instanceof Color color) {
            return color;
        }

        if (raw instanceof Map<?, ?> map) {
            int red = channel(map.get("red"), 255);
            int green = channel(map.get("green"), 255);
            int blue = channel(map.get("blue"), 255);
            return Color.fromRGB(red, green, blue);
        }

        if (raw != null) {
            String value = String.valueOf(raw).trim();
            if (value.startsWith("#")) {
                value = value.substring(1);
            }
            try {
                if (value.matches("[0-9a-fA-F]{6}")) {
                    return Color.fromRGB(Integer.parseInt(value, 16));
                }
            } catch (NumberFormatException ignored) {
                // Fall through to the valid neutral default.
            }
        }

        return Color.WHITE;
    }

    private int channel(Object value, int fallback) {
        if (value instanceof Number number) {
            return Math.clamp(number.intValue(), 0, 255);
        }

        try {
            return Math.clamp(Integer.parseInt(String.valueOf(value)), 0, 255);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }
}
