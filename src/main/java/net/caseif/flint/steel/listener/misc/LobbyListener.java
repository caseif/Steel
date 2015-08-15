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
package net.caseif.flint.steel.listener.misc;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.event.lobby.CommonPlayerClickLobbySignEvent;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.event.lobby.PlayerClickLobbySignEvent;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.util.helper.LocationHelper;
import net.caseif.flint.util.physical.Location3D;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Listener for lobby-related events.
 */
public class LobbyListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getState() instanceof Sign) {
            Location3D loc = LocationHelper.convertLocation(event.getBlock().getLocation());
            for (Minigame mg : SteelCore.getMinigames().values()) {
                for (Arena arena : mg.getArenas()) {
                    if (arena.getLobbySignAt(loc).isPresent()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock().getState() instanceof Sign) {
                Location3D loc = LocationHelper.convertLocation(event.getClickedBlock().getLocation());
                for (Minigame mg : SteelCore.getMinigames().values()) {
                    for (Arena arena : mg.getArenas()) {
                        if (arena.getLobbySignAt(loc).isPresent()) { // location contains lobby sign
                            if (event.getAction() == Action.LEFT_CLICK_BLOCK
                                    && (event.getPlayer().isSneaking()
                                    || !mg.getConfigValue(ConfigNode.REQUIRE_SNEAK_TO_DESTROY_LOBBY))) {
                                if (event.getPlayer().hasPermission(mg.getPlugin() + ".lobby.destroy")) {
                                    arena.getLobbySignAt(loc).get().unregister();
                                    return;
                                }
                                mg.getEventBus().post(new CommonPlayerClickLobbySignEvent(
                                        event.getPlayer().getUniqueId(),
                                        arena.getLobbySignAt(loc).get(),
                                        event.getAction() == Action.LEFT_CLICK_BLOCK
                                                ? PlayerClickLobbySignEvent.ClickType.LEFT
                                                : PlayerClickLobbySignEvent.ClickType.RIGHT
                                ));
                            }
                        }
                    }
                }
            }
        }
    }
}
