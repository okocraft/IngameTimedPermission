package net.okocraft.timedperms.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.luckperms.api.node.types.PermissionNode;
import net.okocraft.timedperms.Main;
import net.okocraft.timedperms.model.LocalPlayer;
import net.okocraft.timedperms.model.LocalPlayerFactory;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TimedPermsCommand implements CommandExecutor, TabExecutor {

    private final Main plugin;

    private final Set<String> playerNames;

    @SuppressWarnings("deprecation")
    public TimedPermsCommand(Main plugin) {
        this.plugin = plugin;
        long twoMonthAgo = System.currentTimeMillis() - 2 * 30 * 24 * 60 * 60 * 1000L;
        this.playerNames = Arrays.stream(this.plugin.getServer().getOfflinePlayers()).parallel()
                .filter(p -> p.getLastPlayed() > twoMonthAgo)
                .map(OfflinePlayer::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        if (!sender.hasPermission("timedperms.use")) {
            sender.sendMessage("you do not have permission.");
            return true;
        }

        if (args.length <= 2) {
            sender.sendMessage("not enough arg.");
            return true;
        }

        OfflinePlayer offlinePlayer;
        try {
            offlinePlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(args[1]));
        } catch (IllegalArgumentException e) {
            offlinePlayer = plugin.getServer().getOfflinePlayer(args[1]);
        }

        if (!offlinePlayer.hasPlayedBefore()) {
            sender.sendMessage("player not found.");
            return true;
        }
        LocalPlayer player = LocalPlayerFactory.get(offlinePlayer.getUniqueId());

        PermissionNode.Builder nodeBuilder = PermissionNode.builder(args[2].toLowerCase(Locale.ROOT));

        int i;
        for (i = 3; i < args.length; i++) {
            String arg = args[i];
            if (arg.contains("=")) {
                String[] argPart = arg.split("=", -1);
                if (argPart.length != 2) {
                    sender.sendMessage("command syntax error on context parsing.");
                    return true;
                }
                nodeBuilder.withContext(argPart[0], argPart[1]);
            } else {
                break;
            }
        }

        String sub = args[0];

        PermissionNode node = nodeBuilder.build();
        if (sub.equalsIgnoreCase("show")) {
            sender.sendMessage(offlinePlayer.getName() + "'s seconds left for permmission " + node + " is " + player.getSeconds(node));
            return true;
        }

        int secondDelta;
        if (args.length >= i + 1) {
            secondDelta = tryParse(args[i]).orElse(-1);
        } else if (sub.equalsIgnoreCase("remove")) {
            secondDelta = player.getSeconds(node);
        } else {
            sender.sendMessage("invalid number input.");
            return true;
        }
        if (secondDelta <= 0) {
            sender.sendMessage("invalid number input.");
            return true;
        }

        if (sub.equalsIgnoreCase("remove")) {
            int now = player.removeSeconds(node, secondDelta);
            sender.sendMessage("success removing for " + node + " " + secondDelta + " seconds (now " + now + ").");
        } else if (sub.equalsIgnoreCase("add")) {
            int now = player.addSeconds(node, secondDelta);
            sender.sendMessage("success adding for " + node + " " + secondDelta + " seconds (now " + now + ").");
        } else if (sub.equalsIgnoreCase("set")) {
            int now = player.setSeconds(node, secondDelta);
            sender.sendMessage("success setting " + node + " to " + now + " seconds.");
        } else {
            sender.sendMessage("add, remove or set required.");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("timedperms.use")) {
            return new ArrayList<>();
        }

        // timedperms show <player> <permission [context=value ...]>;
        // timedperms remove <player> <permission [context=value ...]> [time];
        // timedperms add <player> <permission [context=value ...]> <time>;
        // timedperms set <player> <permission [context=value ...]> <time>;

        List<String> subCommands = Arrays.asList("show", "remove", "add", "set");
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0].toLowerCase(Locale.ROOT), subCommands, new ArrayList<>());
        }
        String subCommand = args[0].toLowerCase(Locale.ROOT);
        if (!subCommands.contains(subCommand)) {
            return new ArrayList<>();
        }

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1].toLowerCase(Locale.ROOT), playerNames, new ArrayList<>());
        }

        if (args.length == 3) {
            return Collections.singletonList("permission");
        }

        int i;
        for (i = 3; i < args.length; i++) {
            String arg = args[i];
            if (arg.contains("=")) {
                String[] argPart = arg.split("=", -1);
                if (argPart.length != 2) {
                    return new ArrayList<>();
                }
            } else {
                break;
            }
        }

        if (i + 1 == args.length) {
            if (subCommand.equalsIgnoreCase("show")) {
                return Collections.singletonList("context=value");
            } else {
                return Arrays.asList("context=value", "time");
            }
        } else {
            return new ArrayList<>();
        }
    }

    private static OptionalInt tryParse(String intStr) {
        try {
            return OptionalInt.of(Integer.parseInt(intStr));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }
}
