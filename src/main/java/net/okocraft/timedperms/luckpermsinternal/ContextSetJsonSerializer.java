package net.okocraft.timedperms.luckpermsinternal;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.context.MutableContextSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Serializes and deserializes {@link ContextSet}s to and from JSON.
 *
 * <p>The entries within the serialized output are sorted, this ensures that any two invocations
 * of {@link #serialize(ContextSet)} with the same {@link ContextSet} will produce
 * the same exact JSON string.</p>
 */
public final class ContextSetJsonSerializer {
    private ContextSetJsonSerializer() {}

    public static JsonObject serialize(ContextSet contextSet) {
        JsonObject output = new JsonObject();

        List<Map.Entry<String, Set<String>>> entries = new ArrayList<>(contextSet.toMap().entrySet());
        entries.sort(Map.Entry.comparingByKey()); // sort - consistent output order

        for (Map.Entry<String, Set<String>> entry : entries) {
            String[] values = entry.getValue().toArray(new String[0]);
            switch (values.length) {
                case 0:
                    break;
                case 1:
                    output.addProperty(entry.getKey(), values[0]);
                    break;
                default:
                    Arrays.sort(values); // sort - consistent output order
                    JsonArray arr = new JsonArray();
                    for (String value : values) {
                        arr.add(new JsonPrimitive(value));
                    }
                    output.add(entry.getKey(), arr);
                    break;
            }
        }

        return output;
    }

    public static ContextSet deserialize(Gson gson, String input) {
        Objects.requireNonNull(input, "input");
        if (input.equals("{}")) {
            return ImmutableContextSet.empty();
        }

        JsonObject jsonObject = gson.fromJson(input, JsonObject.class);
        if (jsonObject == null) {
            return ImmutableContextSet.empty();
        }

        return deserialize(jsonObject);
    }

    public static ContextSet deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject jsonObject = element.getAsJsonObject();

        Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
        if (entries.isEmpty()) {
            return ImmutableContextSet.empty();
        }

        MutableContextSet contextSet = MutableContextSet.create();
        for (Map.Entry<String, JsonElement> entry : entries) {
            String k = entry.getKey();
            JsonElement v = entry.getValue();

            if (v.isJsonArray()) {
                JsonArray values = v.getAsJsonArray();
                for (JsonElement value : values) {
                    contextSet.add(k, value.getAsString());
                }
            } else {
                contextSet.add(k, v.getAsString());
            }
        }

        return contextSet;
    }

}
