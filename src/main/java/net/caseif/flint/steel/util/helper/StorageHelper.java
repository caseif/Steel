/*
 * New BSD License (BSD-new)
 *
 * Copyright (c) 2015 Maxim Roncacé
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.caseif.flint.steel.util.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Static utility class for storage-related functionality.
 */
public class StorageHelper {

    /**
     * Recursively converts a YAML {@link ConfigurationSection} to a
     * {@link JsonObject}.
     *
     * @param cs The {@link ConfigurationSection} to convert
     * @return The converted {@link JsonObject}
     */
    public static JsonObject yamlToJson(ConfigurationSection cs) {
        JsonObject json = new JsonObject();
        for (String key : cs.getKeys(false)) {
            if (cs.isConfigurationSection(key)) {
                json.add(key, yamlToJson(cs.getConfigurationSection(key)));
            } else if (cs.isList(key)) {
                JsonArray arr = new JsonArray();
                for (Object obj : cs.getList(key)) {
                    arr.add(objToJsonElement(obj));
                }
                json.add(key, arr);
            } else {
                json.add(key, objToJsonElement(cs.get(key)));
            }
        }
        return json;
    }

    /**
     * Recursively onverts a {@link JsonObject} to a YAML
     * {@link ConfigurationSection}.
     *
     * @param json The {@link JsonObject} to convert
     * @return The converted {@link ConfigurationSection}
     */
    public static YamlConfiguration jsonToYaml(JsonObject json) {
        return (YamlConfiguration) jsonToYaml(json, true);
    }

    private static ConfigurationSection jsonToYaml(JsonObject json, boolean entryPoint) {
        ConfigurationSection cs = new YamlConfiguration();
        if (!entryPoint) {
            cs = cs.createSection("squid kid");
        }
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            cs.set(entry.getKey(), jsonElementToObj(entry.getValue()));
        }
        return cs;
    }

    private static JsonElement objToJsonElement(Object obj) {
        if (obj instanceof Boolean) {
            return new JsonPrimitive((Boolean) obj);
        } else if (obj instanceof Character) {
            return new JsonPrimitive((Character) obj);
        } else if (obj instanceof Number) {
            return new JsonPrimitive((Number) obj);
        } else if (obj instanceof String) {
            return new JsonPrimitive((String) obj);
        } else if (obj instanceof ConfigurationSerializable) {
            Map<String, Object> serial = ((ConfigurationSerializable) obj).serialize();
            JsonObject json = new JsonObject();
            json.addProperty("==", obj.getClass().getName());
            for (Map.Entry<String, Object> entry : serial.entrySet()) {
                json.add(entry.getKey(), objToJsonElement(entry.getValue()));
            }
            return json;
        } else {
            throw new UnsupportedOperationException("Unsupported object for JSON encoding");
        }
    }

    private static Object jsonPrimToObj(JsonPrimitive prim) {
        if (prim.isBoolean()) {
            return prim.getAsBoolean();
        } else if (prim.isNumber()) {
            if (prim.getAsDouble() % 1 == 0) {
                return prim.getAsLong() != prim.getAsInt() ? prim.getAsLong() : prim.getAsInt();
            } else {
                return prim.getAsNumber();
            }
        } else if (prim.isString()) {
            return prim.getAsString();
        }
        return null;
    }

    private static Object jsonSerialToObj(JsonObject json) {
        assert json.has("==");
        Map<String, Object> serial = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            serial.put(entry.getKey(), jsonElementToObj(entry.getValue()));
        }
        try {
            Class<?> clazz = Class.forName(json.get("==").getAsString());
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals("deserialize")) {
                    return method.invoke(null, serial);
                }
            }
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static Object jsonElementToObj(JsonElement json) {
        if (json.isJsonObject()) {
            if (json.getAsJsonObject().has("==")) {
                return jsonSerialToObj(json.getAsJsonObject());
            } else {
                return jsonToYaml((JsonObject) json, false);
            }
        } else if (json.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement je : json.getAsJsonArray()) {
                if (je.isJsonPrimitive()) {
                    list.add(jsonPrimToObj(je.getAsJsonPrimitive()));
                }
            }
            return list;
        } else if (json.isJsonPrimitive()) {
            return jsonPrimToObj(json.getAsJsonPrimitive());
        }
        return null;
    }

}
