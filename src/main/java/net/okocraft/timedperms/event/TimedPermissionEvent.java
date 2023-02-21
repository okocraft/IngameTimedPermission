package net.okocraft.timedperms.event;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class TimedPermissionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final UUID userUid;
    private final String permission;
    private final Map<String, Set<String>> contexts;

    public TimedPermissionEvent(UUID userUid, String permission, Map<String, Set<String>> contexts) {
        super(!Bukkit.isPrimaryThread());
        this.userUid = userUid;
        this.permission = permission;
        this.contexts = contexts;
    }

    public UUID getUserUid() {
        return this.userUid;
    }

    public String getPermission() {
        return this.permission;
    }

    public Map<String, Set<String>> getContexts() {
        return Collections.unmodifiableMap(this.contexts);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
