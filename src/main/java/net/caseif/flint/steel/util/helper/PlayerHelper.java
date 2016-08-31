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

import net.caseif.flint.common.util.helper.CommonPlayerHelper;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.util.file.SteelDataFiles;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

/**
 * Static utility class for player-related functionality.
 *
 * @author Max Roncacé
 */
public class PlayerHelper {

    private static final String PLAYER_INVENTORY_PRIMARY_KEY = "primary";
    private static final String PLAYER_INVENTORY_ARMOR_KEY = "armor";

    private static Method getOnlinePlayers;
    public static boolean newOnlinePlayersMethod = false;

    static {
        try {
            getOnlinePlayers = Bukkit.class.getMethod("getOnlinePlayers");
            if (getOnlinePlayers.getReturnType() == Collection.class) {
                newOnlinePlayersMethod = true;
            }
        } catch (NoSuchMethodException ex) {
            SteelCore.logSevere("Failed to get getOnlinePlayers method!");
            ex.printStackTrace();
        }
    }

    /**
     * Pushes the inventory of the given player into persistent storage.
     *
     * @param player The {@link Player} to push the inventory of
     * @throws IOException If an exception occurs while saving into persistent
     *     storage
     */
    @SuppressWarnings("deprecation")
    public static void pushInventory(Player player) throws IOException {
        PlayerInventory inv = player.getInventory();
        // the file to store the inventory in
        File storage = new File(SteelDataFiles.PLAYER_INVENTORY_DIR.getFile(), player.getUniqueId() + ".yml");
        if (storage.exists()) { // verify file isn't already present on disk (meaning it wasn't popped the last time)
            SteelCore.logVerbose("Inventory push requested for player " + player.getName() + ", but "
                    + "inventory was already present in persistent storage. Popping stored inventory first.");
            try {
                popInventory(player);
            } catch (InvalidConfigurationException ex) {
                throw new IOException(ex); // this is probably a bad thing of me to do but it's for a fringe case anyway
            }
        }
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set(PLAYER_INVENTORY_PRIMARY_KEY, InventoryHelper.serializeInventory(inv));
        yaml.set(PLAYER_INVENTORY_ARMOR_KEY, InventoryHelper.serializeInventory(inv.getArmorContents()));
        yaml.save(storage); // save to disk
        inv.clear(); // clear the inventory to complete the push to disk
        inv.setArmorContents(new ItemStack[inv.getArmorContents().length]);
        player.updateInventory();
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
    @SuppressWarnings("deprecation")
    public static void popInventory(Player player) throws IllegalArgumentException, IOException,
            InvalidConfigurationException {
        // the file to load the inventory from
        File storage = new File(SteelDataFiles.PLAYER_INVENTORY_DIR.getFile(), player.getUniqueId() + ".yml");
        if (!storage.exists()) { // verify file is present on disk
            throw new IllegalArgumentException("Inventory pop requested for player " + player.getName() + ", but "
                    + "inventory was not present in persistent storage!");
        }
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(storage); // load from disk
        if (!yaml.contains(PLAYER_INVENTORY_PRIMARY_KEY)) {
            throw new InvalidConfigurationException("Stored inventory is missing required section \""
                    + PLAYER_INVENTORY_PRIMARY_KEY + "\"");
        }
        player.getInventory().clear();
        player.getInventory().setContents(
                InventoryHelper.deserializeInventory(yaml.getConfigurationSection(PLAYER_INVENTORY_PRIMARY_KEY))
        );
        if (yaml.contains(PLAYER_INVENTORY_ARMOR_KEY)) {
            player.getInventory().setArmorContents(
                    InventoryHelper.deserializeInventory(yaml.getConfigurationSection(PLAYER_INVENTORY_ARMOR_KEY))
            );
        }
        player.updateInventory();
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
        CommonPlayerHelper.storeLocation(player.getUniqueId(),
                LocationHelper.convertLocation(player.getLocation()));
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
    public static void popLocation(Player player)
            throws IllegalArgumentException, InvalidConfigurationException, IOException {
        Optional<Location3D> retLoc = CommonPlayerHelper.getReturnLocation(player.getUniqueId());
        if (!retLoc.isPresent()) {
            throw new IllegalArgumentException("Location of player " + player.getName()
                    + " not present in persistent store");
        }
        player.teleport(LocationHelper.convertLocation(retLoc.get()));
    }

    /**
     * Version-independent getOnlinePlayers() method.
     *
     * @return a list of online players
     */
    @SuppressWarnings("unchecked")
    public static Collection<? extends Player> getOnlinePlayers() {
        try {
            if (newOnlinePlayersMethod) {
                return (Collection<? extends Player>) getOnlinePlayers.invoke(null);
            } else {
                return Arrays.asList((Player[]) getOnlinePlayers.invoke(null));
            }
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException("Failed to invoke getOnlinePlayers method!", ex);
        }
    }

}
