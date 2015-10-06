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
package net.caseif.flint.steel.lobby.type;

import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.lobby.type.StatusLobbySign;
import net.caseif.flint.steel.SteelMain;
import net.caseif.flint.steel.lobby.SteelLobbySign;
import net.caseif.flint.util.physical.Location3D;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

/**
 * Implements {@link StatusLobbySign}.
 */
public class SteelStatusLobbySign extends SteelLobbySign implements StatusLobbySign {

    private static ChatColor ARENA_COLOR = ChatColor.DARK_AQUA;
    private static ChatColor LIFECYCLE_STAGE_COLOR = ChatColor.DARK_PURPLE;
    private static ChatColor TIMER_COLOR = ChatColor.DARK_PURPLE;
    private static ChatColor PLAYER_COUNT_COLOR = ChatColor.DARK_BLUE;

    public SteelStatusLobbySign(Location3D location, CommonArena arena) {
        super(location, arena);
    }

    @Override
    public Type getType() {
        return Type.STATUS;
    }

    @Override
    public void update() {
        Block b = getBlock();
        if (!(b.getState() instanceof Sign)) {
            unregister();
            throw new IllegalStateException("Cannot update lobby sign: not a sign. Removing...");
        }
        final Sign sign = (Sign) b.getState();
        sign.setLine(0, ARENA_COLOR + getArena().getName());
        if (getArena().getRound().isPresent()) {
            sign.setLine(1,
                    LIFECYCLE_STAGE_COLOR + getArena().getRound().get().getLifecycleStage().getId().toUpperCase());
            long seconds = getArena().getRound().get().getRemainingTime() != -1
                    ? getArena().getRound().get().getRemainingTime()
                    : getArena().getRound().get().getTime();
            String time = seconds / 60 + ":" + (seconds % 60 >= 10 ? seconds % 60 : "0" + seconds % 60);
            sign.setLine(2, TIMER_COLOR + time);
            // get max player count
            int maxPlayers = getArena().getRound().get().getConfigValue(ConfigNode.MAX_PLAYERS);
            // format player count relative to max
            String players = getArena().getRound().get().getChallengers().size() + "/"
                    + (maxPlayers > 0 ? maxPlayers : "∞");
            // add label to player count (shortened version used if the full one won't fit)
            players += players.length() <= 5 ? " players" : (players.length() <= 7 ? " plyrs" : "");
            sign.setLine(3, PLAYER_COUNT_COLOR + players);
        } else {
            for (int i = 1; i < 4; i++) {
                sign.setLine(i, "");
            }
        }
        Bukkit.getScheduler().runTask(SteelMain.getInstance(), new Runnable() {
            public void run() {
                sign.update(true);
            }
        });
    }

}
