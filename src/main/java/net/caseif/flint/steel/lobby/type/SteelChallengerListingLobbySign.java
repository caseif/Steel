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
import net.caseif.flint.exception.OrphanedObjectException;
import net.caseif.flint.lobby.type.ChallengerListingLobbySign;
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
            throw new IllegalStateException("Cannot update lobby sign: not a sign. Removing...");
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
    public int getIndex() throws OrphanedObjectException {
        checkState();
        return index;
    }

}
