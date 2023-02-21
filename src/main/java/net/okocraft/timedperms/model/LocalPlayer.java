package net.okocraft.timedperms.model;

import java.io.Closeable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.IntBinaryOperator;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.util.Tristate;
import net.okocraft.timedperms.event.TimedPermissionCountEvent;
import net.okocraft.timedperms.event.TimedPermissionExpireEvent;
import net.okocraft.timedperms.event.TimedPermissionRegisteredEvent;
import net.okocraft.timedperms.event.TimedPermissionRemovedEvent;
import net.okocraft.timedperms.event.TimedPermissionSecondsModifyEvent;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;

public class LocalPlayer implements Closeable {

    private final UUID uniqueId;
    private final Map<PermissionNode, Integer> timedPermissions;

    private boolean closed = false;

    LocalPlayer(UUID uniqueId, Map<PermissionNode, Integer> timedPermissions) {
        this.uniqueId = uniqueId;
        this.timedPermissions = timedPermissions;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    Map<PermissionNode, Integer> getData() {
        if (closed) {
            return Map.of();
        }
        return Collections.unmodifiableMap(timedPermissions);
    }

    public void countOne() {
        if (!closed) {
            timedPermissions.replaceAll(newSecondsCalculator(1, (seconds, delta) -> seconds - delta));
        }
    }

    public int getSeconds(PermissionNode permission) {
        return closed ? -1 : timedPermissions.getOrDefault(permission, -1);
    }

    public int addSeconds(PermissionNode permission, int delta) {
        return operateSeconds(permission, delta, Integer::sum);
    }

    public int removeSeconds(PermissionNode permission, int delta) {
        return operateSeconds(permission, delta, (seconds, delta_) -> seconds - delta_);
    }

    public int setSeconds(PermissionNode permission, int seconds) {
        return operateSeconds(permission, seconds, (oldValue, newValue) -> newValue);
    }

    private int operateSeconds(PermissionNode permission, int delta, IntBinaryOperator operator) {
        if (closed) {
            return -1;
        }
        Integer ret = timedPermissions.compute(permission, newSecondsCalculator(delta, operator));
        return ret == null ? -1 : ret;
    }

    private BiFunction<PermissionNode, Integer, Integer> newSecondsCalculator(int delta, IntBinaryOperator operator) {
        return (permission, seconds) -> {
            if (seconds == null) {
                seconds = 0;
            }

            int newSeconds = operator.applyAsInt(seconds, delta);
            if (newSeconds <= 0) {
                if (seconds <= 0) {
                    return null;
                }
                if (isPermissionSet(permission)) {
                    setPermission(permission, Tristate.UNDEFINED);
                }
                if (seconds - 1 == newSeconds) {
                    Bukkit.getPluginManager().callEvent(new TimedPermissionExpireEvent(
                            uniqueId,
                            permission.getPermission(),
                            permission.getContexts().toMap(),
                            seconds));
                } else {
                    Bukkit.getPluginManager().callEvent(new TimedPermissionRemovedEvent(
                            uniqueId,
                            permission.getPermission(),
                            permission.getContexts().toMap(),
                            seconds));
                }
                return null;
            } else {
                if (seconds == 0) {
                    Bukkit.getPluginManager().callEvent(new TimedPermissionRegisteredEvent(
                            uniqueId,
                            permission.getPermission(),
                            permission.getContexts().toMap(),
                            newSeconds));
                } else if (seconds - 1 == newSeconds) {
                    Bukkit.getPluginManager().callEvent(new TimedPermissionCountEvent(
                            uniqueId,
                            permission.getPermission(),
                            permission.getContexts().toMap(),
                            seconds,
                            newSeconds));
                } else {
                    Bukkit.getPluginManager().callEvent(new TimedPermissionSecondsModifyEvent(
                            uniqueId,
                            permission.getPermission(),
                            permission.getContexts().toMap(),
                            seconds,
                            newSeconds));
                }
                if (!isPermissionSet(permission)) {
                    setPermission(permission, Tristate.TRUE);
                }
                return newSeconds;
            }
        };
    }

    public boolean isPermissionSet(PermissionNode permission) {
        return !closed && getUser().data().contains(permission, Node::equals) != Tristate.UNDEFINED;
    }

    public void setPermission(PermissionNode permission, Tristate state) {
        if (!closed) {
            if (state == Tristate.UNDEFINED) {
                getUser().data().remove(permission);
            } else {
                getUser().data().add(permission.toBuilder().value(state.asBoolean()).build());
            }
        }
    }

    public void onPermissionRemoved(Set<PermissionNode> removed) {
        timedPermissions.keySet().removeAll(removed);
    }

    private User getUser() {
        UserManager userManager = LuckPermsProvider.get().getUserManager();
        User user = userManager.getUser(uniqueId);
        if (user != null) {
            return user;
        }

        try {
            return userManager.loadUser(uniqueId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @ApiStatus.Internal
    public void saveAndClose() {
        save();
        LocalPlayerFactory.close(this);
        close();
    }

    public void save() {
        if (closed) {
            return;
        }
        LocalPlayerDataSerializer.saveToFile(this);
    }

    @ApiStatus.Internal
    @Override
    public void close() {
        closed = true;
        timedPermissions.clear();
    }

    public boolean isClosed() {
        return this.closed;
    }
}
