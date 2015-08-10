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

import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.round.challenger.Challenger;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.util.helper.LocationHelper;
import net.caseif.flint.util.physical.Boundary;

import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Listener for events relating to players in the world.
 *
 * @author Max Roncacé
 */
public class PlayerWorldListener implements Listener {

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // make sure they moved through space
        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {
            // begin the hunt for the challenger
            for (Minigame mg : SteelCore.getMinigames().values()) {
                Optional<Challenger> challenger = mg.getChallenger(event.getPlayer().getUniqueId());
                // check whether the player is in a round for this minigame
                if (challenger.isPresent()) {
                    Boundary bound = challenger.get().getRound().getArena().getBoundary();
                    // check whether the player is teleporting out of the arena boundary
                    if (!bound.contains(LocationHelper.convertLocation(event.getTo()))) {
                        if (challenger.get().getRound().getConfigValue(ConfigNode.ALLOW_EXIT_BOUNDARY)) {
                            challenger.get().removeFromRound();
                        } else {
                            event.setCancelled(true);
                        }
                    }
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        for (Minigame mg : SteelCore.getMinigames().values()) {
            Optional<Challenger> challenger = mg.getChallenger(event.getPlayer().getUniqueId());
            // check whether the player is in a round for this minigame
            if (challenger.isPresent()) {
                // check if separate team chats are configured
                if (challenger.get().getRound().getConfigValue(ConfigNode.SEPARATE_TEAM_CHATS)) {
                    // check if the player is on a team
                    for (Challenger c : challenger.get().getRound().getChallengers()) {
                        if (c.getTeam().orNull() != challenger.get().getTeam().orNull()) {
                            event.getRecipients().remove(Bukkit.getPlayer(c.getUniqueId()));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // check that both parties involved are playes
        if (event.getEntity().getType() == EntityType.PLAYER && event.getDamager().getType() == EntityType.PLAYER) {
            // begin the hunt for the challenger
            for (Minigame mg : SteelCore.getMinigames().values()) {
                Optional<Challenger> challenger = mg.getChallenger(event.getEntity().getUniqueId());
                Optional<Challenger> damager = mg.getChallenger(event.getDamager().getUniqueId());
                // check whether the player is in a round for this minigame
                if (challenger.isPresent() && damager.isPresent()) {
                    // check whether they're in the same round
                    if (challenger.get().getRound() == damager.get().getRound()) {
                        // check whether damage is disabled entirely
                        if (!challenger.get().getRound().getConfigValue(ConfigNode.ALLOW_DAMAGE)) {
                            event.setCancelled(true);
                            return;
                        } else if (!challenger.get().getRound().getConfigValue(ConfigNode.ALLOW_FRIENDLY_FIRE)) {
                            // check whether friendly fire is disabled
                            if (challenger.get().getTeam().orNull() == damager.get().getTeam().orNull()) {
                                event.setCancelled(true); // cancel if they're on the same team
                                return;
                            }
                        }
                    } else {
                        event.setCancelled(true); // cancel if they're not in the same round
                        return;
                    }
                } else if (challenger.isPresent() != damager.isPresent()) {
                    // cancel if one's in a round and one's not
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

}
