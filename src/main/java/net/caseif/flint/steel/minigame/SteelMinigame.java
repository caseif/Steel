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
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.arena.SteelArena;
import net.caseif.flint.steel.lobby.wizard.WizardManager;
import net.caseif.flint.steel.util.compatibility.MinigameDataMigrationAgent;
import net.caseif.flint.steel.util.file.SteelDataFiles;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.plugin.Plugin;

import java.io.IOException;

/**
 * Implements {@link Minigame}.
 *
 * @author Max Roncacé
 */
public class SteelMinigame extends CommonMinigame {

    private final Plugin plugin;

    private final IWizardManager wizardManager;

    public SteelMinigame(Plugin plugin) {
        super();

        assert plugin != null;

        this.plugin = plugin;
        if (!Bukkit.getPluginManager().isPluginEnabled(plugin)) {
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

        SteelArena arena = new SteelArena(this, id, name, new Location3D[] {spawnPoint}, boundary);
        try {
            arena.store();
        } catch (IOException ex) {
            ex.printStackTrace();
            SteelCore.logSevere("Failed to save arena with ID " + arena.getId() + " to persistent storage");
        }
        return arena;
    }

    public IWizardManager getLobbyWizardManager() {
        return wizardManager;
    }

    protected int checkPhysicalLobbySign(Location3D loc) {
        if (loc.getWorld().isPresent()) {
            World w = Bukkit.getWorld(loc.getWorld().get());
            if (w != null) {
                Block block = w.getBlockAt(
                        (int) Math.floor(loc.getX()),
                        (int) Math.floor(loc.getY()),
                        (int) Math.floor(loc.getZ())
                );
                if (block.getState() instanceof Sign) {
                    return 0;
                } else {
                    SteelCore.logWarning("Found lobby sign with location not containing a sign block. Removing...");
                    return 2;
                }
            } else {
                SteelCore.logVerbose("Cannot load world \"" + loc.getWorld().get()
                        + "\" - not loading contained lobby sign");
                return 1;
            }
        } else {
            SteelCore.logWarning("Found lobby sign in store with invalid location serial. Removing...");
            return 2;
        }
    }

}
