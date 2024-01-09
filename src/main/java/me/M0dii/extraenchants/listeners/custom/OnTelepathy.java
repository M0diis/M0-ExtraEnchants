package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.TelepathyEvent;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

public class OnTelepathy implements Listener {
    private static final Random r = new Random();

    private final ExtraEnchants plugin;

    public OnTelepathy(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTelepathy(final TelepathyEvent e) {
        if (!Utils.shouldTrigger(EEnchant.TELEPATHY)) {
            return;
        }

        Player player = e.getPlayer();
        Block b = e.getBlock();
        PlayerInventory inv = player.getInventory();
        ItemStack hand = inv.getItemInMainHand();

        Collection<ItemStack> drops = e.getDrops();

        boolean hasSilk = hand.getItemMeta()
                .getEnchants().containsKey(Enchantment.SILK_TOUCH);

        boolean fits = doesFit(inv, drops);

        if (b.getType().equals(Material.SPAWNER)
                || b.getType().name().toUpperCase().contains("SPAWNER"))
            return;

        if (!fits) {
            for (ItemStack i : drops)
                b.getWorld().dropItemNaturally(
                        b.getLocation(), i);

            InventoryUtils.applyDurability(hand);

            return;
        }

        if (hasSilk) {
            ItemStack itm = new ItemStack(b.getType());

            String name = itm.getType().name();

            if (name.contains("WALL_") || name.contains("BANNER")) {
                if (name.contains("BANNER")) {
                    for (ItemStack i : drops) {
                        inv.addItem(i);
                    }
                } else {
                    Material m = Material.getMaterial(name.replace("WALL_", ""));

                    if (m != null)
                        inv.addItem(new ItemStack(m));
                }
            } else {
                inv.addItem(itm);
            }
        } else if (inv.firstEmpty() == -1) {
            ItemStack item = drops.iterator().next();

            if (inv.contains(item)) {
                addToStack(player, drops);
            }
        } else {
            for (ItemStack i : drops) {
                inv.addItem(i);
            }
        }

        InventoryUtils.applyDurability(hand);
    }

    public boolean doesFit(Inventory inv, Collection<ItemStack> drops) {
        for (ItemStack i : inv.getStorageContents())
            if (i == null)
                return true;

        return hasSpaceForItem(drops, inv);
    }

    private void addToStack(Player p, Collection<ItemStack> drops) {
        ItemStack item = drops.iterator().next();
        ItemStack[] arrayOfItemStack;

        int j = (arrayOfItemStack = p.getInventory().getContents()).length;

        for (int i = 0; i < j; i++) {
            ItemStack it = arrayOfItemStack[i];

            if ((it.equals(item)) && (it.getAmount() < 64)) {
                for (ItemStack itm : drops) {
                    p.getInventory().addItem(itm);
                }

                break;
            }
        }
    }

    private boolean hasSpaceForItem(Collection<ItemStack> drops, Inventory inv) {
        Iterator<ItemStack> iter = drops.iterator();

        if (iter != null && iter.hasNext()) {
            ItemStack item = iter.next();

            for (ItemStack it : inv.getStorageContents()) {
                if (it != null && it.equals(item) && (it.getAmount() < 64))
                    return true;
            }
        }

        return false;
    }
}