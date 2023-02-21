package net.okocraft.timedperms.event;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TimedPermissionRemovedEvent extends TimedPermissionEvent {

    private static final HandlerList handlers = new HandlerList();

    private final int seconds;

    public TimedPermissionRemovedEvent(UUID userUid, String permission, Map<String, Set<String>> contexts, int seconds) {
        super(userUid, permission, contexts);
        this.seconds = seconds;
    }

    public int getSeconds() {
        return this.seconds;
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
