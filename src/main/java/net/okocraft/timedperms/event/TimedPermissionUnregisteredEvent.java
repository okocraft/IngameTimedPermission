package net.okocraft.timedperms.event;

import java.util.UUID;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TimedPermissionUnregisteredEvent extends TimedPermissionEvent {

    private static final HandlerList handlers = new HandlerList();

    private final int lastSeconds;
    private final TimedPermissionEvent cause;

    public TimedPermissionUnregisteredEvent(UUID userUid, PermissionNode permission, int lastSeconds, TimedPermissionEvent cause) {
        super(userUid, permission);
        this.lastSeconds = lastSeconds;
        this.cause = cause;
    }

    public int getLastSeconds() {
        return this.lastSeconds;
    }

    public TimedPermissionEvent getCause() {
        return this.cause;
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
