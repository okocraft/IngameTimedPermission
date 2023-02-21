package net.okocraft.timedperms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataLoader implements Listener {

    private final Main plugin;

    private final Map<UUID, Properties> timeData = new HashMap<>();

    PlayerDataLoader(Main plugin) {
        this.plugin = plugin;
    }

    void countOne() {
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            Properties data = timeData.get(player.getUniqueId());
            data.stringPropertyNames().forEach(key -> {
                int seconds = getInt(data, key);
                if (seconds >= 0) {
                    seconds--;
                }
                set0(data, player, key, seconds);
            });
        });
    }

    void add(Player player, String permission, int add) {
        set(player, permission, get(player, permission) + add);
    }

    void remove(Player player, String permission, int remove) {
        set(player, permission, get(player, permission) - remove);
    }

    int get(Player player, String permission) {
        return getInt(timeData.getOrDefault(player.getUniqueId(), load(player.getUniqueId())), permission);
    }

    void set(Player player, String permission, int seconds) {
        if (timeData.containsKey(player.getUniqueId())) {
            Properties data = timeData.get(player.getUniqueId());
            set0(data, player, permission, seconds);
        } else {
            Properties data = load(player.getUniqueId());
            set0(data, player, permission, seconds);
            save(player.getUniqueId(), data);
        }
    }

    private void set0(Properties data, Player player, String permission, int seconds) {
        if (seconds < 0) {
            data.remove(permission);
            if (player.isPermissionSet(permission)) {
                setPermission(player.getUniqueId().toString(), permission, false);
            }
        } else {
            data.put(permission, String.valueOf(seconds));
            if (!player.isPermissionSet(permission)) {
                setPermission(player.getUniqueId().toString(), permission, true);
            }
        }
    }

    void loadAll() {
        plugin.getServer().getOnlinePlayers().stream()
                .map(Entity::getUniqueId)
                .forEach(u -> timeData.put(u, load(u)));
    }

    void saveAll() {
        timeData.forEach(this::save);
    }

    private Path getDataFilePath(UUID uuid) {
        return plugin.getDataFolder().toPath()
                .resolve("data").resolve(uuid + ".properties");
    }

    private Properties load(UUID player) {
        Path dataFilePath = getDataFilePath(player);
        try {
            if (!Files.exists(dataFilePath)) {
                Files.createDirectories(dataFilePath.getParent());
                Files.createFile(dataFilePath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (var in = Files.newInputStream(dataFilePath)) {
            Properties data = new Properties();
            data.load(in);
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void save(UUID player, Properties data) {
        try (var out = Files.newOutputStream(getDataFilePath(player))) {
            data.store(out, "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        timeData.put(event.getPlayer().getUniqueId(), load(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        save(event.getPlayer().getUniqueId(), timeData.remove(event.getPlayer().getUniqueId()));
    }

    private void setPermission(String user, String perm, boolean set) {
        // TODO: LUCKPERM REQUIRED. 全権の人のispermissionsetが常にtrueだからlpでなんとかする
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().dispatchCommand(
                plugin.getServer().getConsoleSender(),
                "lp user " + user + " permission " + (set ? "set" : "unset") + " " + perm
        ));
    }

    private static int getInt(Properties p, String key) {
        Object v = p.get(key);
        if (v instanceof String) {
            return Integer.parseInt((String) v);
        } else if (v instanceof Integer) {
            return (Integer) v;
        } else {
            return -1;
        }
    }
}
