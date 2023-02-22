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
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.util.Tristate;
import net.okocraft.timedperms.event.TimedPermissionAddEvent;
import net.okocraft.timedperms.event.TimedPermissionCountEvent;
import net.okocraft.timedperms.event.TimedPermissionEvent;
import net.okocraft.timedperms.event.TimedPermissionExpireEvent;
import net.okocraft.timedperms.event.TimedPermissionRegisteredEvent;
import net.okocraft.timedperms.event.TimedPermissionRemoveEvent;
import net.okocraft.timedperms.event.TimedPermissionSetEvent;
import net.okocraft.timedperms.event.TimedPermissionUnregisteredEvent;
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
            timedPermissions.replaceAll(newSecondsCalculator(1, OperationType.COUNT));
        }
    }

    public int getSeconds(PermissionNode permission) {
        return closed ? -1 : timedPermissions.getOrDefault(permission, -1);
    }

    public int addSeconds(PermissionNode permission, int delta) {
        return operateSeconds(permission, delta, OperationType.ADD);
    }

    public int removeSeconds(PermissionNode permission, int delta) {
        return operateSeconds(permission, delta, OperationType.REMOVE);
    }

    public int setSeconds(PermissionNode permission, int seconds) {
        return operateSeconds(permission, seconds, OperationType.SET);
    }

    private int operateSeconds(PermissionNode permission, int delta, OperationType operationType) {
        if (closed) {
            return -1;
        }
        Integer ret = timedPermissions.compute(permission, newSecondsCalculator(delta, operationType));
        return ret == null ? -1 : ret;
    }

    private BiFunction<PermissionNode, Integer, Integer> newSecondsCalculator(int delta, OperationType operationType) {
        return (permission, seconds) -> {
            int newSeconds =  operationType.getOperator().applyAsInt(seconds == null ? 0 : seconds, delta);
            boolean unregister = newSeconds <= 0;
            if (seconds == null && unregister) {
                return null;
            }

            TimedPermissionEvent event = operationType.createEvent(uniqueId, permission, seconds == null ? 0 : seconds, newSeconds);
            Bukkit.getPluginManager().callEvent(event);
            if (unregister) {
                if (operationType == OperationType.COUNT) {
                    event = new TimedPermissionExpireEvent(uniqueId, permission);
                    Bukkit.getPluginManager().callEvent(event);
                }
                Bukkit.getPluginManager().callEvent(
                        new TimedPermissionUnregisteredEvent(uniqueId, permission, newSeconds, event)
                );
            } else if (seconds == null) {
                Bukkit.getPluginManager().callEvent(
                        new TimedPermissionRegisteredEvent(uniqueId, permission, newSeconds, event)
                );
            }

            checkPermission(permission, !unregister);
            return unregister ? null : newSeconds;
        };
    }

    private void checkPermission(PermissionNode permission, boolean shouldPermissionTrue) {
        NodeMap data = getUser().data();
        boolean permissionUndefined = data.contains(permission, Node::equals) == Tristate.UNDEFINED;

        if (permissionUndefined && shouldPermissionTrue) {
            data.add(permission.toBuilder().value(true).build());
        } else if (!permissionUndefined && !shouldPermissionTrue) {
            data.remove(permission);
        }
    }

    @ApiStatus.Internal
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

    // todo write javadoc to make developer use close or saveAndClose finally.
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

    // todo write javadoc to make developer use close or saveAndClose finally.
    @Override
    public void close() {
        closed = true;
        timedPermissions.clear();
    }

    public boolean isClosed() {
        return this.closed;
    }

    private enum OperationType {
        ADD((seconds, delta) -> seconds + delta),
        REMOVE((seconds, delta) -> seconds - delta),
        SET((seconds, delta) -> delta),
        COUNT((seconds, delta) -> seconds - delta);

        private final IntBinaryOperator operator;

        OperationType(IntBinaryOperator operator) {
            this.operator = operator;
        }

        public IntBinaryOperator getOperator() {
            return this.operator;
        }

        public TimedPermissionEvent createEvent(UUID userUid, PermissionNode permission, int previousSeconds, int newSeconds) {
            return switch (this) {
                case ADD -> new TimedPermissionAddEvent(userUid, permission, previousSeconds, newSeconds);
                case REMOVE -> new TimedPermissionRemoveEvent(userUid, permission, previousSeconds);
                case SET -> new TimedPermissionSetEvent(userUid, permission, previousSeconds, newSeconds);
                case COUNT -> new TimedPermissionCountEvent(userUid, permission, previousSeconds, newSeconds);
            };
        }
    }
}
