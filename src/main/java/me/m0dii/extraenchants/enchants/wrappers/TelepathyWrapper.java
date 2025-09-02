package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.events.TelepathyEvent;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@EnchantWrapper(name = "Telepathy", maxLevel = 1)
public class TelepathyWrapper extends CustomEnchantment {

    public TelepathyWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isTool(item, true) || enchant.canEnchantItemCustom(item);
    }

    @Override
    public boolean conflictsWith(final @NotNull Enchantment enchantment) {
        if (enchant.getCustomConflicts().contains(enchantment)) {
            return true;
        }

        if (!enchant.defaultConflictsEnabled()) {
            return false;
        }

        return Enchantment.SILK_TOUCH.equals(enchantment) || EEnchant.SMELT.equals(enchantment);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }


    @EventHandler
    public void onTelepathy(final TelepathyEvent e) {
        if (!Utils.shouldTrigger(EEnchant.TELEPATHY)) {
            return;
        }

        ItemStack tool = e.getTool();

        if(tool.getItemMeta() == null) {
            return;
        }

        Player player = e.getPlayer();
        Block b = e.getBlock();
        PlayerInventory inv = player.getInventory();

        Collection<ItemStack> drops = e.getDrops();

        boolean hasSilk = tool.getItemMeta()
                .getEnchants().containsKey(Enchantment.SILK_TOUCH);

        boolean fits = doesFit(inv, drops);

        if (b.getType().equals(Material.SPAWNER)
                || b.getType().name().toUpperCase().contains("SPAWNER"))
            return;

        if (!fits) {
            for (ItemStack i : drops)
                b.getWorld().dropItemNaturally(
                        b.getLocation(), i);

            InventoryUtils.applyDurability(player, tool);

            return;
        }

        if (hasSilk) {
            ItemStack silkItem;

            if (b.getType().equals(Material.REDSTONE_WIRE)) {
                silkItem = new ItemStack(Material.REDSTONE);
            } else {
                silkItem = new ItemStack(b.getType());
            }

            String name = silkItem.getType().name();

            if (name.contains("WALL_") || name.contains("BANNER")) {
                if (name.contains("BANNER")) {
                    for (ItemStack i : drops) {
                        inv.addItem(i);
                    }
                } else {
                    Material m = Material.getMaterial(name.replace("WALL_", ""));

                    if (m != null) {
                        inv.addItem(new ItemStack(m));
                    }
                }
            } else {
                if (inv.firstEmpty() == -1) {
                    if (inv.contains(silkItem)) {
                        addToStack(player, List.of(silkItem));
                    }
                } else {
                    inv.addItem(silkItem);
                }
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

        InventoryUtils.applyDurability(player, tool);
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
