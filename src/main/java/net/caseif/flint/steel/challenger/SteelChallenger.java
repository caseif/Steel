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
package net.caseif.flint.steel.challenger;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.challenger.CommonChallenger;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.round.SteelRound;
import net.caseif.flint.steel.util.helper.PlayerHelper;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implements {@link Challenger}.
 *
 * @author Max Roncacé
 */
public class SteelChallenger extends CommonChallenger {

    private GameMode prevGameMode;
    private boolean hadFlight;
    private List<UUID> alreadyInvisibleTo = new ArrayList<>();

    public SteelChallenger(UUID uuid, SteelRound round) {
        super(uuid, Bukkit.getPlayer(uuid).getName(), round);
    }

    @Override
    public void setSpectating(boolean spectating) {
        super.setSpectating(spectating);
        Player pl = Bukkit.getPlayer(getUniqueId());
        assert pl != null;
        if (spectating) {
            prevGameMode = pl.getGameMode();
            if (SteelCore.SPECTATOR_SUPPORT) {
                pl.setGameMode(GameMode.SPECTATOR);
            } else {
                pl.setGameMode(GameMode.ADVENTURE);
                for (Player p : PlayerHelper.getOnlinePlayers()) {
                    tryHide(pl, p);
                }
                hadFlight = pl.getAllowFlight();
                pl.setAllowFlight(true);
            }
        } else {
            if (prevGameMode != null) {
                pl.setGameMode(prevGameMode);
                prevGameMode = null;
            }
            if (!SteelCore.SPECTATOR_SUPPORT) {
                for (Player p : PlayerHelper.getOnlinePlayers()) {
                    if (!alreadyInvisibleTo.contains(p.getUniqueId())) {
                        p.showPlayer(pl);
                    }
                    alreadyInvisibleTo.clear();
                }
                pl.setAllowFlight(hadFlight);
                hadFlight = false;
            }
        }
    }

    public void tryHide(Player hidden, Player viewer) {
        if (viewer.canSee(hidden)) {
            viewer.hidePlayer(hidden);
        } else {
            alreadyInvisibleTo.add(viewer.getUniqueId());
        }
    }

}
