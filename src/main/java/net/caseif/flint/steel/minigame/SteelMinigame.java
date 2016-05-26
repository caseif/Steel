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
import net.caseif.flint.common.util.helper.JsonHelper;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.arena.SteelArena;
import net.caseif.flint.steel.lobby.SteelLobbySign;
import net.caseif.flint.steel.lobby.wizard.WizardManager;
import net.caseif.flint.steel.util.compatibility.MinigameDataMigrationAgent;
import net.caseif.flint.steel.util.file.SteelDataFiles;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

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

        new MinigameDataMigrationAgent(this).migrateData();

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

        SteelArena arena = new SteelArena(this, id, name, spawnPoint, boundary);
        try {
            arena.store();
        } catch (IOException ex) {
            ex.printStackTrace();
            SteelCore.logSevere("Failed to save arena with ID " + arena.getId() + " to persistent storage");
        }
        getArenaMap().put(id, arena);
        return arena;
    }

    public IWizardManager getLobbyWizardManager() {
        return wizardManager;
    }

    //TODO: Whoa, this is supposed to be in Common. Next commit for sure.
    private void loadLobbySigns() {
        try {
            File store = SteelDataFiles.LOBBY_STORE.getFile(this);
            Optional<JsonObject> jsonOpt = JsonHelper.readJson(store);
            if (!jsonOpt.isPresent()) {
                return;
            }
            JsonObject json = jsonOpt.get();

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if (json.get(entry.getKey()).isJsonObject()) {
                    Optional<Arena> arena = getArena(entry.getKey());
                    if (arena.isPresent()) {
                        JsonObject arenaJson = json.getAsJsonObject(entry.getKey());
                        for (Map.Entry<String, JsonElement> arenaEntry : arenaJson.entrySet()) {
                            if (arenaJson.get(arenaEntry.getKey()).isJsonObject()) {
                                try {
                                    Location3D loc = Location3D.deserialize(arenaEntry.getKey());
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
                                                            arenaJson.getAsJsonObject(arenaEntry.getKey()));
                                                    ((SteelArena) arena.get()).getLobbySignMap().put(loc, sign);
                                                } catch (IllegalArgumentException ex) {
                                                    SteelCore.logWarning("Found lobby sign in store with invalid "
                                                            + "configuration. Removing...");
                                                    json.remove(arenaEntry.getKey());
                                                }
                                            } else {
                                                SteelCore.logWarning("Found lobby sign with location not containing a "
                                                        + "sign block. Removing...");
                                                json.remove(arenaEntry.getKey());
                                            }
                                        } else {
                                            SteelCore.logVerbose("Cannot load world \"" + loc.getWorld().get()
                                                    + "\" - not loading contained lobby sign");
                                        }
                                        continue;
                                    } // else: continue to invalid warning
                                } catch (IllegalArgumentException ignored) { // continue to invalid warning
                                }
                            } // else: continue to invalid warning
                            // never executes unless the serial is invalid in some way
                            SteelCore.logWarning("Found lobby sign in store with invalid location serial. "
                                    + "Removing...");
                            json.remove(arenaEntry.getKey());
                        }
                    } else {
                        SteelCore.logVerbose("Found orphaned lobby sign group (arena \"" + entry.getKey()
                                + "\") - not loading");
                    }
                }
            }

            try (FileWriter writer = new FileWriter(store)) {
                writer.write(json.toString());
            }
        } catch (IOException ex) {
            SteelCore.logSevere("Failed to load lobby signs for minigame " + getPlugin());
            ex.printStackTrace();
        }
    }

}
