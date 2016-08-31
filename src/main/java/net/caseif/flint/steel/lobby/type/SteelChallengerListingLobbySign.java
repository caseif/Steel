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
import net.caseif.flint.steel.lobby.SteelLobbySign;
import net.caseif.flint.util.physical.Location3D;

/**
 * Implements {@link ChallengerListingLobbySign}.
 *
 * @author Max Roncacé
 */
public class SteelChallengerListingLobbySign extends SteelLobbySign implements ChallengerListingLobbySign {

    private final int index;

    public SteelChallengerListingLobbySign(Location3D location, CommonArena arena, int index) {
        super(location, arena, Type.CHALLENGER_LISTING);
        this.index = index;
    }

    @Override
    public Type getType() {
        return Type.CHALLENGER_LISTING;
    }

    @Override
    public int getIndex() throws OrphanedComponentException {
        checkState();
        return index;
    }

}
