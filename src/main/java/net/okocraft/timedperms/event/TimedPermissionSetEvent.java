package net.okocraft.timedperms.event;

import java.util.UUID;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TimedPermissionSetEvent extends TimedPermissionModifyEvent {

    private static final HandlerList handlers = new HandlerList();

    public TimedPermissionSetEvent(UUID userUid, PermissionNode permission, int previousSeconds, int newSeconds) {
        super(userUid, permission, previousSeconds, newSeconds);
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
