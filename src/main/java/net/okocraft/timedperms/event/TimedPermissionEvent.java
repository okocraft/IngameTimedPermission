package net.okocraft.timedperms.event;

import java.util.UUID;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class TimedPermissionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final UUID userUid;
    private final PermissionNode permission;

    public TimedPermissionEvent(UUID userUid, PermissionNode permission) {
        super(!Bukkit.isPrimaryThread());
        this.userUid = userUid;
        this.permission = permission;
    }

    public UUID getUserUid() {
        return this.userUid;
    }

    public PermissionNode getPermission() {
        return this.permission;
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
