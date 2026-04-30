package me.m0dii.extraenchants.framework.bridge;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.framework.runtime.CustomEnchantFramework;
import me.m0dii.extraenchants.framework.runtime.TriggerType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TriggerBridgeListener implements Listener {
    private final ExtraEnchants plugin;
    private final CustomEnchantFramework framework;
    private final Map<String, Long> debounce = new HashMap<>();

    public TriggerBridgeListener(ExtraEnchants plugin, CustomEnchantFramework framework) {
        this.plugin = plugin;
        this.framework = framework;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        LivingEntity victim = event.getEntity() instanceof LivingEntity living ? living : null;
        framework.executeForItem(
                TriggerType.ON_ATTACK,
                event,
                attacker,
                attacker,
                victim,
                attacker.getLocation(),
                attacker.getInventory().getItemInMainHand()
        );

        if (event.getEntity() instanceof Player damagedPlayer) {
            executeArmorTrigger(TriggerType.ON_DAMAGED, event, damagedPlayer, attacker, damagedPlayer, damagedPlayer.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        executeHandTrigger(TriggerType.ON_BLOCK_BREAK, event, player, player, null, event.getBlock().getLocation());
        executeHandTrigger(TriggerType.ON_MINE, event, player, player, null, event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.getKiller() instanceof Player killer)) {
            return;
        }

        executeHandTrigger(TriggerType.ON_KILL, event, killer, killer, victim, victim.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.getHealth() - event.getFinalDamage() > 0D) {
            return;
        }

        LivingEntity attacker = event instanceof EntityDamageByEntityEvent byEntity && byEntity.getDamager() instanceof LivingEntity living
                ? living
                : null;
        executeArmorTrigger(TriggerType.ON_DEATH, event, player, attacker, player, player.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        executeHandTrigger(TriggerType.ON_INTERACT, event, player, player, null, player.getLocation());

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            executeHandTrigger(TriggerType.ON_RIGHT_CLICK, event, player, player, null, player.getLocation());
        }

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            executeHandTrigger(TriggerType.ON_LEFT_CLICK, event, player, player, null, player.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        executeHandTrigger(TriggerType.ON_CONSUME, event, player, player, null, player.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Projectile projectile) || !(projectile.getShooter() instanceof Player player)) {
            return;
        }

        executeHandTrigger(TriggerType.ON_SHOOT, event, player, player, null, projectile.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        LivingEntity victim = event.getHitEntity() instanceof LivingEntity living ? living : null;
        executeHandTrigger(TriggerType.ON_PROJECTILE_HIT, event, player, player, victim, event.getEntity().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        LivingEntity victim = event.getCaught() instanceof LivingEntity living ? living : null;
        executeHandTrigger(TriggerType.ON_FISH, event, player, player, victim, player.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        executeHandTrigger(TriggerType.ON_CHAT, event, player, player, null, player.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        if (!debounced(player.getUniqueId(), TriggerType.ON_MOVE, 250L)) {
            return;
        }

        executeHandTrigger(TriggerType.ON_MOVE, event, player, player, null, to);

        if (to.getY() - from.getY() > 0.41D && debounced(player.getUniqueId(), TriggerType.ON_JUMP, 400L)) {
            executeHandTrigger(TriggerType.ON_JUMP, event, player, player, null, to);
        }

        if (from.getY() - to.getY() > 1.5D && debounced(player.getUniqueId(), TriggerType.ON_FALL, 400L)) {
            executeHandTrigger(TriggerType.ON_FALL, event, player, player, null, to);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) {
            return;
        }

        Player player = event.getPlayer();
        executeHandTrigger(TriggerType.ON_SNEAK, event, player, player, null, player.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSprint(PlayerToggleSprintEvent event) {
        if (!event.isSprinting()) {
            return;
        }

        Player player = event.getPlayer();
        executeHandTrigger(TriggerType.ON_SPRINT, event, player, player, null, player.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        executeHandTrigger(TriggerType.ON_UNEQUIP, event, player, player, null, player.getLocation());

        Bukkit.getScheduler().runTask(plugin, () ->
                executeHandTrigger(TriggerType.ON_EQUIP, event, player, player, null, player.getLocation()));
    }

    private void executeHandTrigger(
            TriggerType triggerType,
            org.bukkit.event.Event event,
            Player owner,
            LivingEntity attacker,
            LivingEntity victim,
            Location location
    ) {
        framework.executeForItem(
                triggerType,
                event,
                owner,
                attacker,
                victim,
                location,
                owner.getInventory().getItemInMainHand()
        );
    }

    private void executeArmorTrigger(
            TriggerType triggerType,
            org.bukkit.event.Event event,
            Player owner,
            LivingEntity attacker,
            LivingEntity victim,
            Location location
    ) {
        framework.executeForArmor(triggerType, event, owner, attacker, victim, location);
    }

    private boolean debounced(UUID playerId, TriggerType triggerType, long intervalMillis) {
        long now = System.currentTimeMillis();
        String key = triggerType.name() + ":" + playerId;
        Long last = debounce.get(key);
        if (last != null && now - last < intervalMillis) {
            return false;
        }

        debounce.put(key, now);
        return true;
    }
}





