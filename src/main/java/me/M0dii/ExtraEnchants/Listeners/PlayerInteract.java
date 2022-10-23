package me.M0dii.ExtraEnchants.Listeners;

import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import me.M0dii.ExtraEnchants.ExtraEnchants;
import me.M0dii.ExtraEnchants.Utils.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.stream.Stream;

public class PlayerInteract implements Listener {
    private ExtraEnchants plugin;

    public PlayerInteract(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemClick(PlayerInteractEvent e) {
        if (!e.hasItem())
            return;

        if (!e.getItem().hasItemMeta())
            return;

        if (!e.getItem().getType().equals(Material.ENCHANTED_BOOK))
            return;

        ItemMeta meta = e.getItem().getItemMeta();

        if (Stream.of(CustomEnchants.TELEPATHY,
                CustomEnchants.PLOW,
                CustomEnchants.LAVA_WALKER,
                CustomEnchants.BONDED,
                CustomEnchants.SMELT).allMatch(ench -> (!meta.hasEnchant(ench))))
            return;

        e.getPlayer().sendMessage(Utils.format(e.getItem().getItemMeta().getDisplayName()));

        for (String l : meta.getLore()) {
            e.getPlayer().sendMessage(Utils.format(l));
        }
    }
}