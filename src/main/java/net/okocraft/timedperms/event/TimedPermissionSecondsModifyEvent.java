package net.okocraft.timedperms.event;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TimedPermissionSecondsModifyEvent extends TimedPermissionEvent {

    private static final HandlerList handlers = new HandlerList();
    private final int previousSeconds;
    private final int newSeconds;

    public TimedPermissionSecondsModifyEvent(UUID userUid, String permission, Map<String, Set<String>> contexts, int previousSeconds, int newSeconds) {
        super(userUid, permission, contexts);
        this.previousSeconds = previousSeconds;
        this.newSeconds = newSeconds;
    }

    public int getPreviousSeconds() {
        return this.previousSeconds;
    }

    public int getNewSeconds() {
        return this.newSeconds;
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
