package me.m0dii.extraenchants;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.framework.config.ConditionParser;
import me.m0dii.extraenchants.framework.config.CustomEnchantConfigLoader;
import me.m0dii.extraenchants.framework.config.CustomEnchantLoadResult;
import me.m0dii.extraenchants.framework.model.ItemApplicability;
import me.m0dii.extraenchants.framework.registry.ConditionRegistry;
import me.m0dii.extraenchants.framework.runtime.Exp4jFormulaEngine;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Registry;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.reflections.Reflections;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class ExtraEnchantsBootstrapper implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        Map<String, BootstrapEnchant> enchants = new LinkedHashMap<>();

        loadWrapperEnchants(enchants);
        if (isConfigDrivenEnabled(context)) {
            loadConfigDrivenEnchants(context, enchants);
        } else {
            context.getLogger().info("Config-driven enchants are disabled (config-driven-enchants=false)");
        }

        context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler(event -> {
            for (BootstrapEnchant enchant : enchants.values()) {
                Set<ItemType> supportedValues = resolveSupportedItems(context, event, enchant);

                if (supportedValues.isEmpty()) {
                    context.getLogger().warn("No item types resolved for custom enchant " + enchant.key()
                            + "; falling back to swords");
                    supportedValues.addAll(explicitItems(ItemApplicability.SWORD));
                }

                RegistryKeySet<ItemType> supportedItems = RegistrySet.keySetFromValues(RegistryKey.ITEM, supportedValues);

                event.registry().register(
                        EnchantmentKeys.create(Key.key(enchant.key())),
                        b -> b.description(formatEnchantDescription(enchant.displayName()))
                                .maxLevel(enchant.maxLevel())
                                .supportedItems(supportedItems)
                                .weight(enchant.weight())
                                .anvilCost(1)
                                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 1))
                                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(3, 1))
                                .activeSlots(EquipmentSlotGroup.ANY)

                );
            }
        }));
    }

    private Set<ItemType> resolveSupportedItems(
            BootstrapContext context,
            io.papermc.paper.registry.event.RegistryComposeEvent<org.bukkit.enchantments.Enchantment, EnchantmentRegistryEntry.Builder> event,
            BootstrapEnchant enchant
    ) {
        Set<ItemType> supportedValues = new LinkedHashSet<>();
        for (ItemApplicability applicability : enchant.applicableItems()) {
            if (applicability.getTagKey() == null) {
                supportedValues.addAll(explicitItems(applicability));
                continue;
            }

            try {
                Set<ItemType> resolved = event.getOrCreateTag(applicability.getTagKey())
                        .resolve(Registry.ITEM)
                        .stream()
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                if (resolved.isEmpty()) {
                    context.getLogger().warn("Item tag " + applicability.name() + " resolved no values for "
                            + enchant.key() + "; using explicit compatibility values");
                    resolved.addAll(explicitItems(applicability));
                }
                supportedValues.addAll(resolved);
            } catch (IllegalStateException | LinkageError ex) {
                // This also keeps the 26.3-compiled plugin bootable on a
                // 26.2 smoke server, whose registry does not bind 26.3 tags.
                context.getLogger().warn("Item tag " + applicability.name() + " is unavailable for "
                        + enchant.key() + "; using explicit compatibility values");
                supportedValues.addAll(explicitItems(applicability));
            }
        }
        return supportedValues;
    }

    private Set<ItemType> explicitItems(ItemApplicability applicability) {
        Set<ItemType> items = new LinkedHashSet<>();
        for (Field field : ItemType.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())
                    || !ItemType.class.isAssignableFrom(field.getType())
                    || !applicability.matchesName(field.getName())) {
                continue;
            }

            try {
                Object value = field.get(null);
                if (value instanceof ItemType itemType) {
                    items.add(itemType);
                }
            } catch (IllegalAccessException | LinkageError ignored) {
                // A version-specific item constant is optional for the
                // compatibility fallback and is skipped when unavailable.
            }
        }
        return items;
    }

    private boolean isConfigDrivenEnabled(BootstrapContext context) {
        Path configPath = context.getDataDirectory().resolve("config.yml");
        if (!Files.exists(configPath)) {
            return true;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configPath.toFile());
        return yaml.getBoolean("config-driven-enchants", true);
    }

    private void loadWrapperEnchants(Map<String, BootstrapEnchant> enchants) {
        Reflections reflections = new Reflections("me.m0dii.extraenchants.enchants.wrappers");
        Set<Class<? extends CustomEnchantment>> enchantClasses = reflections.getSubTypesOf(CustomEnchantment.class);

        for (Class<? extends CustomEnchantment> clazz : enchantClasses) {
            if (!clazz.isAnnotationPresent(EnchantWrapper.class)) {
                continue;
            }

            EnchantWrapper wrapper = clazz.getAnnotation(EnchantWrapper.class);
            String key = toNamespacedKey(wrapper.name());
            enchants.put(key, new BootstrapEnchant(
                    key,
                    wrapper.name(),
                    wrapper.maxLevel(),
                    10,
                    List.of(ItemApplicability.SWORD)
            ));
        }
    }

    private void loadConfigDrivenEnchants(BootstrapContext context, Map<String, BootstrapEnchant> enchants) {
        Path base = context.getDataDirectory();
        Path dir = base.resolve("custom-enchants");

        try {
            Files.createDirectories(dir);
            copyDefaultIfMissing(dir, "venom.yml");
            copyDefaultIfMissing(dir, "leeching.yml");
            copyDefaultIfMissing(dir, "freeze.yml");
            copyDefaultIfMissing(dir, "soulrend.yml");
            copyDefaultIfMissing(dir, "thunderclap.yml");
            copyDefaultIfMissing(dir, "executioner.yml");
            copyDefaultIfMissing(dir, "hex.yml");
            copyDefaultIfMissing(dir, "bloodrush.yml");
            copyDefaultIfMissing(dir, "momentum.yml");
            copyDefaultIfMissing(dir, "windstep.yml");
            copyDefaultIfMissing(dir, "foothold.yml");
            copyDefaultIfMissing(dir, "adrenaline.yml");
            copyDefaultIfMissing(dir, "warding.yml");
            copyDefaultIfMissing(dir, "retaliate.yml");
            copyDefaultIfMissing(dir, "bulwark.yml");
            copyDefaultIfMissing(dir, "overcharge.yml");
            copyDefaultIfMissing(dir, "quarry.yml");
            copyDefaultIfMissing(dir, "prospect.yml");
            copyDefaultIfMissing(dir, "overgrowth.yml");
            copyDefaultIfMissing(dir, "anglerluck.yml");
            copyDefaultIfMissing(dir, "battletrance.yml");
            copyDefaultIfMissing(dir, "skirmisher.yml");
            copyDefaultIfMissing(dir, "duskcloak.yml");

            CustomEnchantConfigLoader loader = new CustomEnchantConfigLoader(
                    null, new ConditionParser(new ConditionRegistry(), new Exp4jFormulaEngine()));
            CustomEnchantLoadResult result = loader.loadResult(dir.toFile());
            result.getIssues().forEach(issue -> {
                if (issue.isError()) {
                    context.getLogger().error(issue.format());
                } else {
                    context.getLogger().warn(issue.format());
                }
            });
            if (!result.isCommitted()) {
                context.getLogger().error("Custom-enchant bootstrap registration rejected because configuration is invalid");
                return;
            }

            result.getDefinitions().values().stream()
                    .sorted(Comparator.comparing(definition -> definition.getId().toLowerCase()))
                    .forEach(definition -> {
                        List<ItemApplicability> applicableItems = definition.getApplicableItems().stream()
                                .map(ItemApplicability::parse)
                                .flatMap(java.util.Optional::stream)
                                .toList();
                        // Wrapper-based entries have priority for backwards compatibility.
                        enchants.putIfAbsent(toNamespacedKey(definition.getId()), new BootstrapEnchant(
                                toNamespacedKey(definition.getId()),
                                definition.getDisplayName(),
                                definition.getMaxLevel(),
                                Math.max(1, definition.getWeight()),
                                applicableItems
                        ));
                    });
        } catch (IOException ex) {
            context.getLogger().error("Failed to read config-driven enchants for bootstrap registration", ex);
        }
    }

    private void copyDefaultIfMissing(Path dir, String fileName) throws IOException {
        Path target = dir.resolve(fileName);
        if (Files.exists(target)) {
            return;
        }

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("custom-enchants/" + fileName)) {
            if (in == null) {
                return;
            }

            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String toNamespacedKey(String raw) {
        return "custom:" + raw.toLowerCase().replace(" ", "_").replace(":", "");
    }

    private Component formatEnchantDescription(String raw) {
        if (raw == null || raw.isBlank()) {
            return Component.text("Custom Enchant");
        }

        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
    }

    private record BootstrapEnchant(
            String key,
            String displayName,
            int maxLevel,
            int weight,
            List<ItemApplicability> applicableItems
    ) {
    }
}
