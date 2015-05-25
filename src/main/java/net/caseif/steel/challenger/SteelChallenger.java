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
package net.caseif.steel.challenger;

import net.caseif.steel.event.challenger.SteelChallengerJoinRoundEvent;
import net.caseif.steel.round.SteelRound;
import net.caseif.steel.util.MiscUtil;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.challenger.CommonChallenger;
import net.caseif.flint.exception.round.RoundJoinException;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * Implements {@link Challenger}.
 *
 * @author Max Roncacé
 */
public class SteelChallenger extends CommonChallenger {

    public SteelChallenger(UUID uuid, SteelRound round) throws RoundJoinException {
        if (Bukkit.getPlayer(uuid) == null) {
            throw new RoundJoinException(uuid, round, RoundJoinException.Reason.OFFLINE, "Player is offline");
        }
        this.uuid = uuid;
        this.name = Bukkit.getPlayer(uuid).getName();
        this.round = round;
        SteelChallengerJoinRoundEvent event = new SteelChallengerJoinRoundEvent(this);
        MiscUtil.callEvent(event);
        if (event.isCancelled()) {
            throw new RoundJoinException(uuid, round, RoundJoinException.Reason.CANCELLED, "Event was cancelled");
        }
        round.getChallengerMap().put(uuid, this);
    }
}
