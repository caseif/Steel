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
import net.caseif.flint.common.lobby.wizard.IWizardManager;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.minigame.SteelMinigame;
import net.caseif.flint.steel.util.helper.ChatHelper;
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
            Optional<Challenger> challenger = CommonCore.getChallenger(event.getPlayer().getUniqueId());
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
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // iterate minigames
        for (Minigame mg : SteelCore.getMinigames().values()) {
            // get the wizard manager for the minigame
            IWizardManager wm = ((SteelMinigame) mg).getLobbyWizardManager();
            // check if the player is in a wizard
            if (wm.hasPlayer(event.getPlayer().getUniqueId())) {
                event.setCancelled(true); // cancel the event
                // send the original message for reference
                event.getPlayer().sendMessage("<" + event.getPlayer().getDisplayName() + "> " + event.getMessage());
                // feed the message to the wizard manager and get the response
                String[] response = wm.accept(event.getPlayer().getUniqueId(), event.getMessage());
                event.getPlayer().sendMessage(response); // pass the response on to the player
                return; // no need to do any more checks for the event
            }

            Iterator<Player> it = event.getRecipients().iterator();
            while (it.hasNext()) {
                Player recip = it.next();

                if (((SteelMinigame) mg).getLobbyWizardManager().hasPlayer(recip.getUniqueId())) {
                    ((SteelMinigame) mg).getLobbyWizardManager().withholdMessage(recip.getUniqueId(),
                            event.getPlayer().getDisplayName(), event.getMessage());
                    it.remove();
                    continue;
                }

                if (ChatHelper.isBarrierPresent(event.getPlayer(), recip)) {
                    it.remove();
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
            Optional<Challenger> challenger = CommonCore.getChallenger(event.getEntity().getUniqueId());
            Optional<Challenger> damager = CommonCore.getChallenger(event.getDamager().getUniqueId());
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
                            cancelled = true;
                        }
                    }
                } else {
                    cancelled = true;
                }
            } else if (challenger.isPresent() != damager.isPresent()) {
                // cancel if one's in a round and one's not
                cancelled = true;
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
            if (CommonCore.getChallenger(uuid).isPresent()) {
                //TODO: figure out a better way to solve this than by disabling it
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED
                        + "You may not run this command while in a minigame round");
                return;
            }
        }

        Optional<Challenger> ch = CommonCore.getChallenger(event.getPlayer().getUniqueId());
        if (ch.isPresent()) {
            if (ch.get().getRound().getConfigValue(ConfigNode.FORBIDDEN_COMMANDS)
                    .contains(event.getMessage().split(" ")[0].substring(1))) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED
                        + "You may not run this command while in a minigame round");
            }
        }
    }

    private void processEvent(Cancellable event, Player player) {
        if (!SteelCore.SPECTATOR_SUPPORT) {
            Optional<Challenger> ch = CommonCore.getChallenger(player.getUniqueId());
            if (ch.isPresent() && ch.get().isSpectating()) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
