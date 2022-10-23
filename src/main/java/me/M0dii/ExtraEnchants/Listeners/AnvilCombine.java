package me.M0dii.ExtraEnchants.Listeners;

import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import me.M0dii.ExtraEnchants.ExtraEnchants;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class AnvilCombine implements Listener {
    @EventHandler
    public void anvilCombine(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();

        if (result == null)
            return;

        if (!result.hasItemMeta())
            return;

        ItemMeta itemMeta = result.getItemMeta();

        if(itemMeta == null) {
            return;
        }

        if (!itemMeta.hasLore()) {
            return;
        }

        boolean tele = false;
        boolean plow = false;
        boolean smelt = false;
        boolean bonded = false;
        boolean lava = false;

        List<String> lore = itemMeta.getLore();

        if(lore == null) {
            return;
        }

        for (String l : lore) {
            if (l.contains("Telepathy"))
                tele = true;

            if (l.contains("Plow"))
                plow = true;

            if (l.contains("Smelt"))
                smelt = true;

            if (l.contains("Bonded"))
                bonded = true;

            if (l.contains("Lava"))
                lava = true;
        }

        if (tele) {
            result.addUnsafeEnchantment(CustomEnchants.TELEPATHY, 1);
        }

        if (plow) {
            result.addUnsafeEnchantment(CustomEnchants.PLOW, 1);
        }

        if (smelt) {
            result.addUnsafeEnchantment(CustomEnchants.SMELT, 1);
        }

        if (bonded) {
            result.addUnsafeEnchantment(CustomEnchants.BONDED, 1);
        }

        if (lava) {
            result.addUnsafeEnchantment(CustomEnchants.LAVA_WALKER, 1);
        }
    }
}