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
import net.caseif.flint.component.exception.OrphanedComponentException;
import net.caseif.flint.lobby.type.ChallengerListingLobbySign;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.SteelMain;
import net.caseif.flint.steel.lobby.SteelLobbySign;
import net.caseif.flint.util.physical.Location3D;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

/**
 * Implements {@link ChallengerListingLobbySign}.
 *
 * @author Max Roncacé
 */
public class SteelChallengerListingLobbySign extends SteelLobbySign implements ChallengerListingLobbySign {

    private final int index;

    public SteelChallengerListingLobbySign(Location3D location, CommonArena arena, int index) {
        super(location, arena);
        this.index = index;
    }

    @Override
    public Type getType() {
        return Type.CHALLENGER_LISTING;
    }

    @Override
    public void update() {
        Block b = getBlock();
        if (!(b.getState() instanceof Sign)) {
            // hehe, illegal "state"
            unregister();
            SteelCore.logWarning("Cannot update lobby sign at ("
                    + "\"" + b.getWorld().getName() + "\", " + b.getX() + ", " + b.getY() + ", " + b.getZ()
                    + "): not a sign. Removing...");
        }
        final Sign sign = (Sign) b.getState();
        int startIndex = getIndex() * sign.getLines().length;
        boolean round = getArena().getRound().isPresent();
        for (int i = 0; i < sign.getLines().length; i++) {
            if (round && startIndex + i < getArena().getRound().get().getChallengers().size()) {
                sign.setLine(i, getArena().getRound().get().getChallengers().get(startIndex + i).getName());
            } else {
                sign.setLine(i, "");
            }
        }
        Bukkit.getScheduler().runTask(SteelMain.getInstance(), new Runnable() {
            public void run() {
                sign.update(true);
            }
        });
    }

    @Override
    public int getIndex() throws OrphanedComponentException {
        checkState();
        return index;
    }

}
