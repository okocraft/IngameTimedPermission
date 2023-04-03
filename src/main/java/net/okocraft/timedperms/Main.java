package net.okocraft.timedperms;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.okocraft.timedperms.command.TimedPermsCommand;
import net.okocraft.timedperms.language.TranslationManager;
import net.okocraft.timedperms.listener.PlayerListener;
import net.okocraft.timedperms.model.LocalPlayer;
import net.okocraft.timedperms.model.LocalPlayerFactory;
import net.okocraft.timedperms.placeholderapi.PlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    private static final boolean FOLIA;

    static {
        boolean isFolia;
        try {
            Bukkit.class.getDeclaredMethod("getGlobalRegionScheduler");
            isFolia = true;
        } catch (NoSuchMethodException e) {
            isFolia = false;
        }

        FOLIA = isFolia;
    }

    private final YamlConfiguration configuration = YamlConfiguration.create(getDataFolder().toPath().resolve("config.yml"));
    private final TranslationManager translationManager = new TranslationManager(
            getName(), getDescription().getVersion(), getJarPath(), getDataFolder().toPath());
    private final PlayerListener playerListener = new PlayerListener();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final TimedPermsCommand commandHandler = new TimedPermsCommand(this);
    private PlaceholderHook placeholderHook;

    @Override
    public void onLoad() {
        translationManager.load();
    }

    @Override
    public void onEnable() {
        try {
            ResourceUtils.copyFromJarIfNotExists(getJarPath(), "config.yml", configuration.getPath());
            configuration.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load config.yml", e);
            getServer().getPluginManager().disablePlugin(this);
        }

        getServer().getPluginManager().registerEvents(playerListener, this);
        playerListener.subscribeLuckPermsEvents();

        PluginCommand command = Objects.requireNonNull(getCommand("timedperms"));
        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);

        executor.scheduleAtFixedRate(() -> {
            try {
                for (Player p : getServer().getOnlinePlayers()) {
                    LocalPlayerFactory.get(p.getUniqueId()).countOne();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);

        try {
            placeholderHook = new PlaceholderHook(this);
            placeholderHook.register();
            getLogger().info("Using PlaceholderAPI.");
        } catch (NoClassDefFoundError ignored) {
        }
    }

    @Override
    public void onDisable() {
        if (placeholderHook != null) {
            placeholderHook.unregister();
        }

        executor.shutdownNow().forEach(Runnable::run);
        for (Player p : getServer().getOnlinePlayers()) {
            LocalPlayerFactory.get(p.getUniqueId()).saveAndClose();
        }

        playerListener.unsubscribeLuckPermsEvents();
        HandlerList.unregisterAll(playerListener);

        translationManager.unload();
    }

    public YamlConfiguration getConfiguration() {
        return this.configuration;
    }

    public PlaceholderHook getPlaceholderHook() {
        return this.placeholderHook;
    }

    public void runTask(Runnable task) {
        if (FOLIA) {
            getServer().getGlobalRegionScheduler().run(this, $ -> task.run());
        } else {
            getServer().getScheduler().runTask(this, task);
        }
    }

    public ScheduledFuture<?> schedule(Runnable task, int delaySeconds) {
        return this.executor.schedule(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, delaySeconds, TimeUnit.SECONDS);
    }

    public static Path getJarPath() {
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        Path jarFilePath;
        try {
            // for linux.
            jarFilePath = Paths.get(path);
        } catch (InvalidPathException e) {
            // for windows.
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            jarFilePath = Paths.get(path);
        }
        return jarFilePath;
    }
}
