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
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.event.round.CommonRoundTimerStartEvent;
import net.caseif.flint.common.event.round.CommonRoundTimerStopEvent;
import net.caseif.flint.common.event.round.challenger.CommonChallengerJoinRoundEvent;
import net.caseif.flint.common.event.round.challenger.CommonChallengerLeaveRoundEvent;
import net.caseif.flint.common.exception.round.CommonRoundJoinException;
import net.caseif.flint.common.round.CommonRound;
import net.caseif.flint.component.exception.OrphanedComponentException;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.exception.round.RoundJoinException;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.arena.SteelArena;
import net.caseif.flint.steel.challenger.SteelChallenger;
import net.caseif.flint.steel.minigame.SteelMinigame;
import net.caseif.flint.steel.util.helper.LocationHelper;
import net.caseif.flint.steel.util.helper.PlayerHelper;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements {@link Round}.
 *
 * @author Max Roncacé
 */
public class SteelRound extends CommonRound {

    private final int schedulerHandle;
    private boolean timerTicking = true;

    private AtomicInteger nextSpawn = new AtomicInteger();

    public SteelRound(CommonArena arena, ImmutableSet<LifecycleStage> stages) {
        super(arena, stages);
        schedulerHandle = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                ((SteelMinigame) getArena().getMinigame()).getBukkitPlugin(),
                new RoundWorker(this),
                0L,
                20L
        );
        try {
            ((SteelArena) getArena()).getRollbackHelper().createRollbackDatabase();
        } catch (IOException | SQLException ex) {
            SteelCore.logSevere("Failed to create rollback store");
            ex.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("DuplicateThrows")
    public Challenger addChallenger(UUID uuid) throws IllegalStateException, RoundJoinException,
            OrphanedComponentException {
        Player bukkitPlayer = Bukkit.getPlayer(uuid);
        if (bukkitPlayer == null) {
            throw new CommonRoundJoinException(uuid, this, RoundJoinException.Reason.OFFLINE,
                    "Cannot enter challenger with UUID " + uuid.toString() + "(Player is offline)");
        }

        if (getChallengers().size() >= getConfigValue(ConfigNode.MAX_PLAYERS)) {
            throw new CommonRoundJoinException(uuid, this, RoundJoinException.Reason.FULL,
                    "Cannot enter challenger " + bukkitPlayer.getName() + " (Round is full)");
        }

        for (Minigame mg : CommonCore.getMinigames().values()) {
            for (Challenger c : mg.getChallengers()) {
                if (c.getUniqueId().equals(uuid)) {
                    throw new CommonRoundJoinException(uuid, this, RoundJoinException.Reason.ALREADY_ENTERED,
                            "Cannot enter challenger " + bukkitPlayer.getName() + " (Already in a round)");
                }
            }
        }

        SteelChallenger challenger = new SteelChallenger(uuid, this);

        try {
            PlayerHelper.pushInventory(bukkitPlayer);
        } catch (IOException ex) {
            throw new CommonRoundJoinException(uuid, this, ex, "Could not push inventory for player "
                    + challenger.getName() + " into persistent storage");
        }
        try {
            PlayerHelper.storeLocation(bukkitPlayer);
        } catch (IllegalArgumentException | InvalidConfigurationException | IOException ex) {
            throw new CommonRoundJoinException(uuid, this, ex, "Could not push location for player "
                    + challenger.getName() + " into persistent storage");
        }

        int spawnIndex = getConfigValue(ConfigNode.RANDOM_SPAWNING)
                ? (int)Math.floor(Math.random() * getArena().getSpawnPoints().size())
                : nextSpawn.getAndIncrement();
        if (nextSpawn.intValue() == getArena().getSpawnPoints().size()) {
            nextSpawn.set(0);
        }
        bukkitPlayer.teleport(LocationHelper.convertLocation(getArena().getSpawnPoints().get(spawnIndex)));

        getChallengerMap().put(uuid, challenger);

        for (LobbySign sign : getArena().getLobbySigns()) {
            sign.update();
        }

        getArena().getMinigame().getEventBus().post(new CommonChallengerJoinRoundEvent(challenger));
        return challenger;
    }

    @Override
    public void removeChallenger(Challenger challenger) throws OrphanedComponentException {
        checkState();
        removeChallenger(challenger, false, true, true);
    }

    @Override // overridden from CommonRound
    public void removeChallenger(Challenger challenger, boolean isDisconnecting, boolean updateSigns, boolean orphan)
            throws OrphanedComponentException {
        Player bukkitPlayer = Bukkit.getPlayer(challenger.getUniqueId());
        Location3D returnPoint;
        try {
            returnPoint = PlayerHelper.getReturnLocation(bukkitPlayer);
        } catch (InvalidConfigurationException | IOException ex) {
            ex.printStackTrace();
            returnPoint = LocationHelper.convertLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        CommonChallengerLeaveRoundEvent event = new CommonChallengerLeaveRoundEvent(challenger, returnPoint);
        getArena().getMinigame().getEventBus().post(event);

        super.removeChallenger(challenger, isDisconnecting, updateSigns, orphan);

        if (updateSigns) {
            for (LobbySign sign : getArena().getLobbySigns()) {
                sign.update();
            }
        }

        if (!isDisconnecting) {
            try {
                PlayerHelper.popInventory(bukkitPlayer);
            } catch (InvalidConfigurationException | IOException ex) {
                throw new RuntimeException("Could not pop inventory for player " + bukkitPlayer.getName()
                        + " from persistent storage", ex);
            }
        }

        if (!event.getReturnLocation().equals(returnPoint)) {
            try {
                PlayerHelper.storeLocation(bukkitPlayer, event.getReturnLocation());
            } catch (InvalidConfigurationException | IOException ex) {
                ex.printStackTrace();
            }
        }
        if (!isDisconnecting) {
            try {
                PlayerHelper.popLocation(bukkitPlayer);
            } catch (IllegalArgumentException | InvalidConfigurationException | IOException ex) {
                SteelCore.logSevere("Could not pop location for player " + bukkitPlayer.getName()
                        + " from persistent storage - defaulting to world spawn");
                ex.printStackTrace();
                bukkitPlayer.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }
        }
        challenger.setSpectating(false);
    }

    @Override
    public boolean isTimerTicking() throws OrphanedComponentException {
        return this.timerTicking;
    }

    @Override
    public void setTimerTicking(boolean ticking) throws OrphanedComponentException {
        if (ticking != isTimerTicking()) {
            timerTicking = ticking;
            getArena().getMinigame().getEventBus()
                    .post(ticking ? new CommonRoundTimerStartEvent(this) : new CommonRoundTimerStopEvent(this));
        }
    }

    @Override
    public void end(boolean rollback, boolean natural) {
        cancelTimerTask();
        super.end(rollback, natural);
        for (LobbySign ls : getArena().getLobbySigns()) {
            ls.update();
        }
        this.orphan();
    }

    @Override
    public void broadcast(String message) {
        for (Challenger c : getChallengers()) {
            Bukkit.getPlayer(c.getUniqueId()).sendMessage(message);
        }
    }

    public void cancelTimerTask() {
        Bukkit.getScheduler().cancelTask(schedulerHandle);
    }

    /**
     * Return whether this {@link SteelRound} object is orphaned.
     *
     * @return Whether this {@link SteelRound} object is orphaned
     * @since 1.0
     */
    public boolean isOrphaned() {
        return orphan;
    }

}
