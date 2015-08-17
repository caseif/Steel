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
import net.caseif.flint.common.event.round.CommonRoundTimerTickEvent;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;
import net.caseif.flint.steel.util.helper.LocationHelper;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Used as the {@link Runnable} for {@link Round} timers.
 *
 * @author Max Roncacé
 */
public class RoundWorker implements Runnable {

    private final SteelRound round;

    public RoundWorker(SteelRound round) {
        this.round = round;
    }

    public void run() {
        if (round.isTimerTicking()) {
            handleTick();
        }
        if (!round.isOrphaned()) {
            checkPlayerLocations();

            for (LobbySign sign : round.getArena().getLobbySigns()) {
                if (sign.getType() == LobbySign.Type.STATUS) {
                    sign.update();
                }
            }
        }
    }

    private void handleTick() {
        boolean stageSwitch = round.getLifecycleStage().getDuration() > 0
                && round.getTime() >= round.getLifecycleStage().getDuration();
        if (stageSwitch) {
            Optional<LifecycleStage> nextStage = round.getNextLifecycleStage();
            if (nextStage.isPresent()) {
                round.setTime(0);
                round.setLifecycleStage(nextStage.get());
            } else {
                round.end(round.getConfigValue(ConfigNode.ROLLBACK_ON_END), true);
                return;
            }
        } else {
            round.setTime(round.getTime() + 1, false);
        }
        round.getArena().getMinigame().getEventBus().post(new CommonRoundTimerTickEvent(round, round.getTime(),
                stageSwitch ? 0 : round.getTime() + 1));
    }

    private void checkPlayerLocations() {
        Boundary bound = round.getArena().getBoundary();
        for (Challenger challenger : round.getChallengers()) {
            Player player = Bukkit.getPlayer(challenger.getUniqueId());
            Location3D loc = LocationHelper.convertLocation(player.getLocation());
            if (!bound.contains(loc)) {
                if (round.getConfigValue(ConfigNode.ALLOW_EXIT_BOUNDARY)) {
                    challenger.removeFromRound();
                } else {
                    double x = loc.getX() > bound.getUpperBound().getX() ? bound.getUpperBound().getX()
                            : loc.getX() < bound.getLowerBound().getX() ? bound.getLowerBound().getX()
                                    : loc.getX();
                    double y = loc.getY() > bound.getUpperBound().getY() ? bound.getUpperBound().getY()
                            : loc.getY() < bound.getLowerBound().getY() ? bound.getLowerBound().getY()
                                    : loc.getY();
                    double z = loc.getZ() > bound.getUpperBound().getZ() ? bound.getUpperBound().getZ()
                            : loc.getZ() < bound.getLowerBound().getZ() ? bound.getLowerBound().getZ()
                                    : loc.getZ();
                    player.teleport(new Location(player.getWorld(), x, y, z,
                            player.getLocation().getYaw(), player.getLocation().getPitch()));
                }
            }
        }
    }

}
