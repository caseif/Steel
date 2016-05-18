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
package net.caseif.flint.steel.util.agent;

import static com.google.common.base.Preconditions.checkArgument;
import net.caseif.flint.common.util.agent.chat.IChatAgent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Platform implementation of {@link IChatAgent}.
 */
public class ChatAgent implements IChatAgent {

    @Override
    public void processAndSend(UUID recipient, String message) {
        // we can basically just send the message with the legacy codes
        getPlayer(recipient).sendMessage(message);
    }

    @Override
    public void processAndSend(UUID recipient, String... message) {
        getPlayer(recipient).sendMessage(message);
    }

    private Player getPlayer(UUID uuid) {
        Player pl = Bukkit.getPlayer(uuid);
        checkArgument(pl != null, "Cannot find player with given UUID");
        return pl;
    }

}
