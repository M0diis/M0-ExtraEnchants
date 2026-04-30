package me.m0dii.extraenchants;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.reflections.Reflections;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ALL")
public class ExtraEnchantsBootstrapper implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        Map<String, BootstrapEnchant> enchants = new LinkedHashMap<>();

        loadWrapperEnchants(enchants);
        loadConfigDrivenEnchants(context, enchants);

        context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler(event -> {
            for (BootstrapEnchant enchant : enchants.values()) {
                event.registry().register(
                        EnchantmentKeys.create(Key.key(enchant.key())),
                        b -> b.description(formatEnchantDescription(enchant.displayName()))
                                .maxLevel(enchant.maxLevel())
                                .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS))
                                .weight(enchant.weight())
                                .anvilCost(1)
                                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 1))
                                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(3, 1))
                                .activeSlots(EquipmentSlotGroup.ANY)

                );
            }
        }));
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
            enchants.put(key, new BootstrapEnchant(key, wrapper.name(), wrapper.maxLevel(), 10));
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

            try (var files = Files.list(dir)) {
                files.filter(path -> path.getFileName().toString().endsWith(".yml"))
                        .forEach(path -> {
                            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(path.toFile());
                            String id = yaml.getString("id", path.getFileName().toString().replace(".yml", ""));
                            String displayName = yaml.getString("display-name", id);
                            int maxLevel = Math.max(1, yaml.getInt("max-level", 1));
                            int weight = Math.max(1, yaml.getInt("weight", yaml.getInt("spawn-chance", 10)));
                            String key = toNamespacedKey(id);

                            // Wrapper-based entries have priority for backwards compatibility.
                            enchants.putIfAbsent(key, new BootstrapEnchant(key, displayName, maxLevel, weight));
                        });
            }
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

    private record BootstrapEnchant(String key, String displayName, int maxLevel, int weight) {
    }
}