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

import net.caseif.steel.util.SteelMetadatable;

import com.google.common.collect.ImmutableSet;
import net.caseif.flint.Minigame;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.challenger.Team;
import net.caseif.flint.round.Round;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements {@link Team}.
 *
 * @author Max Roncacé
 */
public class SteelTeam extends SteelMetadatable implements Team {

    private String id;
    private Round round;

    private String name;
    private Set<Challenger> challengers = new HashSet<>();

    public SteelTeam(String id, Round round) throws IllegalArgumentException {
        if (round.getTeam(id).isPresent()) {
            throw new IllegalArgumentException("Team \"" + id + "\" already exists");
        }
        this.id = id;
        this.name = id;
        this.round = round;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.name = displayName;
    }

    @Override
    public Round getRound() {
        return round;
    }

    @Override
    public Set<Challenger> getChallengers() {
        return ImmutableSet.copyOf(challengers);
    }

    @Override
    public void addChallenger(Challenger challenger) {
        challengers.add(challenger);
    }

    @Override
    public Minigame getMinigame() {
        return getRound().getMinigame();
    }

    @Override
    public String getPlugin() {
        return getRound().getPlugin();
    }
}
