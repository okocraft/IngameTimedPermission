package net.okocraft.timedperms.config;

import java.util.List;
import java.util.stream.Collectors;
import net.luckperms.api.node.types.PermissionNode;
import net.okocraft.timedperms.Main;
import net.okocraft.timedperms.event.TimedPermissionCountEvent;
import net.okocraft.timedperms.event.TimedPermissionEvent;
import net.okocraft.timedperms.model.LocalPlayerFactory;
import net.okocraft.timedperms.placeholderapi.PlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public record CommandExecution(
    TriggerType trigger,
    int executionOffset,
    PermissionNode targetPermission,
    String playerPermission,
    boolean isConsoleSource,
    List<String> commands
) {

    private static List<CommandExecution> cache;
    private static Main plugin;

    public static List<CommandExecution> getExecutions() {
        if (cache == null) {
            cache = getPlugin().getConfiguration()
                    .getList("command-executions", CommandExecutionSerializer.SERIALIZER);
        }
        return cache;
    }

    private static Main getPlugin() {
        if (plugin == null) {
            plugin = Main.getPlugin(Main.class);
        }
        return plugin;
    }

    public void handleEvent(TimedPermissionEvent event, OfflinePlayer playerContext) {
        if (!event.getPermission().equals(targetPermission)
                || !LocalPlayerFactory.get(event.getUserUid()).hasPermission(playerPermission)) {
            return;
        }

        if (executionOffset < 0) {
            if (trigger == TriggerType.EXPIRE) {
                if (event instanceof TimedPermissionCountEvent
                        && ((TimedPermissionCountEvent) event).getNewSeconds() + executionOffset == 0) {
                    scheduleCommands(playerContext, 0);
                }
                return;
            }
        } else if (executionOffset > 0) {
            if (trigger != TriggerType.COUNT) {
                if (trigger.getCorrespondingEventType() == event.getClass()) {
                    scheduleCommands(playerContext, executionOffset);
                }
                return;
            }
        }

        // offset == 0 || trigger == count || (seconds + offset == 0 && trigger == expire)
        if (trigger.getCorrespondingEventType() == event.getClass()) {
            scheduleCommands(playerContext, executionOffset);
        }
    }

    public void scheduleCommands(OfflinePlayer playerContext, int delay) {
        if (delay == 0) {
            executeCommands(playerContext);
        } else {
            getPlugin().schedule(() -> executeCommands(playerContext), delay);
        }
    }

    public void executeCommands(OfflinePlayer playerContext) {
        getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
            if (isConsoleSource()) {
                getReplacedCommands(playerContext).forEach(
                        command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            } else {
                Player player = playerContext.getPlayer();
                if (player != null) {
                    getReplacedCommands(playerContext).forEach(command -> Bukkit.dispatchCommand(player, command));
                }
            }
        });
    }

    private List<String> getReplacedCommands(OfflinePlayer playerContext) {
        PlaceholderHook placeholder = getPlugin().getPlaceholderHook();
        return commands.stream()
                .map(command -> placeholder == null ? command : placeholder.setPlaceholder(playerContext, command))
                .map(command -> command.replaceAll("%permission%", targetPermission().getPermission()))
                .collect(Collectors.toList());
    }
}
