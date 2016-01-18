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
