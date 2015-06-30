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
package net.caseif.flint.steel;

import net.caseif.flint.Arena;
import net.caseif.flint.common.CommonArena;
import net.caseif.flint.common.CommonMinigame;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;
import net.caseif.flint.steel.round.SteelRound;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * Implements {@link Arena}.
 *
 * @author Max Roncacé
 */
public class SteelArena extends CommonArena {

    public SteelArena(CommonMinigame parent, String id, String name, Location3D initialSpawn) {
        super(parent, id, name, initialSpawn);
    }

    @Override
    public Round createRound(ImmutableSet<LifecycleStage> stages) throws IllegalArgumentException,
            IllegalStateException {
        Preconditions.checkState(!getRound().isPresent(), "Cannot create a round in an arena already hosting one");
        Preconditions.checkArgument(!stages.isEmpty(), "LifecycleStage set must not be empty");
        parent.getRoundMap().put(this, new SteelRound(this, stages));
        assert getRound().isPresent();
        return getRound().get();
    }

    @Override
    public Round createRound() throws IllegalArgumentException, IllegalStateException {
        Preconditions.checkState(!getRound().isPresent(), "Cannot create a round in an arena already hosting one");
        Preconditions.checkArgument(parent.getConfigValue(ConfigNode.DEFAULT_LIFECYCLE_STAGES) != null,
                "Illegal call to no-args createRound method: default lifecycle stages are not set");
        return createRound(parent.getConfigValue(ConfigNode.DEFAULT_LIFECYCLE_STAGES));
    }
}
