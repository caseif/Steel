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
import net.caseif.steel.util.SteelMetadatable;

import com.google.common.base.Optional;
import net.caseif.flint.Minigame;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.challenger.Team;
import net.caseif.flint.event.challenger.ChallengerJoinRoundEvent;
import net.caseif.flint.exception.round.RoundJoinException;
import net.caseif.flint.round.Round;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * Implements {@link Challenger}.
 *
 * @author Max Roncacé
 */
public class SteelChallenger extends SteelMetadatable implements Challenger {

    private UUID uuid;
    private String name;
    private SteelRound round;

    private Team team;
    private boolean spectating = false;

    public SteelChallenger(UUID uuid, SteelRound round) throws RoundJoinException {
        if (Bukkit.getPlayer(uuid) == null) {
            throw new RoundJoinException(uuid, round, RoundJoinException.Reason.OFFLINE, "Player is offline");
        }
        this.uuid = uuid;
        this.name = Bukkit.getPlayer(uuid).getName();
        this.round = round;
        ChallengerJoinRoundEvent event = new SteelChallengerJoinRoundEvent(this);

    }

    /**
     * Checks the state of this {@link SteelChallenger} object.
     *
     * @throws IllegalStateException If this object is no longer contained by a
     *                               {@link Round}
     */
    private void checkState() throws IllegalStateException {
        if (round == null) {
            throw new IllegalStateException("Challenger is no longer in a round");
        }
    }

    @Override
    public String getName() throws IllegalStateException {
        checkState();
        return name;
    }

    @Override
    public UUID getUniqueId() throws IllegalStateException {
        checkState();
        return uuid;
    }

    @Override
    public Round getRound() throws IllegalStateException {
        checkState();
        return round;
    }

    @Override
    public void removeFromRound() throws IllegalStateException {
        checkState();
        round.removeChallenger(this);
    }

    public void invalidate() {
        checkState();
        round = null;
    }

    @Override
    public Optional<Team> getTeam() throws IllegalStateException {
        checkState();
        return Optional.fromNullable(team);
    }

    @Override
    public void setTeam(Team team) throws IllegalStateException {
        checkState();
        this.team = team;
    }

    @Override
    public boolean isSpectating() throws IllegalStateException {
        checkState();
        return spectating;
    }

    @Override
    public void setSpectating(boolean spectating) throws IllegalStateException {
        checkState();
        if (this.spectating != spectating) {
            this.spectating = spectating;
            round.spectators += spectating ? 1 : -1;
        }
    }

    @Override
    public Minigame getMinigame() throws IllegalStateException {
        checkState();
        return round.getMinigame();
    }

    @Override
    public String getPlugin() throws IllegalStateException {
        checkState();
        return round.getPlugin();
    }
}
