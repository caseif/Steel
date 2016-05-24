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
package net.caseif.flint.steel.arena;

import static com.google.common.base.Preconditions.checkArgument;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.minigame.CommonMinigame;
import net.caseif.flint.component.exception.OrphanedComponentException;
import net.caseif.flint.exception.rollback.RollbackException;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.lobby.type.ChallengerListingLobbySign;
import net.caseif.flint.lobby.type.StatusLobbySign;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;
import net.caseif.flint.steel.lobby.SteelLobbySign;
import net.caseif.flint.steel.lobby.type.SteelChallengerListingLobbySign;
import net.caseif.flint.steel.lobby.type.SteelStatusLobbySign;
import net.caseif.flint.steel.minigame.SteelMinigame;
import net.caseif.flint.steel.round.SteelRound;
import net.caseif.flint.steel.util.helper.LocationHelper;
import net.caseif.flint.steel.util.helper.rollback.RollbackHelper;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Implements {@link Arena}.
 *
 * @author Max Roncacé
 */
@SuppressWarnings("DuplicateThrows")
public class SteelArena extends CommonArena {

    public SteelArena(CommonMinigame parent, String id, String name, Location3D initialSpawn, Boundary boundary) {
        super(parent, id.toLowerCase(), name, initialSpawn, boundary);
        assert !id.contains(".");
        rbHelper = new RollbackHelper(this);
    }

    @Override
    public Round createRound(ImmutableSet<LifecycleStage> stages)
            throws IllegalArgumentException, IllegalStateException, OrphanedComponentException {
        checkState();
        Preconditions.checkState(!getRound().isPresent(), "Cannot create a round in an arena already hosting one");
        checkArgument(stages != null && !stages.isEmpty(), "LifecycleStage set must not be null or empty");
        ((SteelMinigame) getMinigame()).getRoundMap().put(this, new SteelRound(this, stages));
        Preconditions.checkState(getRound().isPresent(), "Cannot get created round from arena! This is a bug.");
        return getRound().get();
    }

    @Override
    public Optional<StatusLobbySign> createStatusLobbySign(Location3D location) throws IllegalArgumentException {
        if (checkLocationForLobbySign(location)) {
            return storeAndWrap((StatusLobbySign) new SteelStatusLobbySign(location, this));
        }
        return Optional.absent();
    }

    @Override
    public Optional<ChallengerListingLobbySign> createChallengerListingLobbySign(Location3D location, int index) {
        if (checkLocationForLobbySign(location)) {
            return storeAndWrap((ChallengerListingLobbySign)
                    new SteelChallengerListingLobbySign(location, this, index));
        }
        return Optional.absent();
    }

    @Override
    public void markForRollback(Location3D location) throws IllegalArgumentException, RollbackException {
        checkArgument(getBoundary().contains(location),
                "Cannot mark block for rollback in arena " + getId() + " - not within boundary");

        try {
            Location loc = LocationHelper.convertLocation(location);
            getRollbackHelper().logBlockChange(loc, loc.getBlock().getState());
        } catch (IOException | SQLException ex) {
            throw new RollbackException(ex);
        }
    }

    @Override
    public RollbackHelper getRollbackHelper() {
        return (RollbackHelper) super.getRollbackHelper();
    }

    private boolean checkLocationForLobbySign(Location3D location) throws IllegalArgumentException {
        checkArgument(location.getWorld().isPresent(), "Location for lobby sign must contain world");
        World world = Bukkit.getWorld(location.getWorld().get());
        if (world == null) {
            throw new IllegalArgumentException("Invalid world for lobby sign location");
        }
        Block block = LocationHelper.convertLocation(location).getBlock();
        return block.getState() instanceof Sign && !getLobbySignMap().containsKey(location);
    }

    private <T extends LobbySign> Optional<T> storeAndWrap(T sign) {
        ((SteelLobbySign) sign).store();
        getLobbySignMap().put(sign.getLocation(), sign);
        return Optional.of(sign);
    }

}
