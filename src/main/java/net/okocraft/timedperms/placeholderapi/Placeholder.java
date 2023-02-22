package net.okocraft.timedperms.placeholderapi;

import java.util.Locale;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.luckperms.api.node.types.PermissionNode;
import net.okocraft.timedperms.Main;
import net.okocraft.timedperms.model.LocalPlayer;
import net.okocraft.timedperms.model.LocalPlayerFactory;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Placeholder extends PlaceholderExpansion {

    private final Main plugin;

    public Placeholder(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getRequiredPlugin() {
        return plugin.getName();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // identifier: %identifier_params% -> identifier
        // params: %identifier_params% -> params

        // format1: %timedperms_<player>_<permission>_<context=value>_...% (returns seconds left)
        // format2: %timedperms_<permission>_<context=value>_...% (executor required, returns seconds left)

        String[] args = params.split(params.contains(",") ? "," : "_", -1);

        int permissionIndex = -1;
        for (int i = 0; i < args.length; i++) {
            permissionIndex = i - 1;
            if (args[i].contains("=")) {
                break;
            }
        }
        if (permissionIndex < 0) {
            return "-1";
        }

        OfflinePlayer offlinePlayer;
        if (permissionIndex == 0) {
            offlinePlayer = player;
        } else {
            String[] nameSplit = new String[permissionIndex];
            System.arraycopy(args, 0, nameSplit, 0, permissionIndex);
            String name = String.join("_", nameSplit);
            try {
                offlinePlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(name));
            } catch (IllegalArgumentException e) {
                offlinePlayer = plugin.getServer().getOfflinePlayer(name);
            }
        }

        if (!offlinePlayer.hasPlayedBefore()) {
            return "-1";
        }
        LocalPlayer localPlayer = LocalPlayerFactory.get(offlinePlayer.getUniqueId());

        PermissionNode.Builder builder = PermissionNode.builder(args[permissionIndex].toLowerCase(Locale.ROOT));

        for (int i = permissionIndex + 1; i < args.length; i++) {
            String arg = args[i];
            if (arg.contains("=")) {
                String[] argPart = arg.split("=", -1);
                if (argPart.length != 2) {
                    return "-1";
                }
                builder.withContext(argPart[0], argPart[1]);
            } else {
                break;
            }
        }

        return String.valueOf(localPlayer.getSeconds(builder.build()));
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        return onRequest(player, params);
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getName().toLowerCase(Locale.ROOT);
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
}
