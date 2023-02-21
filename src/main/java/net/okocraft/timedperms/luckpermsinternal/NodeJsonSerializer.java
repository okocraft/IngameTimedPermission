package net.okocraft.timedperms.luckpermsinternal;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeBuilder;
import net.luckperms.api.node.metadata.types.InheritanceOriginMetadata;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class NodeJsonSerializer {

    private NodeJsonSerializer() {

    }

    public static JsonObject serializeNode(Node node, boolean includeInheritanceOrigin) {
        JsonObject attributes = new JsonObject();

        attributes.addProperty("type", node.getType().name().toLowerCase(Locale.ROOT));
        attributes.addProperty("key", node.getKey());
        attributes.addProperty("value", node.getValue());

        Instant expiry = node.getExpiry();
        if (expiry != null) {
            attributes.addProperty("expiry", expiry.getEpochSecond());
        }

        if (!node.getContexts().isEmpty()) {
            attributes.add("context", ContextSetJsonSerializer.serialize(node.getContexts()));
        }

        if (includeInheritanceOrigin) {
            InheritanceOriginMetadata origin = node.getMetadata(InheritanceOriginMetadata.KEY).orElse(null);
            if (origin != null) {
                JsonObject metadata = new JsonObject();
                metadata.add("inheritanceOrigin", serializeInheritanceOrigin(origin));
                attributes.add("metadata", metadata);
            }
        }

        return attributes;
    }

    private static JsonObject serializeInheritanceOrigin(InheritanceOriginMetadata origin) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", origin.getOrigin().getType());
        obj.addProperty("name", origin.getOrigin().getName());
        return obj;
    }

    public static JsonArray serializeNodes(Collection<Node> nodes) {
        JsonArray arr = new JsonArray();
        for (Node node : nodes) {
            arr.add(serializeNode(node, false));
        }
        return arr;
    }

    public static Node deserializeNode(JsonElement ent) {
        JsonObject attributes = ent.getAsJsonObject();

        String key = attributes.get("key").getAsString();

        if (key.isEmpty()) {
            return null; // skip
        }

        NodeBuilder<?, ?> builder = LuckPermsProvider.get().getNodeBuilderRegistry().forKey(key);

        boolean value = attributes.get("value").getAsBoolean();
        builder.value(value);

        if (attributes.has("expiry")) {
            builder.expiry(attributes.get("expiry").getAsLong());
        }

        if (attributes.has("context")) {
            builder.context(ContextSetJsonSerializer.deserialize(attributes.get("context")));
        }

        return builder.build();
    }

    public static Set<Node> deserializeNodes(JsonArray arr) {
        Set<Node> nodes = new HashSet<>();
        for (JsonElement ent : arr) {
            Node node = deserializeNode(ent);
            if (node != null) {
                nodes.add(node);
            }
        }
        return nodes;
    }
}