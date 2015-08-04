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
package net.caseif.flint.steel.listener.block;

import net.caseif.flint.Minigame;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.arena.SteelArena;
import net.caseif.flint.steel.util.MiscUtil;

import com.google.common.base.Optional;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Listener for events logged by the rollback engine.
 */
public class RollbackListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        for (Minigame mg : SteelCore.getMinigames().values()) {
            Optional<Challenger> challenger = mg.getChallenger(event.getPlayer().getUniqueId());
            if (challenger.isPresent()) {
                if (challenger.get().getRound().getArena().getWorld().equals(event.getBlock().getWorld().getName())) {
                    if (challenger.get().getRound().getArena().getBoundary().contains(
                            MiscUtil.convertLocation(event.getBlock().getLocation()))) {
                        try {
                            ((SteelArena)challenger.get().getRound().getArena()).getRollbackHelper().logBlockChange(
                                    event.getBlock().getLocation(),
                                    event.getBlock().getState()
                            );
                        } catch (InvalidConfigurationException | IOException | SQLException ex) {
                            throw new RuntimeException("Failed to log block break in arena "
                                    + challenger.get().getRound().getArena().getName(), ex);
                        }
                    }
                }
            }
        }
    }

}
