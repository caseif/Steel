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
package net.caseif.steel.round;

import net.caseif.steel.challenger.SteelChallenger;
import net.caseif.steel.event.challenger.SteelChallengerJoinRoundEvent;
import net.caseif.steel.event.challenger.SteelChallengerLeaveRoundEvent;
import net.caseif.steel.event.round.SteelRoundTimerChangeEvent;
import net.caseif.steel.event.round.SteelRoundTimerStartEvent;
import net.caseif.steel.event.round.SteelRoundTimerStopEvent;
import net.caseif.steel.util.MiscUtil;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.CommonArena;
import net.caseif.flint.common.round.CommonRound;
import net.caseif.flint.exception.round.RoundJoinException;
import net.caseif.flint.round.Round;

import java.util.UUID;

/**
 * Implements {@link Round}.
 *
 * @author Max Roncacé
 */
public class SteelRound extends CommonRound {

    public SteelRound(CommonArena arena) {
        super(arena);
    }

    @Override
    public Challenger addChallenger(UUID uuid) throws RoundJoinException {
        return new SteelChallenger(uuid, this);
    }

    @Override
    public void removeChallenger(Challenger challenger) {
        SteelChallengerLeaveRoundEvent event = new SteelChallengerLeaveRoundEvent(challenger);
        MiscUtil.callEvent(event);
        if (!event.isCancelled()) {
            super.removeChallenger(challenger);
        }
    }

    @Override
    public void startTimer() {
        SteelRoundTimerStartEvent event = new SteelRoundTimerStartEvent(this);
        MiscUtil.callEvent(event);
        if (!event.isCancelled()) {
            super.startTimer();
        }
    }

    @Override
    public void stopTimer() {
        SteelRoundTimerStopEvent event = new SteelRoundTimerStopEvent(this);
        MiscUtil.callEvent(event);
        if (!event.isCancelled()) {
            super.stopTimer();
        }
    }

    @Override
    public void setTime(long time) {
        SteelRoundTimerChangeEvent event = new SteelRoundTimerChangeEvent(this, this.getTime(), time);
        MiscUtil.callEvent(event);
        if (!event.isCancelled()) {
            super.setTime(time);
        }
    }

}
