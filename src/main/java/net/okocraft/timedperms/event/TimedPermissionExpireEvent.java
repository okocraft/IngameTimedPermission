package net.okocraft.timedperms.event;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TimedPermissionExpireEvent extends TimedPermissionRemovedEvent {

    private static final HandlerList handlers = new HandlerList();

    public TimedPermissionExpireEvent(UUID userUid, String permission, Map<String, Set<String>> contexts, int seconds) {
        super(userUid, permission, contexts, seconds);
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
