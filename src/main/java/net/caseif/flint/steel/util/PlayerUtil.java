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
package net.caseif.flint.steel.util;

import net.caseif.flint.steel.util.helper.InventoryHelper;
import net.caseif.flint.steel.util.io.DataFiles;
import net.caseif.flint.util.physical.Location3D;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;

/**
 * Utility methods regarding players.
 *
 * @author Max Roncacé
 */
public class PlayerUtil {

    private static final String PLAYER_INVENTORY_PRIMARY_KEY = "primary";
    private static final String PLAYER_INVENTORY_ARMOR_KEY = "armor";

    /**
     * Pushes the inventory of the given player into persistent storage.
     *
     * @param player The {@link Player} to push the inventory of
     * @throws IllegalStateException If the inventory of the given
     *     {@link Player} is already present in persistent storage
     * @throws IOException If an exception occurs while saving into persistent
     *     storage
     */
    public static void pushInventory(Player player) throws IllegalStateException, IOException {
        PlayerInventory inv = player.getInventory();
        // the file to store the inventory in
        File storage = new File(DataFiles.PLAYER_INVENTORY_DIR.getFile(), player.getUniqueId() + ".yml");
        if (storage.exists()) { // verify file isn't already present on disk (meaning it wasn't popped the last time)
            throw new IllegalStateException("Inventory push requested for player " + player.getName() + ", but "
                    + "inventory was already present in persistent storage!");
        }
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set(PLAYER_INVENTORY_PRIMARY_KEY, InventoryHelper.serializeInventory(inv));
        yaml.set(PLAYER_INVENTORY_ARMOR_KEY, InventoryHelper.serializeInventory(inv.getArmorContents()));
        yaml.save(storage); // save to disk
        inv.clear(); // clear the inventory to complete the push to disk
    }

    /**
     * Pops the inventory of the given player from persistent storage.
     *
     * @param player The {@link Player} to pop the inventory of
     * @throws IllegalArgumentException If the inventory of the given
     *     {@link Player} is not present in persistent storage
     * @throws IOException If an exception occurs while loading from persistent
     *     storage
     * @throws InvalidConfigurationException If the stored inventory is invalid
     */
    //TODO: generalize some of this code for use with rollback storage
    public static void popInventory(Player player) throws IllegalArgumentException, IOException,
            InvalidConfigurationException {
        // the file to load the inventory from
        File storage = new File(DataFiles.PLAYER_INVENTORY_DIR.getFile(), player.getUniqueId() + ".yml");
        if (!storage.exists()) { // verify file is present on disk
            throw new IllegalStateException("Inventory pop requested for player " + player.getName() + ", but "
                    + "inventory was not present in persistent storage!");
        }
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(storage); // load from disk
        if (!yaml.contains(PLAYER_INVENTORY_PRIMARY_KEY)) {
            throw new InvalidConfigurationException("Stored inventory is missing required section \""
                    + PLAYER_INVENTORY_PRIMARY_KEY + "\"");
        }
        player.getInventory().clear();
        {
            player.getInventory().setContents(
                    InventoryHelper.deserializeInventory(yaml.getConfigurationSection(PLAYER_INVENTORY_PRIMARY_KEY))
            );
        }
        {
            if (yaml.contains(PLAYER_INVENTORY_ARMOR_KEY)) {
                player.getInventory().setArmorContents(
                        InventoryHelper.deserializeInventory(yaml.getConfigurationSection(PLAYER_INVENTORY_ARMOR_KEY))
                );
            }
        }
        //noinspection ResultOfMethodCallIgnored
        storage.delete();
    }

    /**
     * Stores the given {@link Player}'s current location to persistent storage.
     *
     * @param player The {@link Player} to store the location of
     * @throws InvalidConfigurationException If an exception occurs while saving
     *     to disk
     * @throws IOException If an exception occurs while saving to disk
     */
    public static void storeLocation(Player player) throws InvalidConfigurationException, IOException {
        File file = DataFiles.PLAYER_LOCATION_STORE.getFile();
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(file);
        yaml.set(player.getUniqueId().toString(), MiscUtil.convertLocation(player.getLocation()).serialize());
        yaml.save(file);
    }

    /**
     * Pops the given {@link Player}'s stored location from persistent storage,
     * teleporting them to it.
     *
     * @param player The {@link Player} to load the location of and teleport
     * @throws IllegalArgumentException If the player's location is not present
     *     in the persistent store or if an error occurs during deserialization
     * @throws InvalidConfigurationException If an exception occurs while
     *     loading from disk
     * @throws IOException If an exception occurs while saving to disk
     */
    public static void popLocation(Player player) throws IllegalArgumentException, InvalidConfigurationException,
            IOException {
        File file = DataFiles.PLAYER_LOCATION_STORE.getFile();
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(file);
        if (!yaml.isSet(player.getUniqueId().toString())) {
            throw new IllegalArgumentException("Location of player " + player.getName() + " not present in persistent "
                    + "store");
        }
        Location3D l3d = Location3D.deserialize(yaml.getString(player.getUniqueId().toString()));
        if (!l3d.getWorld().isPresent()) {
            throw new IllegalArgumentException("World not present in stored location of player " + player.getName());
        }
        player.teleport(MiscUtil.convertLocation(l3d));
        yaml.set(player.getUniqueId().toString(), null);
        yaml.save(file);
    }

}
