package net.okocraft.timedperms.event;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TimedPermissionRegisteredEvent extends TimedPermissionEvent {

    private static final HandlerList handlers = new HandlerList();

    private final int initialSeconds;

    public TimedPermissionRegisteredEvent(UUID userUid, String permission, Map<String, Set<String>> contexts, int initialSeconds) {
        super(userUid, permission, contexts);
        this.initialSeconds = initialSeconds;
    }

    public int getInitialSeconds() {
        return this.initialSeconds;
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
