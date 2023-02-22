package net.okocraft.timedperms.event;

import java.util.UUID;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TimedPermissionRemoveEvent extends TimedPermissionModifyEvent {

    private static final HandlerList handlers = new HandlerList();

    public TimedPermissionRemoveEvent(UUID userUid, PermissionNode permission, int seconds) {
        super(userUid, permission, seconds, 0);
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
