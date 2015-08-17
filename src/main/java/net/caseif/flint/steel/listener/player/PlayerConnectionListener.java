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
package net.caseif.flint.steel.listener.player;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.minigame.SteelMinigame;
import net.caseif.flint.steel.round.SteelRound;
import net.caseif.flint.steel.util.file.DataFiles;
import net.caseif.flint.steel.util.helper.PlayerHelper;

import com.google.common.base.Optional;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Listener for events relating to players' connections.
 *
 * @author Max Roncacé
 */
public class PlayerConnectionListener implements Listener {

    /**
     * Config key for the list of offline players that need to be reset on join.
     */
    private static final String OFFLINE_PLAYER_LIST_KEY = "offline";

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        for (Minigame mg : CommonCore.getMinigames().values()) {
            Optional<Challenger> ch = mg.getChallenger(uuid);
            if (ch.isPresent()) {
                // store the player to disk so their inventory and location can be popped later
                ((SteelRound)ch.get().getRound()).removeChallenger(ch.get(), true, true);

                try {
                    File offlinePlayers = DataFiles.OFFLINE_PLAYER_STORE.getFile();
                    YamlConfiguration yaml = new YamlConfiguration();
                    yaml.load(offlinePlayers);

                    List<String> players = yaml.getStringList(OFFLINE_PLAYER_LIST_KEY);
                    if (players == null) {
                        players = new ArrayList<>();
                    }
                    players.add(uuid.toString());
                    yaml.set(OFFLINE_PLAYER_LIST_KEY, players);
                    yaml.save(offlinePlayers);
                } catch (InvalidConfigurationException | IOException ex) {
                    ex.printStackTrace();
                    SteelCore.logSevere("Failed to store data for disconnecting challenger "
                            + event.getPlayer().getName());
                }
            }

            if (((SteelMinigame) mg).getLobbyWizardManager().isWizardPlayer(event.getPlayer().getUniqueId())) {
                ((SteelMinigame) mg).getLobbyWizardManager().removePlayer(event.getPlayer().getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        tryReset(event.getPlayer());

        // the rest of the method is insurance against a catastrophic failure
        // of the server while players are still in a round
        try {
            PlayerHelper.popInventory(event.getPlayer());
        } catch (IllegalArgumentException ignored) { // don't need to pop, so we can ignore it
        } catch (InvalidConfigurationException | IOException ex) {
            // inventory was present but Something Happened™
            SteelCore.logSevere("Failed to pop inventory for player " + event.getPlayer().getName());
            ex.printStackTrace();
        }

        try {
            PlayerHelper.popLocation(event.getPlayer());
        } catch (IllegalArgumentException ignored) { // don't need to pop, so we can ignore it
        } catch (InvalidConfigurationException | IOException ex) {
            // location was present but Something Happened™
            SteelCore.logSevere("Failed to pop location for player " + event.getPlayer().getName());
            ex.printStackTrace();
        }
    }

    private void tryReset(Player player) {
        UUID uuid = player.getUniqueId();
        try {
            File offlinePlayers = DataFiles.OFFLINE_PLAYER_STORE.getFile();
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.load(offlinePlayers);

            if (yaml.isSet(OFFLINE_PLAYER_LIST_KEY)) {
                List<String> players = yaml.getStringList(OFFLINE_PLAYER_LIST_KEY);
                // check whether the player left while in a round
                if (players.contains(uuid.toString())) {

                    // these two try-blocks are separate so they can both run even if one fails
                    try {
                        PlayerHelper.popInventory(player);
                    } catch (InvalidConfigurationException | IOException ex) {
                        ex.printStackTrace();
                        SteelCore.logSevere("Failed to pop inventory for player " + player.getName());
                    }

                    try {
                        PlayerHelper.popLocation(player);
                    } catch (InvalidConfigurationException | IOException ex) {
                        ex.printStackTrace();
                        SteelCore.logSevere("Failed to pop inventory for player " + player.getName());
                    }

                    players.remove(uuid.toString());
                    yaml.set(OFFLINE_PLAYER_LIST_KEY, players);
                    yaml.save(offlinePlayers);
                }
            }
        } catch (InvalidConfigurationException | IOException ex) {
            ex.printStackTrace();
            SteelCore.logSevere("Failed to load offline player data");
        }
    }

}
