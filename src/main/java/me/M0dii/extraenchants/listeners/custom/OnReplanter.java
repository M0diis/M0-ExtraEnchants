package me.m0dii.extraenchants.listeners.custom;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.google.common.collect.Sets;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.PlowEvent;
import me.m0dii.extraenchants.events.ReplanterEvent;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.Set;

public class OnReplanter implements Listener {
    private static final Random rnd = new Random();
    private static final Set<Material> CROPS = Sets.newHashSet(
            Material.WHEAT_SEEDS,
            Material.BEETROOT_SEEDS,
            Material.MELON_SEEDS,
            Material.PUMPKIN_SEEDS,
            Material.POTATO,
            Material.CARROT,
            Material.NETHER_WART);

    private final Residence res;
    private ResidenceManager rm;
    private final ExtraEnchants plugin;

    public OnReplanter(ExtraEnchants plugin) {
        this.plugin = plugin;

        this.res = Residence.getInstance();

        if (res != null) {
            this.rm = res.getResidenceManager();
        }
    }

    @EventHandler
    public void onReplanter(final ReplanterEvent e) {
        Messenger.debug("Replanter event called.");

        if (!Utils.shouldTrigger(EEnchant.REPLANTER)) {
            return;
        }

        Block blockPlant = e.getInteractEvent().getClickedBlock();

        if(blockPlant == null) {
            return;
        }

        Messenger.debug("Block is not null.");

        if (!CROPS.contains(this.fineBlockToSeeds(blockPlant.getType()))) {
            return;
        }

        Messenger.debug("Block is a crop.");

        BlockData dataPlant = blockPlant.getBlockData();

        if (!(dataPlant instanceof Ageable plant)) {
            return;
        }

        Messenger.debug("Block is ageable.");

        Player player = e.getPlayer();

        if(!allowed(player, blockPlant.getLocation())) {
            return;
        }

        Messenger.debug("Player is allowed to replant.");

        ItemStack hand = player.getInventory().getItemInMainHand();

        if (takeSeeds(player, plant.getMaterial())) {
            blockPlant.breakNaturally(hand);

            blockPlant.setType(plant.getMaterial());
            plant.setAge(0);
            blockPlant.setBlockData(plant);

            InventoryUtils.applyDurability(hand);
        }
    }

    private boolean allowed(Player p, Location loc) {
        if (res == null) {
            return true;
        }

        ClaimedResidence residence = rm.getByLoc(loc);

        if (residence == null) {
            return true;
        }

        ResidencePermissions perms = residence.getPermissions();

        return perms.playerHas(p, Flags.build, true)
            || residence.isOwner(p)
            || residence.isTrusted(p)
            || res.isResAdminOn(p);
    }

    @NotNull
    private Material fineSeedsToBlock(@NotNull Material material) {
        return switch (material) {
            case POTATO -> Material.POTATOES;
            case CARROT -> Material.CARROTS;
            case BEETROOT_SEEDS -> Material.BEETROOTS;
            case WHEAT_SEEDS -> Material.WHEAT;
            case PUMPKIN_SEEDS -> Material.PUMPKIN_STEM;
            case MELON_SEEDS -> Material.MELON_STEM;
            default -> material;
        };
    }

    @NotNull
    private Material fineBlockToSeeds(@NotNull Material material) {
        return switch (material) {
            case POTATOES -> Material.POTATO;
            case CARROTS -> Material.CARROT;
            case BEETROOTS -> Material.BEETROOT_SEEDS;
            case WHEAT -> Material.WHEAT_SEEDS;
            case MELON_STEM -> Material.MELON_SEEDS;
            case PUMPKIN_STEM -> Material.PUMPKIN_SEEDS;
            default -> material;
        };
    }

    private boolean takeSeeds(@NotNull Player player, @NotNull Material material) {
        material = fineBlockToSeeds(material);

        int slot = player.getInventory().first(material);

        if (slot < 0) {
            return false;
        }

        ItemStack seed = player.getInventory().getItem(slot);

        if (seed == null || seed.getType().isAir()) {
            return false;
        }

        seed.setAmount(seed.getAmount() - 1);

        return true;
    }
}