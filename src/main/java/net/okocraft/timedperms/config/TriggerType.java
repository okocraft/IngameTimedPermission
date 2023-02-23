package net.okocraft.timedperms.config;

import java.util.Locale;
import net.okocraft.timedperms.event.TimedPermissionAddEvent;
import net.okocraft.timedperms.event.TimedPermissionCountEvent;
import net.okocraft.timedperms.event.TimedPermissionEvent;
import net.okocraft.timedperms.event.TimedPermissionExpireEvent;
import net.okocraft.timedperms.event.TimedPermissionRegisteredEvent;
import net.okocraft.timedperms.event.TimedPermissionRemoveEvent;
import net.okocraft.timedperms.event.TimedPermissionSetEvent;
import net.okocraft.timedperms.event.TimedPermissionUnregisteredEvent;

public enum TriggerType {
    REGISTER(TimedPermissionRegisteredEvent.class),
    UNREGISTER(TimedPermissionUnregisteredEvent.class),
    COUNT(TimedPermissionCountEvent.class),
    EXPIRE(TimedPermissionExpireEvent.class),
    ADD(TimedPermissionAddEvent.class),
    SET(TimedPermissionSetEvent.class),
    REMOVE(TimedPermissionRemoveEvent.class);

    private final Class<? extends TimedPermissionEvent> correspondingEventClass;

    TriggerType(Class<? extends TimedPermissionEvent> correspondingEventClass) {
        this.correspondingEventClass = correspondingEventClass;
    }

    public Class<? extends TimedPermissionEvent> getCorrespondingEventType() {
        return this.correspondingEventClass;
    }

    public static TriggerType match(String name, TriggerType def) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
}