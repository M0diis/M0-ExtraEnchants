package me.m0dii.extraenchants.enchants.wrappers;

import com.google.common.collect.Sets;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.events.ReplanterBreakEvent;
import me.m0dii.extraenchants.events.ReplanterEvent;
import me.m0dii.extraenchants.events.TelepathyEvent;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@EnchantWrapper(name = "Replanter", maxLevel = 1)
public class ReplanterWrapper extends CustomEnchantment {

    public ReplanterWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);

        init();
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isHoe(item) || enchant.canEnchantItemCustom(item);
    }

    @Override
    public boolean conflictsWith(final @NotNull Enchantment enchantment) {
        if (enchant.getCustomConflicts().contains(enchantment)) {
            return true;
        }

        if (!enchant.defaultConflictsEnabled()) {
            return false;
        }

        return enchantment.equals(Enchantment.SILK_TOUCH) || enchantment.equals(EEnchant.PLOW.getEnchantment());
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }

    private static final Set<Material> CROPS = Sets.newHashSet(
            Material.WHEAT_SEEDS,
            Material.BEETROOT_SEEDS,
            Material.MELON_SEEDS,
            Material.PUMPKIN_SEEDS,
            Material.POTATO,
            Material.CARROT,
            Material.NETHER_WART
    );

    private void init() {
        Bukkit.getScheduler().runTaskTimer(ExtraEnchants.getInstance(), () -> {
            List<ReplantLocation> toReplant = pendingToReplant.stream()
                    .filter(replant -> System.currentTimeMillis() - replant.getTime() >= 3000)
                    .toList();

            for (ReplantLocation replant : toReplant) {
                replant.getBlock().setType(replant.getPlant().getMaterial());
                replant.getPlant().setAge(0);
                replant.getBlock().setBlockData(replant.getPlant());
            }

            pendingToReplant.removeAll(toReplant);
        }, 20L, 20L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onReplanter(final ReplanterEvent e) {
        Messenger.debug("ReplanterEvent called.");

        if (!Utils.shouldTrigger(EEnchant.REPLANTER)) {
            return;
        }

        Block blockPlant = e.getPlayerInteractEvent().getClickedBlock();

        if (blockPlant == null) {
            return;
        }

        if (!CROPS.contains(this.fineBlockToSeeds(blockPlant.getType()))) {
            return;
        }

        BlockData dataPlant = blockPlant.getBlockData();

        if (!(dataPlant instanceof Ageable plant)) {
            return;
        }

        if (plant.getAge() != plant.getMaximumAge()) {
            return;
        }

        Player player = e.getPlayer();

        if (!Utils.allowedAt(player, blockPlant.getLocation())) {
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();

        if (takeSeeds(player, plant.getMaterial())) {
            if (InventoryUtils.hasEnchant(hand, EEnchant.TELEPATHY)) {
                Bukkit.getPluginManager().callEvent(new TelepathyEvent(e.getPlayer(), hand, e.getPlayerInteractEvent(), blockPlant.getDrops(hand)));
            } else {
                blockPlant.breakNaturally(hand);
            }

            blockPlant.setType(plant.getMaterial());
            plant.setAge(0);
            blockPlant.setBlockData(plant);

            InventoryUtils.applyDurability(e.getPlayer(), hand);
        }
    }

    @EventHandler
    public void onPlayerInteractReplanter(final PlayerInteractEvent e) {
        if (EEnchant.REPLANTER.isDisabled()) {
            return;
        }

        if (e.isCancelled()) {
            return;
        }

        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();

        if (hand.getType().isAir()) {
            return;
        }

        if (hand.getItemMeta() == null) {
            return;
        }

        if (!hand.getItemMeta().hasEnchant(EEnchant.REPLANTER.getEnchantment())) {
            return;
        }

        if (!EnchantableItemTypeUtil.isHoe(hand)) {
            return;
        }

        Player p = e.getPlayer();

        if ((p.getGameMode() == GameMode.CREATIVE)
                || (p.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new ReplanterEvent(p, e));
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

    class ReplantLocation {
        private final Block block;
        private final Ageable plant;
        private final long time = System.currentTimeMillis();

        public ReplantLocation(Block block, Ageable plant) {
            this.block = block;
            this.plant = plant;
        }

        public Block getBlock() {
            return this.block;
        }

        public Ageable getPlant() {
            return this.plant;
        }

        public long getTime() {
            return this.time;
        }
    }

    private final List<ReplantLocation> pendingToReplant = new ArrayList<>();

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final ReplanterBreakEvent e) {
        if (!Utils.shouldTrigger(EEnchant.REPLANTER)) {
            return;
        }

        if (e.getBlockBreakEvent().isCancelled()) {
            return;
        }

        if (e.getEnchantLevel() <= 0) {
            return;
        }

        Block block = e.getBlockBreakEvent().getBlock();

        if (!CROPS.contains(this.fineBlockToSeeds(block.getType()))) {
            return;
        }

        BlockData data = block.getBlockData();

        if (!(data instanceof Ageable plant)) {
            return;
        }

        if (plant.getAge() != plant.getMaximumAge()) {
            e.getBlockBreakEvent().setCancelled(true);
            return;
        }

        Player player = e.getPlayer();

        if (!Utils.allowedAt(player, block.getLocation())) {
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();

        if (takeSeeds(player, plant.getMaterial())) {
            if (InventoryUtils.hasEnchant(hand, EEnchant.TELEPATHY)) {
                Bukkit.getPluginManager().callEvent(new TelepathyEvent(e.getPlayer(), hand, e.getBlockBreakEvent(), block.getDrops(hand)));
                block.setType(Material.AIR);
            } else {
                block.breakNaturally(hand);
            }

            pendingToReplant.add(new ReplantLocation(block, plant));

            InventoryUtils.applyDurability(e.getPlayer(), hand);
        }
    }
}
