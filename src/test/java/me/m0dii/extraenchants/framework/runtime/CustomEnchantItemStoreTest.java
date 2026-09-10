package me.m0dii.extraenchants.framework.runtime;

import me.m0dii.extraenchants.ExtraEnchants;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomEnchantItemStoreTest {
    @BeforeEach
    void mockServer() {
        MockBukkit.mock();
    }

    @AfterEach
    void unmockServer() {
        MockBukkit.unmock();
    }

    @Test
    void customEnchantLevelSurvivesStoreRecreationThroughPdc() {
        ExtraEnchants plugin = Mockito.mock(ExtraEnchants.class);
        Mockito.when(plugin.getName()).thenReturn("M0-ExtraEnchants");
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);

        new CustomEnchantItemStore(plugin).setLevel(item, "battletrance", 3);

        assertEquals(3, new CustomEnchantItemStore(plugin).getLevel(item, "battletrance"));
    }
}
