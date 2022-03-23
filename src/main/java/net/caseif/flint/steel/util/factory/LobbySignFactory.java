/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2022, Max Roncace <me@caseif.net>
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

package net.caseif.flint.steel.util.factory;

import static net.caseif.flint.common.lobby.CommonLobbySign.PERSIST_INDEX_KEY;
import static net.caseif.flint.common.lobby.CommonLobbySign.PERSIST_TYPE_KEY;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.util.factory.ILobbySignFactory;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.steel.lobby.SteelLobbySign;
import net.caseif.flint.steel.lobby.type.SteelChallengerListingLobbySign;
import net.caseif.flint.steel.lobby.type.SteelStatusLobbySign;
import net.caseif.flint.util.physical.Location3D;

import com.google.gson.JsonObject;

public class LobbySignFactory implements ILobbySignFactory {

    @Override
    public LobbySign createLobbySign(Location3D location, Arena arena, JsonObject json)
            throws IllegalArgumentException {
        if (json.has(PERSIST_TYPE_KEY)) {
            String type = json.get(PERSIST_TYPE_KEY).getAsString();
            switch (type) {
                case SteelLobbySign.PERSIST_TYPE_STATUS: {
                    return new SteelStatusLobbySign(location, (CommonArena) arena);
                }
                case SteelLobbySign.PERSIST_TYPE_LISTING: {
                    if (!json.has(PERSIST_INDEX_KEY)) {
                        break;
                    }
                    int index = json.get(PERSIST_INDEX_KEY).getAsInt();
                    return new SteelChallengerListingLobbySign(location, (CommonArena) arena, index);
                }
                default: { // continue to IllegalArgumentException
                }
            }
        }
        throw new IllegalArgumentException("Invalid configuration for LobbySign");
    }

}
