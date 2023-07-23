package net.okocraft.timedperms.scheduler;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

final class LegacyScheduler implements Scheduler {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Plugin plugin;

    LegacyScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runOnGlobalScheduler(@NotNull Runnable task) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    @Override
    public void scheduleDelayedTask(@NotNull Runnable task, long delay, @NotNull TimeUnit timeUnit) {
        scheduler.schedule(task, delay, timeUnit);
    }

    @Override
    public void scheduleRepeatingTask(@NotNull Runnable task, long interval, @NotNull TimeUnit timeUnit) {
        scheduler.scheduleAtFixedRate(task, interval, interval, timeUnit);
    }

    @Override
    public void shutdown() {
        scheduler.shutdownNow().forEach(Runnable::run);
    }
}
