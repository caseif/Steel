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

import net.caseif.steel.SteelArena;
import net.caseif.steel.challenger.SteelChallenger;
import net.caseif.steel.challenger.SteelTeam;
import net.caseif.steel.util.SteelMetadatable;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import net.caseif.flint.Arena;
import net.caseif.flint.Minigame;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.challenger.Team;
import net.caseif.flint.config.RoundConfigNode;
import net.caseif.flint.exception.round.RoundJoinException;
import net.caseif.flint.locale.Localizable;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/**
 * Implements {@link Round}.
 *
 * @author Max Roncacé
 */
public class SteelRound extends SteelMetadatable implements Round {

    private SteelArena arena;

    private BiMap<UUID, Challenger> challengers = HashBiMap.create();
    private BiMap<String, Team> teams = HashBiMap.create();
    private HashMap<RoundConfigNode<?>, Object> config = new HashMap<>();

    private ArrayList<LifecycleStage> stages = new ArrayList<>();
    private int currentStage = 0;
    private long time;

    public int spectators;

    public SteelRound(SteelArena arena) {
        this.arena = arena;
    }

    @Override
    public Arena getArena() {
        return arena;
    }

    @Override
    public Set<Challenger> getChallengers() {
        return ImmutableSet.copyOf(challengers.values());
    }

    @Override
    public Optional<Challenger> getChallenger(UUID uuid) {
        return Optional.fromNullable(challengers.get(uuid));
    }

    @Override
    public Challenger addChallenger(UUID uuid) throws RoundJoinException {
        return new SteelChallenger(uuid, this);
    }

    @Override
    public void removeChallenger(UUID uuid) throws IllegalArgumentException {
        Challenger c = challengers.get(uuid);
        if (c == null) {
            throw new IllegalArgumentException("Cannot get Challenger from UUID");
        }
        removeChallenger(c);
    }

    @Override
    public void removeChallenger(Challenger challenger) {
        if (challenger.getRound() == this) {
            challengers.remove(challenger.getUniqueId(), challenger);
            ((SteelChallenger)challenger).invalidate();
        } else {
            throw new IllegalArgumentException("Cannot remove Challenger: round mismatch");
        }
    }

    @Override
    public Set<Team> getTeams() {
        return ImmutableSet.copyOf(teams.values());
    }

    @Override
    public Optional<Team> getTeam(String id) {
        return Optional.fromNullable(teams.get(id));
    }

    @Override
    public Team createTeam(String id) throws IllegalArgumentException {
        if (teams.containsKey(id)) {
            throw new IllegalArgumentException("Team \"" + id + "\"already exists");
        }
        return new SteelTeam(id, this);
    }

    @Override
    public Team getOrCreateTeam(String id) {
        Optional<Team> team = getTeam(id);
        if (team.isPresent()) {
            return team.get();
        } else {
            return createTeam(id);
        }
    }

    @Override
    public int getSpectatorCount() {
        return spectators;
    }

    @Override
    public void broadcast(String message) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public void broadcast(Localizable message) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public ArrayList<LifecycleStage> getLifecycleStages() {
        return stages;
    }

    @Override
    public void setLifecycleStages(ArrayList<LifecycleStage> stages) {
        this.stages = stages;
    }

    @Override
    public LifecycleStage getLifecycleStage() {
        return getLifecycleStages().get(currentStage);
    }

    @Override
    public Optional<LifecycleStage> getNextLifecycleStage() {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public long getRemainingTime() {
        return getLifecycleStage().getDuration() == -1 ? -1 : getLifecycleStage().getDuration() - time;
    }

    @Override
    public void startTimer() {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public void stopTimer() {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public void resetTimer() {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public void rollback() {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public void end() {
        end(true);
    }

    @Override
    public void end(boolean rollback) {
        if (rollback) {
            rollback();
        }
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(RoundConfigNode node) {
        return (T)config.get(node);
    }

    @Override
    public <T> void setConfigValue(RoundConfigNode node, T value) {
        config.put(node, value);
    }

    @Override
    public Minigame getMinigame() {
        return getArena().getMinigame();
    }

    @Override
    public String getPlugin() {
        return getArena().getPlugin();
    }
}
