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
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.lobby.wizard.WizardManager;
import net.caseif.flint.steel.minigame.SteelMinigame;
import net.caseif.flint.steel.util.helper.LocationHelper;
import net.caseif.flint.util.physical.Boundary;

import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.Iterator;
import java.util.UUID;

/**
 * Listener for events relating to players in the world.
 *
 * @author Max Roncacé
 */
public class PlayerWorldListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // iterate minigames
        for (Minigame mg : SteelCore.getMinigames().values()) {
            // get the wizard manager for the minigame
            WizardManager wm = ((SteelMinigame) mg).getLobbyWizardManager();
            // check if the player is in a wizard
            if (wm.isWizardPlayer(event.getPlayer().getUniqueId())) {
                event.setCancelled(true); // cancel the event
                // send the original message for reference
                event.getPlayer().sendMessage("<" + event.getPlayer().getDisplayName() + "> " + event.getMessage());
                // feed the message to the wizard manager and get the response
                String[] response = wm.accept(event.getPlayer().getUniqueId(), event.getMessage());
                event.getPlayer().sendMessage(response); // pass the response on to the player
                return; // no need to do any more checks for the event
            }

            Optional<Challenger> challenger = mg.getChallenger(event.getPlayer().getUniqueId());

            Iterator<Player> it = event.getRecipients().iterator();
            while (it.hasNext()) {
                Player recip = it.next();
                Optional<Challenger> rChal = mg.getChallenger(recip.getUniqueId());

                if (((SteelMinigame) mg).getLobbyWizardManager().isWizardPlayer(recip.getUniqueId())) {
                    ((SteelMinigame) mg).getLobbyWizardManager().withholdMessage(recip.getUniqueId(),
                            event.getPlayer().getDisplayName(), event.getMessage());
                    it.remove();
                    continue;
                }

                // here be dragons

                // in all seriousness I commented this section best I could, but the logic can be really confusing

                // checks for separating round chats
                if (challenger.get().getRound().getConfigValue(ConfigNode.SEPARATE_ROUND_CHATS)) {
                    if (challenger.isPresent() != rChal.isPresent()) { // one's in a round, one's not
                        it.remove();
                        continue;
                    } else if (challenger.isPresent() && rChal.isPresent() // redundant rChal check for clarity
                            && challenger.get().getRound() != rChal.get().getRound()) { // they're in different rounds
                        it.remove();
                        continue;
                    }
                }

                // checks for separating spectator chat
                if (challenger.get().getRound().getConfigValue(ConfigNode.WITHHOLD_SPECTATOR_CHAT)) {
                    if (challenger.isPresent() && challenger.get().isSpectating()) {
                        // this is an anti-conditional: if any of the checks fail, the message is withheld
                        if (!(rChal.isPresent() // must be in A round to receive
                                && rChal.get().getRound() == challenger.get().getRound()) // must be in same round
                                && rChal.get().isSpectating()) { // must be spectating to receive message
                            // if we're here, one of the checks failed and the message must be withheld
                            it.remove();
                        }
                    }
                }
            }

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        boolean cancelled = false;
        // check that both parties involved are playes
        if (event.getEntity().getType() == EntityType.PLAYER && event.getDamager().getType() == EntityType.PLAYER) {
            // begin the hunt for the challenger
            for (Minigame mg : SteelCore.getMinigames().values()) {
                Optional<Challenger> challenger = mg.getChallenger(event.getEntity().getUniqueId());
                Optional<Challenger> damager = mg.getChallenger(event.getDamager().getUniqueId());

                // cancel if one of them is spectating
                if ((challenger.isPresent() && challenger.get().isSpectating())
                        || (damager.isPresent() && damager.get().isSpectating())) {
                    event.setCancelled(true);
                    return;
                }

                // check whether the player is in a round for this minigame
                if (challenger.isPresent() && damager.isPresent()) {
                    // check whether they're in the same round
                    if (challenger.get().getRound() == damager.get().getRound()) {
                        // check whether damage is disabled entirely
                        if (!challenger.get().getRound().getConfigValue(ConfigNode.ALLOW_DAMAGE)) {
                            cancelled = true;
                        } else if (!challenger.get().getRound().getConfigValue(ConfigNode.ALLOW_FRIENDLY_FIRE)) {
                            // check whether friendly fire is disabled
                            // check if they're on the same team
                            if (challenger.get().getTeam().orNull() == damager.get().getTeam().orNull()) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                    } else {
                        event.setCancelled(true);
                        return;
                    }
                } else if (challenger.isPresent() != damager.isPresent()) {
                    // cancel if one's in a round and one's not
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (cancelled) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        processEvent(event, event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        processEvent(event, event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null
                || !(event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getClickedBlock().getState() instanceof InventoryHolder)) {
            processEvent(event, event.getPlayer());
        }
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER) {
            processEvent(event, (Player) event.getEntity());
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent event) {
        if (event.getInventory().getHolder() instanceof Block) {
            processEvent(event, (Player) event.getWhoClicked());
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("/suicide") || event.getMessage().startsWith("/kill")) {
            UUID uuid;
            @SuppressWarnings("deprecation")
            Player pl = event.getMessage().startsWith("/kill ")
                    ? Bukkit.getPlayer(event.getMessage().split(" ")[1])
                    : event.getPlayer();
            if (pl == null) {
                return;
            } else {
                uuid = pl.getUniqueId();
            }
            for (Minigame mg : SteelCore.getMinigames().values()) {
                if (mg.getChallenger(uuid).isPresent()) {
                    //TODO: figure out a better way to solve this than by disabling it
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED
                            + "You may not run this command while in a minigame round");
                }
            }
        }
    }

    private void processEvent(Cancellable event, Player player) {
        if (!SteelCore.SPECTATOR_SUPPORT) {
            for (Minigame mg : SteelCore.getMinigames().values()) {
                Optional<Challenger> ch = mg.getChallenger(player.getUniqueId());
                if (ch.isPresent() && ch.get().isSpectating()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

}
