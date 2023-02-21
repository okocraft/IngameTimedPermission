package net.okocraft.timedperms.event;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TimedPermissionCountEvent extends TimedPermissionSecondsModifyEvent {

    private static final HandlerList handlers = new HandlerList();

    public TimedPermissionCountEvent(UUID userUid, String permission, Map<String, Set<String>> contexts,
                                     int previousSeconds, int newSeconds) {
        super(userUid, permission, contexts, previousSeconds, newSeconds);
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
