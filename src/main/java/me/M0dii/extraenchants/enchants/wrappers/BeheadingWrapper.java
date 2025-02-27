package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.BeheadingEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@EnchantWrapper(name = "Beheading", maxLevel = 1)
public class BeheadingWrapper extends CustomEnchantment {

    public BeheadingWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isSword(item) || enchant.canEnchantItemCustom(item);
    }

    @Override
    public boolean conflictsWith(final @NotNull Enchantment enchantment) {
        if (enchant.getCustomConflicts().contains(enchantment)) {
            return true;
        }

        if (!enchant.defaultConflictsEnabled()) {
            return false;
        }

        return enchantment.equals(Enchantment.LOOTING);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }

    private static final List<String> heads = List.of(
            "CREEPER_HEAD",
            "WITHER_SKELETON_SKULL",
            "ZOMBIE_HEAD",
            "DRAGON_HEAD"
    );

    @EventHandler
    public void onBehead(final BeheadingEvent e) {
        if (!Utils.shouldTrigger(EEnchant.BEHEADING)) {
            return;
        }

        Player killer = e.getPlayer();

        Entity died = e.getEntityDeathEvent().getEntity();

        ItemStack head;

        switch (died) {
            case Player player -> {
                head = new ItemStack(Material.PLAYER_HEAD);

                ((SkullMeta) head.getItemMeta()).setOwningPlayer((OfflinePlayer) died);

                died.getWorld().dropItemNaturally(died.getLocation(), head);

                return;
            }
            case Skeleton skeleton -> {
                head = new ItemStack(Material.SKELETON_SKULL);

                died.getWorld().dropItemNaturally(died.getLocation(), head);

                return;
            }
            case Sheep sheep -> {
                head = new ItemStack(Material.PLAYER_HEAD);

                ((SkullMeta) head.getItemMeta()).setOwningPlayer((OfflinePlayer) died);

                died.getWorld().dropItemNaturally(died.getLocation(), head);

                return;
            }
            default -> {
            }
        }

        for (String hn : heads) {
            if (died.getName().equalsIgnoreCase(hn)) {
                Material mat = Material.getMaterial(hn);

                if (mat == null) {
                    continue;
                }

                head = new ItemStack(mat);

                died.getWorld().dropItemNaturally(died.getLocation(), head);

                break;
            }
        }
    }
}
