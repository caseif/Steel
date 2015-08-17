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
package net.caseif.flint.steel.lobby;

import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.lobby.CommonLobbySign;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.lobby.type.ChallengerListingLobbySign;
import net.caseif.flint.lobby.type.StatusLobbySign;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.SteelMain;
import net.caseif.flint.steel.arena.SteelArena;
import net.caseif.flint.steel.lobby.type.SteelChallengerListingLobbySign;
import net.caseif.flint.steel.lobby.type.SteelStatusLobbySign;
import net.caseif.flint.steel.util.file.DataFiles;
import net.caseif.flint.steel.util.helper.LocationHelper;
import net.caseif.flint.util.physical.Location3D;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Implements {@link LobbySign}.
 *
 * @author Max Roncacé
 */
public abstract class SteelLobbySign extends CommonLobbySign {

    private static final String PERSIST_TYPE_KEY = "type";
    private static final String PERSIST_TYPE_STATUS = "status";
    private static final String PERSIST_TYPE_LISTING = "listing";

    private static final String PERSIST_INDEX_KEY = "index";

    public SteelLobbySign(Location3D location, CommonArena arena) {
        super(location, arena);
        final LobbySign sign = this;
        Bukkit.getScheduler().runTask(SteelMain.getInstance(), new Runnable() {
            @Override
            public void run() {
                sign.update();
            }
        });
    }

    @Override
    public void unregister() {
        super.unregister();
        // blank the physical sign block
        World world = Bukkit.getWorld(getLocation().getWorld().get());
        if (world == null) {
            SteelCore.logVerbose("Cannot blank unregistered lobby sign: world is not loaded");
        }
        Block block = LocationHelper.convertLocation(getLocation()).getBlock();
        if (block.getState() instanceof Sign) {
            for (int i = 0; i < ((Sign) block.getState()).getLines().length; i++) {
                ((Sign) block.getState()).setLine(i, "");
            }
        } else {
            SteelCore.logWarning("Cannot blank unregistered lobby sign: not a sign");
        }
        orphan();
    }

    @Override
    public void store() {
        store(false);
    }

    @Override
    public void unstore() {
        store(true);
    }

    public Block getBlock() {
        World world = Bukkit.getWorld(getLocation().getWorld().get());
        if (world == null) {
            throw new IllegalStateException("Cannot get world \"" + getLocation().getWorld().get()
                    + "\" for lobby sign");
        }
        return world.getBlockAt((int) getLocation().getX(), (int) getLocation().getY(), (int) getLocation().getZ());
    }

    private void store(boolean remove) {
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            File f = DataFiles.LOBBY_STORE.getFile(getMinigame());
            yaml.load(f);
            ConfigurationSection arenaSection = yaml.getConfigurationSection(getArena().getId());
            if (arenaSection == null) {
                if (!remove) { // okay to create it since we're newly storing the sign
                    arenaSection = yaml.createSection(getArena().getId());
                } else { // can't delete something that's not there
                    SteelCore.logWarning("Engine requested removal of lobby sign from store, but arena was not "
                            + "defined");
                    return;
                }
            }

            String locSerial = getLocation().serialize();
            if (remove) {
                if (arenaSection.isSet(locSerial)) {
                    arenaSection.set(locSerial, null);
                } else {
                    SteelCore.logWarning("Engine requested removal of lobby sign from store, but respective section "
                            + "was not defined");
                }
            } else {
                ConfigurationSection signSection = arenaSection.createSection(locSerial);
                String type;
                if (this instanceof StatusLobbySign) {
                    type = PERSIST_TYPE_STATUS;
                } else if (this instanceof ChallengerListingLobbySign) {
                    type = PERSIST_TYPE_LISTING;
                } else {
                    throw new AssertionError("Invalid LobbySign object. Report this immediately.");
                }
                signSection.set(PERSIST_TYPE_KEY, type);
                if (this instanceof ChallengerListingLobbySign) {
                    signSection.set(PERSIST_INDEX_KEY, ((ChallengerListingLobbySign) this).getIndex());
                }
            }

            yaml.save(f);
        } catch (InvalidConfigurationException | IOException ex) {
            SteelCore.logSevere("Failed to write to lobby sign store");
            ex.printStackTrace();
        }
    }

    public static SteelLobbySign of(Location3D location, SteelArena arena, ConfigurationSection section)
            throws IllegalArgumentException {
        if (section.isString(PERSIST_TYPE_KEY)) {
            String type = section.getString(PERSIST_TYPE_KEY);
            switch (type) {
                case PERSIST_TYPE_STATUS: {
                    return new SteelStatusLobbySign(location, arena);
                }
                case PERSIST_TYPE_LISTING: {
                    if (!section.isInt(PERSIST_INDEX_KEY)) {
                        break;
                    }
                    int index = section.getInt(PERSIST_INDEX_KEY);
                    return new SteelChallengerListingLobbySign(location, arena, index);
                }
                default: { // continue to IllegalArgumentException
                }
            }
        }
        throw new IllegalArgumentException("Invalid ConfigurationSection for LobbySign");
    }

}
