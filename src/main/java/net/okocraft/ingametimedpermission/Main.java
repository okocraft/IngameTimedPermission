package net.okocraft.ingametimedpermission;

import java.util.Locale;
import java.util.OptionalInt;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Main extends JavaPlugin implements Listener {

    private final PlayerDataLoader dataLoader = new PlayerDataLoader(this);

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(dataLoader, this);
        dataLoader.loadAll();
        executor.scheduleAtFixedRate(dataLoader::countOne, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        executor.shutdownNow();
        dataLoader.saveAll();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        if (!sender.hasPermission("ingametimedpermission.use")) {
            sender.sendMessage("you do not have permission.");
            return true;
        }

        if (args.length <= 2) {
            sender.sendMessage("not enough arg.");
            return true;
        }

        // timedpermission remove <player> <permission> [time];
        // timedpermission add <player> <permission> <time>;
        // timedpermission set <player> <permission> <time>;

        Player player = getServer().getPlayer(args[1]);
        if (player == null) {
            sender.sendMessage(args[1] + " is not online^^");
            return true;
        }

        String permission = args[2].toLowerCase(Locale.ROOT);

        IntConsumer valueSetter;

        String sub = args[0];
        if (sub.equalsIgnoreCase("remove")) {
            if (args.length == 3) {
                getServer().dispatchCommand(
                        getServer().getConsoleSender(),
                        "lp user " + player.getName() + " permission unset " + permission
                );
                return true;
            } else {
                valueSetter = remove -> dataLoader.remove(player, permission, remove);
            }
        } else if (sub.equalsIgnoreCase("add")) {
            if (args.length == 3) {
                sender.sendMessage("not enough arg.");
                return true;
            } else {
                valueSetter = add -> dataLoader.add(player, permission, add);
            }
        } else if (sub.equalsIgnoreCase("set")) {
            if (args.length == 3) {
                sender.sendMessage("not enough arg.");
                return true;
            } else {
                valueSetter = set -> dataLoader.set(player, permission, set);
            }
        } else {
            sender.sendMessage("add, remove or set required.");
            return true;
        }

        tryParse(args[3]).ifPresentOrElse(
                value -> {
                    valueSetter.accept(value);
                    sender.sendMessage("success.");
                },
                () -> sender.sendMessage("invalid number.")
        );
        return true;
    }

    private static OptionalInt tryParse(String intStr) {
        try {
            return OptionalInt.of(Integer.parseInt(intStr));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }
}
