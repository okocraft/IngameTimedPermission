package net.okocraft.timedperms.placeholderapi;

import net.okocraft.timedperms.Main;

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
}
