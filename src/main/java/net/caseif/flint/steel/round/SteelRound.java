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

import net.caseif.flint.Minigame;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.CommonArena;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.event.challenger.CommonChallengerJoinRoundEvent;
import net.caseif.flint.common.event.challenger.CommonChallengerLeaveRoundEvent;
import net.caseif.flint.common.event.round.CommonRoundTimerChangeEvent;
import net.caseif.flint.common.event.round.CommonRoundTimerStartEvent;
import net.caseif.flint.common.event.round.CommonRoundTimerStopEvent;
import net.caseif.flint.common.round.CommonRound;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.event.Cancellable;
import net.caseif.flint.event.FlintEvent;
import net.caseif.flint.exception.round.RoundJoinException;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;
import net.caseif.flint.steel.SteelMinigame;
import net.caseif.flint.steel.challenger.SteelChallenger;
import net.caseif.flint.steel.util.MiscUtil;
import net.caseif.flint.steel.util.PlayerUtil;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements {@link Round}.
 *
 * @author Max Roncacé
 */
public class SteelRound extends CommonRound {

    private int schedulerHandle = -1;
    private boolean timerTicking = false;

    private AtomicInteger nextSpawn = new AtomicInteger();

    public SteelRound(CommonArena arena, ImmutableSet<LifecycleStage> stages) {
        super(arena, stages);
        schedulerHandle = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                ((SteelMinigame) getMinigame()).getBukkitPlugin(),
                new RoundWorker(this),
                0L,
                20L
        );
    }

    @Override
    public Challenger addChallenger(UUID uuid) throws RoundJoinException {
        Player bukkitPlayer = Bukkit.getPlayer(uuid);
        if (bukkitPlayer == null) {
            throw new RoundJoinException(uuid, this, RoundJoinException.Reason.OFFLINE,
                    "Cannot enter challenger with UUID " + uuid.toString() + "(Player is offline)");
        }

        if (getChallengers().size() >= getConfigValue(ConfigNode.MAX_PLAYERS)) {
            throw new RoundJoinException(uuid, this, RoundJoinException.Reason.FULL,
                    "Cannot enter challenger " + bukkitPlayer.getName() + " (Round is full)");
        }

        for (Minigame mg : CommonCore.getMinigames().values()) {
            for (Challenger c : mg.getChallengers()) {
                if (c.getUniqueId().equals(uuid)) {
                    throw new RoundJoinException(uuid, this, RoundJoinException.Reason.ALREADY_ENTERED,
                            "Cannot enter challenger " + bukkitPlayer.getName() + " (Already in a round)");
                }
            }
        }

        SteelChallenger challenger = new SteelChallenger(uuid, this);
        CommonChallengerJoinRoundEvent event = new CommonChallengerJoinRoundEvent(challenger);
        getMinigame().getEventBus().post(event);
        if (event.isCancelled()) {
            throw new RoundJoinException(uuid, this, RoundJoinException.Reason.CANCELLED,
                    "Cannot enter challenger " + bukkitPlayer.getName() + " (Event was cancelled)");
        }

        try {
            PlayerUtil.pushInventory(bukkitPlayer);
        } catch (IOException ex) {
            throw new RoundJoinException(uuid, this, "Could not push inventory for player " + challenger.getName()
                    + " into persistent storage", ex);
        }
        try {
            PlayerUtil.storeLocation(bukkitPlayer);
        } catch (IllegalArgumentException | InvalidConfigurationException | IOException ex) {
            throw new RoundJoinException(uuid, this, "Could not push location for player " + challenger.getName()
                    + " into persistent storage", ex);
        }

        int spawnIndex = getConfigValue(ConfigNode.RANDOM_SPAWNING)
                ? (int)Math.floor(Math.random() * getArena().getSpawnPoints().size())
                : nextSpawn.getAndIncrement();
        if (nextSpawn.intValue() == getArena().getSpawnPoints().size()) {
            nextSpawn.set(0);
        }
        bukkitPlayer.teleport(MiscUtil.convertLocation(getArena().getSpawnPoints().get(spawnIndex)));
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
        CommonChallengerLeaveRoundEvent event = new CommonChallengerLeaveRoundEvent(challenger);
        getMinigame().getEventBus().post(event);
        super.removeChallenger(challenger);
        Player bukkitPlayer = Bukkit.getPlayer(challenger.getUniqueId());
        if (!isDisconnecting) {
            try {
                PlayerUtil.popInventory(bukkitPlayer);
            } catch (InvalidConfigurationException | IOException ex) {
                throw new RuntimeException("Could not pop inventory for player " + challenger.getName()
                        + " from persistent storage", ex);
            }
            try {
                PlayerUtil.popLocation(bukkitPlayer);
            } catch (IllegalArgumentException | InvalidConfigurationException | IOException ex) {
                ex.printStackTrace();
                System.err.println("Could not pop location for player " + challenger.getName()
                        + " from persistent storage - defaulting to world spawn");
                bukkitPlayer.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }
        }
    }

    @Override
    public boolean isTimerTicking() {
        return this.timerTicking;
    }

    @Override
    public void setTimerTicking(boolean ticking) {
        if (ticking != isTimerTicking()) {
            Cancellable event = ticking ? new CommonRoundTimerStartEvent(this) : new CommonRoundTimerStopEvent(this);
            getMinigame().getEventBus().post(event);
            if (event.isCancelled()) {
                return;
            }
            timerTicking = ticking;
        }
    }

    @Override
    public void end(boolean rollback, boolean natural) {
        cancelTimerTask();
        super.end(rollback, natural);
    }

    public void cancelTimerTask() {
        Bukkit.getScheduler().cancelTask(schedulerHandle);
    }

}
