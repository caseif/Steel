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

package net.caseif.flint.steel.lobby.populator;

import net.caseif.flint.common.lobby.populator.StockStatusLobbySignPopulator;
import net.caseif.flint.lobby.LobbySign;

import org.bukkit.ChatColor;

public class RichStockStatusLobbySignPopulator extends StockStatusLobbySignPopulator {

    private static final ChatColor[] STATUS_COLORS = new ChatColor[] {ChatColor.DARK_AQUA, ChatColor.DARK_PURPLE,
            ChatColor.DARK_PURPLE, ChatColor.DARK_BLUE};

    public String first(LobbySign sign) {
        return STATUS_COLORS[0] + super.first(sign);
    }

    public String second(LobbySign sign) {
        return STATUS_COLORS[1] + super.second(sign);
    }

    public String third(LobbySign sign) {
        return STATUS_COLORS[2] + super.third(sign);
    }

    public String fourth(LobbySign sign) {
        return STATUS_COLORS[3] + super.fourth(sign);
    }

}
