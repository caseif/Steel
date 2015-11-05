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