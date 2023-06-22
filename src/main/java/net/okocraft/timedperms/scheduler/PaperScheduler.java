package net.okocraft.timedperms.scheduler;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

final class PaperScheduler implements Scheduler {

    private final Plugin plugin;

    PaperScheduler(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runOnGlobalScheduler(@NotNull Runnable task) {
        plugin.getServer().getGlobalRegionScheduler().run(plugin, $ -> task.run());
    }

    @Override
    public void scheduleDelayedTask(@NotNull Runnable task, long delay, @NotNull TimeUnit timeUnit) {
        plugin.getServer().getAsyncScheduler().runDelayed(plugin, $ -> task.run(), delay, timeUnit);
    }

    @Override
    public void scheduleRepeatingTask(@NotNull Runnable task, long interval, @NotNull TimeUnit timeUnit) {
        plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, $ -> task.run(), interval, interval, timeUnit);
    }

    @Override
    public void shutdown() {
        plugin.getServer().getGlobalRegionScheduler().cancelTasks(plugin);
        plugin.getServer().getAsyncScheduler().cancelTasks(plugin);
    }
}
