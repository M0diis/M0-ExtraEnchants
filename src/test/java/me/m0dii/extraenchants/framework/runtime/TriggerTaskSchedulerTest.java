package me.m0dii.extraenchants.framework.runtime;

import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.m0dii.extraenchants.ExtraEnchants;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

class TriggerTaskSchedulerTest {
    @Test
    void schedulesAtEffectLocationForFolia() {
        ExtraEnchants plugin = Mockito.mock(ExtraEnchants.class);
        PlatformScheduler scheduler = Mockito.mock(PlatformScheduler.class);
        World world = Mockito.mock(World.class);
        WrappedTask task = Mockito.mock(WrappedTask.class);
        Mockito.when(plugin.getScheduler()).thenReturn(scheduler);
        Mockito.when(scheduler.runAtLocationLater(any(), any(Runnable.class), anyLong())).thenReturn(task);

        new TriggerTaskScheduler(plugin).schedule(new Location(world, 1, 2, 3), () -> { }, 4);

        verify(scheduler).runAtLocationLater(any(), any(Runnable.class), Mockito.eq(4L));
        Mockito.verify(scheduler, Mockito.never()).runLater(any(Runnable.class), anyLong());
    }
}
