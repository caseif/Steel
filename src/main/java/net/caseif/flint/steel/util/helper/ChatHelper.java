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

package net.caseif.flint.steel.util.helper;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.steel.SteelCore;

import com.google.common.base.Optional;
import org.bukkit.entity.Player;

/**
 * Static utility class for chat-related functionality.
 *
 * @author Max Roncacé
 */
public class ChatHelper {

    public static  boolean isBarrierPresent(Player sender, Player recipient) {
        return isRoundBarrierPresent(sender, recipient)
                || isTeamBarrierPresent(sender, recipient)
                || isSpectatorBarrierPresent(sender, recipient);
    }

    public static boolean isRoundBarrierPresent(Player sender, Player recipient) {
        Optional<Challenger> senderCh = SteelCore.getChallenger(sender.getUniqueId());
        Optional<Challenger> recipCh = SteelCore.getChallenger(recipient.getUniqueId());

        if (checkRoundBarrier(senderCh) || checkRoundBarrier(recipCh)) {
            if (senderCh.isPresent() != recipCh.isPresent() || senderCh.get().getRound() != recipCh.get().getRound()) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkRoundBarrier(Optional<Challenger> ch) {
        return ch.isPresent() && ch.get().getRound().getConfigValue(ConfigNode.SEPARATE_ROUND_CHATS);
    }

    public static boolean isTeamBarrierPresent(Player sender, Player recipient) {
        Optional<Challenger> senderCh = SteelCore.getChallenger(sender.getUniqueId());
        Optional<Challenger> recipCh = SteelCore.getChallenger(recipient.getUniqueId());

        if (senderCh.isPresent() && recipCh.isPresent()) {
            if (senderCh.get().getRound() == recipCh.get().getRound()) {
                if (senderCh.get().getRound().getConfigValue(ConfigNode.SEPARATE_TEAM_CHATS)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isSpectatorBarrierPresent(Player sender, Player recipient) {
        Optional<Challenger> senderCh = SteelCore.getChallenger(sender.getUniqueId());
        Optional<Challenger> recipCh = SteelCore.getChallenger(recipient.getUniqueId());

        if (senderCh.isPresent()) {
            if (senderCh.get().isSpectating()
                    && senderCh.get().getRound().getConfigValue(ConfigNode.WITHHOLD_SPECTATOR_CHAT)) {
                return !(recipCh.isPresent() && recipCh.get().getRound() == senderCh.get().getRound()
                        && recipCh.get().isSpectating());
            }
        }
        return false;
    }

}