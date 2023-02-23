package net.okocraft.timedperms.listener;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeClearEvent;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import net.okocraft.timedperms.model.LocalPlayerFactory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final Set<EventSubscription<? extends LuckPermsEvent>> subscriptions = new HashSet<>();

    public void subscribeLuckPermsEvents() {
        EventBus eventBus = LuckPermsProvider.get().getEventBus();
        subscriptions.add(eventBus.subscribe(NodeMutateEvent.class, this::onNodeMutate));
        subscriptions.add(eventBus.subscribe(NodeRemoveEvent.class, this::onNodeRemove));
        subscriptions.add(eventBus.subscribe(NodeAddEvent.class, this::onNodeAdd));
        subscriptions.add(eventBus.subscribe(NodeClearEvent.class, this::onNodeClear));
    }

    public void unsubscribeLuckPermsEvents() {
        subscriptions.forEach(EventSubscription::close);
        subscriptions.clear();
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        LocalPlayerFactory.get(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        LocalPlayerFactory.get(event.getPlayer().getUniqueId()).saveAndClose();
    }

    private void onNodeMutate(NodeMutateEvent event) {
        onNodeChange(event.getTarget(), event.getDataBefore(), event.getDataAfter());
    }

    private void onNodeRemove(NodeRemoveEvent event) {
        onNodeChange(event.getTarget(), event.getDataBefore(), event.getDataAfter());
    }

    private void onNodeAdd(NodeAddEvent event) {
        onNodeChange(event.getTarget(), event.getDataBefore(), event.getDataAfter());
    }

    private void onNodeClear(NodeClearEvent event) {
        onNodeChange(event.getTarget(), event.getDataBefore(), event.getDataAfter());
    }

    private void onNodeChange(PermissionHolder permissionHolder, Set<Node> before, Set<Node> after) {
        if (!(permissionHolder instanceof User)) {
            return;
        }

        User user = (User) permissionHolder;
        LocalPlayerFactory.get(user.getUniqueId()).onPermissionRemoved(
                before.stream()
                        .filter(node -> node instanceof PermissionNode)
                        .map(node -> (PermissionNode) node)
                        .filter(Predicate.not(after::contains))
                        .collect(Collectors.toSet())
        );
    }
}
