/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016, Max Roncace <me@caseif.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.caseif.flint.steel.util.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Static utility class for inventory-related functionality.
 *
 * @author Max Roncacé
 */
public class InventoryHelper {

    public static ConfigurationSection serializeInventory(Inventory inventory) {
        return serializeInventory(inventory.getContents());
    }

    public static ConfigurationSection serializeInventory(ItemStack[] contents) {
        ConfigurationSection cs = new YamlConfiguration().createSection("doot doot");
        cs.set("capacity", contents.length);
        for (int i = 0; i < contents.length; i++) {
            cs.set(Integer.toString(i), contents[i]);
        }
        return cs;
    }

    public static ItemStack[] deserializeInventory(ConfigurationSection serial) throws IllegalArgumentException {
        if (!serial.contains("capacity")) {
            throw new IllegalArgumentException("Serialized inventory is missing required element \"capacity\"");
        }
        int capacity = serial.getInt("capacity");
        ItemStack[] contents = new ItemStack[capacity];
        for (int i = 0; i < capacity; i++) {
            if (serial.contains(Integer.toString(i))) {
                contents[i] = serial.getItemStack(Integer.toString(i));
            }
        }
        return contents;
    }

    private static JsonObject csToJson(ConfigurationSection cs) {
        JsonObject json = new JsonObject();
        for (String key : cs.getKeys(false)) {
            if (cs.isConfigurationSection(key)) {
                json.add(key, csToJson(cs.getConfigurationSection(key)));
            } else {
                if (cs.isList(key)) {
                    JsonArray arr = new JsonArray();
                    for (Object obj : cs.getList(key)) {
                        arr.add(objToJsonPrim(obj));
                    }
                } else {
                    json.add(key, objToJsonPrim(cs.get(key)));
                }
            }
        }
        return json;
    }

    private static JsonPrimitive objToJsonPrim(Object obj) {
        if (obj instanceof Boolean) {
            return new JsonPrimitive((Boolean) obj);
        } else if (obj instanceof Character) {
            return new JsonPrimitive((Character) obj);
        } else if (obj instanceof Number) {
            return new JsonPrimitive((Number) obj);
        } else if (obj instanceof String) {
            return new JsonPrimitive((String) obj);
        } else {
            throw new UnsupportedOperationException("BLEH");
        }
    }

}
