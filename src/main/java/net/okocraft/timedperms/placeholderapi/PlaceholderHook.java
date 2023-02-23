package net.okocraft.timedperms.placeholderapi;

import me.clip.placeholderapi.PlaceholderAPI;
import net.okocraft.timedperms.Main;
import org.bukkit.OfflinePlayer;

public class PlaceholderHook {

    private final Placeholder placeholder;

    public PlaceholderHook(Main plugin) {
        this.placeholder = new Placeholder(plugin);
    }

    public void register() {
        placeholder.register();
    }

    public void unregister() {
        placeholder.unregister();
    }

    public String setPlaceholder(OfflinePlayer context, String original) {
        return PlaceholderAPI.setPlaceholders(context, original);
    }
}
