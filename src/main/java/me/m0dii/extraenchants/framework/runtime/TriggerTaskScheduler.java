package me.m0dii.extraenchants.framework.runtime;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.m0dii.extraenchants.ExtraEnchants;
import org.bukkit.Location;

/**
 * The only scheduling boundary used by config-driven effects.
 *
 * A location-aware task is required on Folia so an effect runs on the region
 * that owns the event location. The global scheduler is used only when there
 * is no usable location.
 */
public final class TriggerTaskScheduler {
    private final ExtraEnchants plugin;

    public TriggerTaskScheduler(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    public WrappedTask schedule(Location location, Runnable task, long delayTicks) {
        long delay = Math.max(0L, delayTicks);
        if (location != null && location.getWorld() != null) {
            return plugin.getScheduler().runAtLocationLater(location.clone(), task, delay);
        }

        return plugin.getScheduler().runLater(task, delay);
    }

    public void cancelAll() {
        plugin.getScheduler().cancelAllTasks();
    }
}
