package me.m0dii.extraenchants.framework.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemApplicabilityTest {
    @BeforeEach
    void mockServer() {
        MockBukkit.mock();
    }

    @AfterEach
    void unmockServer() {
        MockBukkit.unmock();
    }

    @Test
    void categoriesDoNotCollapseToSwordCompatibility() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemStack bow = new ItemStack(Material.BOW);
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);

        assertTrue(ItemApplicability.SWORD.matches(sword));
        assertFalse(ItemApplicability.SWORD.matches(crossbow));
        assertTrue(ItemApplicability.BOW.matches(bow));
        assertFalse(ItemApplicability.BOW.matches(sword));
        assertTrue(ItemApplicability.CROSSBOW.matches(crossbow));
        assertFalse(ItemApplicability.BOW.matches(crossbow));
        assertTrue(ItemApplicability.HELMET.matches(helmet));
    }
}
