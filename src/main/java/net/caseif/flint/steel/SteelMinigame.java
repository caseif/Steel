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
package net.caseif.flint.steel;

import net.caseif.flint.Arena;
import net.caseif.flint.Minigame;
import net.caseif.flint.common.CommonMinigame;
import net.caseif.flint.steel.util.io.DataFiles;
import net.caseif.flint.util.physical.Location3D;

import org.bukkit.Bukkit;
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

    private Plugin plugin;

    public SteelMinigame(String plugin) {
        if (Bukkit.getPluginManager().isPluginEnabled(plugin)) {
            this.plugin = Bukkit.getPluginManager().getPlugin(plugin);
        } else {
            throw new IllegalArgumentException("Plugin \"" + plugin + "\" is not loaded!");
        }
        DataFiles.createCoreDataFiles();
        loadArenas();
    }

    @Override
    public String getPlugin() {
        return plugin.getName();
    }

    public Plugin getBukkitPlugin() {
        return plugin;
    }

    @Override
    public Arena createArena(String id, String name, Location3D spawnPoint) throws IllegalArgumentException {
        if (arenas.containsKey(id)) {
            throw new IllegalArgumentException("Arena with ID \"" + id + "\" already exists");
        }
        SteelArena arena = new SteelArena(this, id, name, spawnPoint);
        try {
            arena.store();
        } catch (InvalidConfigurationException | IOException ex) {
            ex.printStackTrace();
            SteelCore.logSevere("Failed to save arena with ID " + arena.getId() + " to persistent storage");
        }
        return arena;
    }

    @Override
    public Arena createArena(String id, Location3D spawnPoint) throws IllegalArgumentException {
        return createArena(id, id, spawnPoint);
    }

    private void loadArenas() {
        File arenaStore = DataFiles.ARENA_STORE.getFile(this);
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(arenaStore);
            for (String key : yaml.getKeys(false)) {
                if (yaml.isConfigurationSection(key)) {
                    ConfigurationSection arenaSection = yaml.getConfigurationSection(key);
                    if (arenaSection.isSet(SteelArena.PERSISTENCE_NAME_KEY)
                            && arenaSection.isSet(SteelArena.PERSISTENCE_WORLD_KEY)) {
                        SteelArena arena = new SteelArena(this, key,
                                arenaSection.getString(SteelArena.PERSISTENCE_NAME_KEY),
                                new Location3D(arenaSection.getString(SteelArena.PERSISTENCE_WORLD_KEY), 0, 0, 0));
                        arena.removeSpawnPoint(0);
                        arena.configure(arenaSection);
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

}
