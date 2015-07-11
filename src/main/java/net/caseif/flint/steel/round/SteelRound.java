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
package net.caseif.flint.steel.round;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.CommonArena;
import net.caseif.flint.common.event.service.EventDispatcher;
import net.caseif.flint.common.round.CommonRound;
import net.caseif.flint.exception.round.RoundJoinException;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;
import net.caseif.flint.steel.SteelMinigame;
import net.caseif.flint.steel.challenger.SteelChallenger;
import net.caseif.flint.steel.event.challenger.SteelChallengerJoinRoundEvent;
import net.caseif.flint.steel.event.challenger.SteelChallengerLeaveRoundEvent;
import net.caseif.flint.steel.event.round.SteelRoundTimerChangeEvent;
import net.caseif.flint.steel.event.round.SteelRoundTimerStartEvent;
import net.caseif.flint.steel.event.round.SteelRoundTimerStopEvent;
import net.caseif.flint.steel.util.PlayerUtil;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;
import java.util.UUID;

/**
 * Implements {@link Round}.
 *
 * @author Max Roncacé
 */
public class SteelRound extends CommonRound {

    private int schedulerHandle = -1;

    public SteelRound(CommonArena arena, ImmutableSet<LifecycleStage> stages) {
        super(arena, stages);
    }

    @Override
    public Challenger addChallenger(UUID uuid) throws RoundJoinException {
        if (Bukkit.getPlayer(uuid) == null) {
            throw new RoundJoinException(uuid, this, RoundJoinException.Reason.OFFLINE, "Player is offline");
        }
        SteelChallenger challenger = new SteelChallenger(uuid, this);
        SteelChallengerJoinRoundEvent event = new SteelChallengerJoinRoundEvent(challenger);
        EventDispatcher.dispatchEvent(event);
        if (event.isCancelled()) {
            throw new RoundJoinException(uuid, this, RoundJoinException.Reason.CANCELLED, "Event was cancelled");
        }
        try {
            PlayerUtil.pushInventory(Bukkit.getPlayer(uuid));
        } catch (IOException ex) {
            throw new RoundJoinException(uuid, this, "Could not push player inventory into persistent storage", ex);
        }
        getChallengerMap().put(uuid, challenger);
        return challenger;
    }

    @Override
    public void removeChallenger(Challenger challenger) {
        removeChallenger(challenger, false);
    }

    /**
     * Removes the given {@link Challenger} from this {@link SteelRound}, taking
     * note as to whether they are currently disconnecting from the server.
     *
     * @param challenger The {@link Challenger} to remove
     * @param isDisconnecting Whether the {@link Challenger} is currently
     *     disconnecting from the server
     */
    public void removeChallenger(Challenger challenger, boolean isDisconnecting) {
        SteelChallengerLeaveRoundEvent event = new SteelChallengerLeaveRoundEvent(challenger);
        EventDispatcher.dispatchEvent(event);
        if (!event.isCancelled()) {
            super.removeChallenger(challenger);
            if (!isDisconnecting) {
                try {
                    PlayerUtil.popInventory(Bukkit.getPlayer(challenger.getUniqueId()));
                } catch (InvalidConfigurationException | IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    @Override
    public void startTimer() {
        if (!isTimerTicking()) {
            SteelRoundTimerStartEvent event = new SteelRoundTimerStartEvent(this);
            EventDispatcher.dispatchEvent(event);
            if (event.isCancelled()) {
                return;
            }
            schedulerHandle = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                    ((SteelMinigame) getMinigame()).getBukkitPlugin(),
                    new RoundWorker(this),
                    0L,
                    20L
            );
        }
    }

    @Override
    public void stopTimer() {
        if (isTimerTicking()) {
            SteelRoundTimerStopEvent event = new SteelRoundTimerStopEvent(this);
            EventDispatcher.dispatchEvent(event);
            if (event.isCancelled()) {
                return;
            }
            Bukkit.getScheduler().cancelTask(schedulerHandle);
        }
    }

    @Override
    public boolean isTimerTicking() {
        return schedulerHandle >= 0;
    }

    @Override
    public void setTime(long time) {
        setTime(time, true);
    }

    /**
     * Sets the time of this {@link Round}.
     *
     * @param time The new time of the {@link Round}
     * @param callEvent Whether an event should be fired
     */
    public void setTime(long time, boolean callEvent) {
        if (callEvent) {
            SteelRoundTimerChangeEvent event = new SteelRoundTimerChangeEvent(this, this.getTime(), time);
            EventDispatcher.dispatchEvent(event);
        }
        super.setTime(time);
    }

}
