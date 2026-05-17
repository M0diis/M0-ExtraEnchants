package me.m0dii.extraenchants.mockbukkit;

import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.Mockito;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllEnchantWrappersCoverageMockBukkitTest {

    private static final int EXPECTED_WRAPPER_COUNT = 31;

    private static ServerMock server;
    private static ExtraEnchants plugin;

    @BeforeAll
    static void beforeAll() throws Exception {
        server = MockBukkit.mock();

        YamlConfiguration config = new YamlConfiguration();
        for (EEnchant enchant : EEnchant.values()) {
            String base = "enchants." + enchant.getConfigName();
            config.set(base + ".enabled", true);
            config.set(base + ".trigger-chance", -1);
            config.set(base + ".default-conflicts", true);
            config.set(base + ".enchantable-items", java.util.List.of());
            config.set(base + ".conflicts", java.util.List.of());
            config.set(base + ".ignored-blocks", java.util.List.of());
        }

        PlatformScheduler scheduler = Mockito.mock(PlatformScheduler.class);
        WrappedTask wrappedTask = Mockito.mock(WrappedTask.class);
        Mockito.lenient().when(scheduler.runTimer(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.anyLong())).thenReturn(wrappedTask);
        Mockito.lenient().when(scheduler.runLater(Mockito.any(Runnable.class), Mockito.anyLong())).thenReturn(wrappedTask);

        plugin = Mockito.mock(ExtraEnchants.class);
        Mockito.when(plugin.getName()).thenReturn("M0-ExtraEnchants");
        Mockito.when(plugin.getConfig()).thenReturn(config);
        Mockito.when(plugin.getCfg()).thenReturn(config);
        Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("AllEnchantWrappersCoverageMockBukkitTest"));
        Mockito.when(plugin.getScheduler()).thenReturn(scheduler);

        TestPluginBindingUtil.bindPlugin(plugin);
    }

    @AfterAll
    static void afterAll() throws Exception {
        TestPluginBindingUtil.bindPlugin(null);
        if (server != null) {
            MockBukkit.unmock();
        }
    }

    @Test
    void allEnchantWrappersCanBeDiscoveredAndConstructed() throws Exception {
        Reflections reflections = new Reflections("me.m0dii.extraenchants.enchants.wrappers");
        Set<Class<? extends CustomEnchantment>> wrappers = reflections.getSubTypesOf(CustomEnchantment.class);

        assertEquals(EXPECTED_WRAPPER_COUNT, wrappers.size(), "Wrapper count changed. Update test expectations.");

        for (Class<? extends CustomEnchantment> wrapperClass : wrappers) {
            assertTrue(wrapperClass.isAnnotationPresent(EnchantWrapper.class), wrapperClass.getName() + " must declare @EnchantWrapper");

            EnchantWrapper annotation = wrapperClass.getAnnotation(EnchantWrapper.class);
            EEnchant parsed = EEnchant.parse(annotation.name());
            assertNotNull(parsed, "Could not parse EEnchant for wrapper name: " + annotation.name());

            Constructor<? extends CustomEnchantment> ctor = wrapperClass.getConstructor(String.class, int.class, EEnchant.class);
            CustomEnchantment wrapper = ctor.newInstance(annotation.name(), annotation.maxLevel(), parsed);

            assertNotNull(wrapper);
            assertEquals(annotation.name(), wrapper.getName());
            assertEquals(annotation.maxLevel(), wrapper.getMaxLevel());
        }
    }

}

