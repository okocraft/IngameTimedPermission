package net.okocraft.timedperms.scheduler;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public interface Scheduler {

    static @NotNull Scheduler create(@NotNull Plugin plugin) {
        try {
            plugin.getServer().getClass().getDeclaredMethod("getAsyncScheduler");
            return new PaperScheduler(plugin);
        } catch (NoSuchMethodException e) {
            return new LegacyScheduler(plugin);
        }
    }

    void runOnGlobalScheduler(@NotNull Runnable task);

    void scheduleDelayedTask(@NotNull Runnable task, long delay, @NotNull TimeUnit timeUnit);

    void scheduleRepeatingTask(@NotNull Runnable task, long interval, @NotNull TimeUnit timeUnit);

    void shutdown();
}
