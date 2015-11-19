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
