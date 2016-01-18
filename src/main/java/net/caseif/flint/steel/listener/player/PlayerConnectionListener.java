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
package net.caseif.flint.steel.listener.player;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.challenger.SteelChallenger;
import net.caseif.flint.steel.minigame.SteelMinigame;
import net.caseif.flint.steel.round.SteelRound;
import net.caseif.flint.steel.util.file.DataFiles;
import net.caseif.flint.steel.util.helper.PlayerHelper;

import com.google.common.base.Optional;
import org.bukkit.Bukkit;
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
        Optional<Challenger> ch = CommonCore.getChallenger(uuid);
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

        for (Minigame mg : CommonCore.getMinigames().values()) {
            if (((SteelMinigame) mg).getLobbyWizardManager().isWizardPlayer(event.getPlayer().getUniqueId())) {
                ((SteelMinigame) mg).getLobbyWizardManager().removePlayer(event.getPlayer().getUniqueId());
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!SteelCore.SPECTATOR_SUPPORT) {
            for (Minigame mg : SteelCore.getMinigames().values()) {
                for (Challenger ch : mg.getChallengers()) {
                    if (ch.isSpectating()) {
                        ((SteelChallenger) ch).tryHide(Bukkit.getPlayer(ch.getUniqueId()), event.getPlayer());
                    }
                }
            }
        }

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
                    } catch (IllegalArgumentException | InvalidConfigurationException | IOException ex) {
                        SteelCore.logSevere("Failed to pop inventory for player " + player.getName());
                        ex.printStackTrace();
                    }

                    players.remove(uuid.toString());
                    yaml.set(OFFLINE_PLAYER_LIST_KEY, players);
                    yaml.save(offlinePlayers);
                    try {
                        PlayerHelper.popLocation(player);
                    } catch (IllegalArgumentException | InvalidConfigurationException | IOException ex) {
                        SteelCore.logSevere("Failed to pop location for player " + player.getName());
                        ex.printStackTrace();
                    }
                }
            }
        } catch (InvalidConfigurationException | IOException ex) {
            ex.printStackTrace();
            SteelCore.logSevere("Failed to load offline player data");
        }
    }

}
