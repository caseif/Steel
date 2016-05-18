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
package net.caseif.flint.steel.round;

import net.caseif.flint.arena.SpawningMode;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.challenger.CommonChallenger;
import net.caseif.flint.common.event.round.challenger.CommonChallengerJoinRoundEvent;
import net.caseif.flint.common.event.round.challenger.CommonChallengerLeaveRoundEvent;
import net.caseif.flint.common.round.CommonJoinResult;
import net.caseif.flint.common.round.CommonRound;
import net.caseif.flint.component.exception.OrphanedComponentException;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.round.JoinResult;
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
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Implements {@link Round}.
 *
 * @author Max Roncacé
 */
public class SteelRound extends CommonRound {

    private final int schedulerHandle;

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
    public JoinResult addChallenger(UUID uuid) throws IllegalStateException, OrphanedComponentException {
        checkState();

        Player bukkitPlayer = Bukkit.getPlayer(uuid);
        if (bukkitPlayer == null) {
            return new CommonJoinResult(JoinResult.Status.PLAYER_OFFLINE);
        }

        if (getChallengers().size() >= getConfigValue(ConfigNode.MAX_PLAYERS)) {
            return new CommonJoinResult(JoinResult.Status.ROUND_FULL);
        }

        if (CommonCore.getChallenger(uuid).isPresent()) {
            return new CommonJoinResult(JoinResult.Status.ALREADY_IN_ROUND);
        }

        Location spawn = LocationHelper.convertLocation(nextSpawnPoint());

        SteelChallenger challenger = new SteelChallenger(uuid, this);

        try {
            PlayerHelper.storeLocation(bukkitPlayer);
        } catch (IllegalArgumentException | InvalidConfigurationException | IOException ex) {
            return new CommonJoinResult(ex);
        }


        bukkitPlayer.teleport(spawn);

        getChallengerMap().put(uuid, challenger);

        for (LobbySign sign : getArena().getLobbySigns()) {
            sign.update();
        }

        try {
            PlayerHelper.pushInventory(bukkitPlayer);
        } catch (IOException ex) {
            return new CommonJoinResult(ex);
        }

        getArena().getMinigame().getEventBus().post(new CommonChallengerJoinRoundEvent(challenger));
        return new CommonJoinResult(challenger);
    }

    @Override // overridden from CommonRound
    public void removeChallenger(Challenger challenger, boolean isDisconnecting, boolean updateSigns)
            throws OrphanedComponentException {
        super.removeChallenger(challenger, isDisconnecting, updateSigns);

        Player bukkitPlayer = Bukkit.getPlayer(challenger.getUniqueId());
        Location3D returnPoint;
        try {
            returnPoint = PlayerHelper.getReturnLocation(bukkitPlayer);
        } catch (InvalidConfigurationException | IOException ex) {
            ex.printStackTrace();
            returnPoint = LocationHelper.convertLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        if (!isDisconnecting) {
            try {
                PlayerHelper.popInventory(bukkitPlayer);
            } catch (InvalidConfigurationException | IOException ex) {
                // don't actually throw the exception so it doesn't ruin everything
                new RuntimeException("Could not pop inventory for player " + bukkitPlayer.getName()
                        + " from persistent storage", ex).printStackTrace();
            }
        }

        CommonChallengerLeaveRoundEvent event = new CommonChallengerLeaveRoundEvent(challenger, returnPoint);
        getArena().getMinigame().getEventBus().post(event);

        if (!challenger.getRound().isEnding()) {
            ((CommonChallenger) challenger).orphan();
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
    }

    @Override
    public Location3D nextSpawnPoint() {
        if (getConfigValue(ConfigNode.SPAWNING_MODE) == SpawningMode.PROXIMITY_HIGH) {
            List<Location3D> candidates = new ArrayList<>();
            double greatestMean = 0;

            if (getChallengers().size() == 0) {
                // just select a random spawn point
                return getArena().getSpawnPoints()
                        .get((int) Math.floor(Math.random() * getArena().getSpawnPoints().size()));
            }

            for (Location3D loc : getArena().getSpawnPoints().values()) {
                Location bukkitLoc = LocationHelper.convertLocation(loc);
                int sum = 0;
                for (Challenger ch : getChallengers()) {
                    sum += Bukkit.getPlayer(ch.getUniqueId()).getLocation().distance(bukkitLoc);
                }
                double mean = sum / getChallengers().size();
                if (mean > greatestMean) {
                    candidates = Collections.singletonList(loc);
                    greatestMean = mean;
                } else if (mean == greatestMean) {
                    candidates.add(loc);
                }
            }
            return candidates.get((int) Math.floor(Math.random() * candidates.size()));
        } else {
            return super.nextSpawnPoint();
        }
    }

    @SuppressWarnings("DuplicateThrows")
    @Override
    public void end(boolean rollback, boolean natural) throws IllegalStateException, OrphanedComponentException {
        checkState();
        cancelTimerTask();
        super.end(rollback, natural);
        for (LobbySign ls : getArena().getLobbySigns()) {
            ls.update();
        }
        this.orphan();
    }

    @Override
    public void broadcast(String message) {
        checkState();
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
