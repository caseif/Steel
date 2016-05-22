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
package net.caseif.flint.steel.minigame;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.lobby.wizard.IWizardManager;
import net.caseif.flint.common.minigame.CommonMinigame;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.arena.SteelArena;
import net.caseif.flint.steel.lobby.SteelLobbySign;
import net.caseif.flint.steel.lobby.wizard.WizardManager;
import net.caseif.flint.steel.util.file.SteelDataFiles;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

/**
 * Implements {@link Minigame}.
 *
 * @author Max Roncacé
 */
public class SteelMinigame extends CommonMinigame {

    private final Plugin plugin;

    private final IWizardManager wizardManager;

    public SteelMinigame(String plugin) {
        super();
        assert plugin != null;
        if (Bukkit.getPluginManager().isPluginEnabled(plugin)) {
            this.plugin = Bukkit.getPluginManager().getPlugin(plugin);
        } else {
            throw new IllegalArgumentException("Plugin \"" + plugin + "\" is not loaded!");
        }
        SteelCore.logInfo(this.plugin + " has successfully hooked Steel");
        wizardManager = new WizardManager(this);
        SteelDataFiles.createMinigameDataFiles(this);
        loadArenas();
        loadLobbySigns();
    }

    @Override
    public String getPlugin() {
        return plugin.getName();
    }

    public Plugin getBukkitPlugin() {
        return plugin;
    }

    @Override
    public Arena createArena(String id, String name, Location3D spawnPoint, Boundary boundary)
            throws IllegalArgumentException {
        id = id.toLowerCase();
        if (getArenaMap().containsKey(id)) {
            throw new IllegalArgumentException("Cannot create arena: arena with ID \"" + id + "\" already exists");
        }
        if (id.contains(".")) {
            //TODO: document
            throw new IllegalArgumentException("Cannot create arena: ID \"" + id + "\" contains illegal characters");
        }

        SteelArena arena = new SteelArena(this, id, name, spawnPoint, boundary);
        try {
            arena.store();
        } catch (InvalidConfigurationException | IOException ex) {
            ex.printStackTrace();
            SteelCore.logSevere("Failed to save arena with ID " + arena.getId() + " to persistent storage");
        }
        getArenaMap().put(id, arena);
        return arena;
    }

    public IWizardManager getLobbyWizardManager() {
        return wizardManager;
    }

    private void loadArenas() {
        File arenaStore = SteelDataFiles.ARENA_STORE.getFile(this);
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(arenaStore);
            for (String key : yaml.getKeys(false)) {
                if (yaml.isConfigurationSection(key)) {
                    ConfigurationSection arenaSection = yaml.getConfigurationSection(key);
                    if (arenaSection.isSet(SteelArena.PERSISTENCE_NAME_KEY)
                            && arenaSection.isSet(SteelArena.PERSISTENCE_WORLD_KEY)) {
                        Location3D upperBound = Location3D.deserialize(
                                arenaSection.getString(SteelArena.PERSISTENCE_BOUNDS_UPPER_KEY)
                        );
                        Location3D lowerBound = Location3D.deserialize(
                                arenaSection.getString(SteelArena.PERSISTENCE_BOUNDS_LOWER_KEY)
                        );
                        SteelArena arena = new SteelArena(
                                this,
                                key.toLowerCase(),
                                arenaSection.getString(SteelArena.PERSISTENCE_NAME_KEY),
                                new Location3D(arenaSection.getString(SteelArena.PERSISTENCE_WORLD_KEY),
                                        lowerBound.getX(), lowerBound.getY(), lowerBound.getZ()),
                                new Boundary(
                                        upperBound,
                                        lowerBound
                                )
                        );
                        arena.getSpawnPointMap().remove(0); // remove initial placeholder spawn
                        arena.configure(arenaSection);
                        getArenaMap().put(arena.getId(), arena);
                    } else {
                        SteelCore.logWarning("Invalid configuration section \"" + key + "\"in arena store");
                    }
                }
            }
        } catch (InvalidConfigurationException | IOException ex) {
            ex.printStackTrace();
            SteelCore.logSevere("Failed to load existing arenas from disk");
        }
    }

    public void loadLobbySigns() {
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            File f = SteelDataFiles.LOBBY_STORE.getFile(this);
            yaml.load(f);
            for (String arenaKey : yaml.getKeys(false)) {
                if (yaml.isConfigurationSection(arenaKey)) {
                    Optional<Arena> arena = getArena(arenaKey);
                    if (arena.isPresent()) {
                        ConfigurationSection arenaSection = yaml.getConfigurationSection(arenaKey);
                        for (String coordKey : arenaSection.getKeys(false)) {
                            if (arenaSection.isConfigurationSection(coordKey)) {
                                try {
                                    Location3D loc = Location3D.deserialize(coordKey);
                                    if (loc.getWorld().isPresent()) {
                                        World w = Bukkit.getWorld(loc.getWorld().get());
                                        if (w != null) {
                                            Block block = w.getBlockAt(
                                                    (int) Math.floor(loc.getX()),
                                                    (int) Math.floor(loc.getY()),
                                                    (int) Math.floor(loc.getZ())
                                            );
                                            if (block.getState() instanceof Sign) {
                                                try {
                                                    LobbySign sign = SteelLobbySign.of(loc, (SteelArena) arena.get(),
                                                            arenaSection.getConfigurationSection(coordKey));
                                                    ((SteelArena) arena.get()).getLobbySignMap().put(loc, sign);
                                                } catch (IllegalArgumentException ex) {
                                                    SteelCore.logWarning("Found lobby sign in store with invalid "
                                                            + "configuration. Removing...");
                                                    arenaSection.set(coordKey, null);
                                                }
                                            } else {
                                                SteelCore.logWarning("Found lobby sign with location not containing a "
                                                        + "sign block. Removing...");
                                                arenaSection.set(coordKey, null);
                                            }
                                        } else {
                                            SteelCore.logVerbose("Cannot load world \"" + loc.getWorld().get()
                                                    + "\" - not loading contained lobby sign");
                                        }
                                        continue;
                                    } // else: continue to invalid warning
                                } catch (IllegalArgumentException ignored) { // continue to invalid warning
                                    ignored.printStackTrace();
                                }
                            } // else: continue to invalid warning
                            // never executes unless the serial is invalid in some way
                            SteelCore.logWarning("Found lobby sign in store with invalid location serial."
                                    + "Removing...");
                            arenaSection.set(coordKey, null);
                        }
                    } else {
                        SteelCore.logVerbose("Found orphaned lobby sign group (arena \"" + arenaKey
                                + "\" - not loading");
                    }
                }
            }
            yaml.save(f);
        } catch (InvalidConfigurationException | IOException ex) {
            SteelCore.logSevere("Failed to load lobby signs for minigame " + getPlugin());
            ex.printStackTrace();
        }
    }

}
