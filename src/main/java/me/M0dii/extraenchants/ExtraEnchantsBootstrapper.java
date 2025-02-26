package me.m0dii.extraenchants;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.utils.EnchantWrapper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.reflections.Reflections;

import java.util.Set;

public class ExtraEnchantsBootstrapper implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.freeze().newHandler(event -> {
            Reflections reflections = new Reflections("me.m0dii.extraenchants.enchants.wrappers");
            Set<Class<? extends CustomEnchantment>> enchantClasses = reflections.getSubTypesOf(CustomEnchantment.class);
            for (Class<? extends CustomEnchantment> clazz : enchantClasses) {
                if (!clazz.isAnnotationPresent(EnchantWrapper.class)) {
                    continue;
                }
                EnchantWrapper wrapper = clazz.getAnnotation(EnchantWrapper.class);
                event.registry().register(
                        EnchantmentKeys.create(Key.key("custom:" + wrapper.name().toLowerCase().replace(" ", "_"))),
                        b -> b.description(Component.text(wrapper.name()))
                                .maxLevel(wrapper.maxLevel())
                                .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS))
                                .weight(10)
                                .anvilCost(1)
                                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 1))
                                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(3, 1))
                                .activeSlots(EquipmentSlotGroup.ANY)

                );
            }
        }));
    }
}