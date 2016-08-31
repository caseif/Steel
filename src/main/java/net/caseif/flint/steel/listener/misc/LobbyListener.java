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

package net.caseif.flint.steel.listener.misc;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.event.lobby.CommonPlayerClickLobbySignEvent;
import net.caseif.flint.common.lobby.wizard.IWizardManager;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.event.lobby.PlayerClickLobbySignEvent;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.minigame.SteelMinigame;
import net.caseif.flint.steel.util.helper.LocationHelper;
import net.caseif.flint.util.physical.Location3D;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

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
                                if (event.getPlayer().hasPermission(mg.getPlugin() + ".lobby.destroy")
                                        || event.getPlayer().hasPermission(mg.getPlugin() + ".lobby.*")) {
                                    arena.getLobbySignAt(loc).get().unregister();
                                    return;
                                }
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {
        for (Map.Entry<String, Minigame> e : SteelCore.getMinigames().entrySet()) {
            if (event.getLine(0).equalsIgnoreCase("[" + e.getKey() + "]")) {
                if (e.getValue().getConfigValue(ConfigNode.ENABLE_LOBBY_WIZARD)) {
                    if (event.getPlayer().hasPermission(e.getKey() + ".lobby.create")
                            || event.getPlayer().hasPermission(e.getKey() + ".lobby.*")) {
                        IWizardManager wm = ((SteelMinigame) e.getValue()).getLobbyWizardManager();
                        if (!wm.hasPlayer(event.getPlayer().getUniqueId())) {
                            wm.addPlayer(event.getPlayer().getUniqueId(),
                                    LocationHelper.convertLocation(event.getBlock().getLocation()));
                        } else {
                            event.getPlayer().sendMessage(ChatColor.RED + "You are already in a lobby sign wizard");
                        }
                    } else {
                        event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to do this");
                    }
                }
                return;
            }
        }
    }

}
