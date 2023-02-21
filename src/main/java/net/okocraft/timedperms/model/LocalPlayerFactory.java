package net.okocraft.timedperms.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LocalPlayerFactory {

    private LocalPlayerFactory() {
    }

    private static final Map<UUID, LocalPlayer> CACHE = new HashMap<>();

    public static LocalPlayer get(UUID uuid) {
        return CACHE.computeIfAbsent(uuid, LocalPlayerFactory::create);
    }

    static void close(LocalPlayer player) {
        CACHE.remove(player.getUniqueId());
    }

    private static LocalPlayer create(UUID uid) {
        return new LocalPlayer(uid, LocalPlayerDataSerializer.loadFromFile(uid));
    }
}
