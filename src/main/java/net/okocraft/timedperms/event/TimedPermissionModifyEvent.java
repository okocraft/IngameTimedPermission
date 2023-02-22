package net.okocraft.timedperms.event;

import java.util.UUID;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TimedPermissionModifyEvent extends TimedPermissionEvent {

    private static final HandlerList handlers = new HandlerList();
    private final int previousSeconds;
    private final int newSeconds;

    public TimedPermissionModifyEvent(UUID userUid, PermissionNode permission, int previousSeconds, int newSeconds) {
        super(userUid, permission);
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
