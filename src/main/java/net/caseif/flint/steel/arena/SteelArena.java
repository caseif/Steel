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
package net.caseif.flint.steel.arena;

import static com.google.common.base.Preconditions.checkArgument;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.minigame.CommonMinigame;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.exception.OrphanedObjectException;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.lobby.type.ChallengerListingLobbySign;
import net.caseif.flint.lobby.type.StatusLobbySign;
import net.caseif.flint.metadata.Metadata;
import net.caseif.flint.metadata.persist.PersistableMetadata;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.lobby.SteelLobbySign;
import net.caseif.flint.steel.lobby.type.SteelChallengerListingLobbySign;
import net.caseif.flint.steel.lobby.type.SteelStatusLobbySign;
import net.caseif.flint.steel.minigame.SteelMinigame;
import net.caseif.flint.steel.round.SteelRound;
import net.caseif.flint.steel.util.file.DataFiles;
import net.caseif.flint.steel.util.helper.LocationHelper;
import net.caseif.flint.steel.util.helper.rollback.RollbackHelper;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Implements {@link Arena}.
 *
 * @author Max Roncacé
 */
@SuppressWarnings("ALL")
public class SteelArena extends CommonArena {

    public static final String PERSISTENCE_NAME_KEY = "name";
    public static final String PERSISTENCE_WORLD_KEY = "world";
    public static final String PERSISTENCE_SPAWNS_KEY = "spawns";
    public static final String PERSISTENCE_BOUNDS_UPPER_KEY = "bound.upper";
    public static final String PERSISTENCE_BOUNDS_LOWER_KEY = "bound.lower";
    public static final String PERSISTENCE_METADATA_KEY = "metadata";

    private final RollbackHelper rbHelper;

    public SteelArena(CommonMinigame parent, String id, String name, Location3D initialSpawn, Boundary boundary) {
        super(parent, id.toLowerCase(), name, initialSpawn, boundary);
        this.rbHelper = new RollbackHelper(this);
    }

    @Override
    public Round createRound(ImmutableSet<LifecycleStage> stages)
            throws IllegalArgumentException, IllegalStateException, OrphanedObjectException {
        checkState();
        Preconditions.checkState(!getRound().isPresent(), "Cannot create a round in an arena already hosting one");
        checkArgument(!stages.isEmpty(), "LifecycleStage set must not be empty");
        ((SteelMinigame) getMinigame()).getRoundMap().put(this, new SteelRound(this, stages));
        Preconditions.checkState(getRound().isPresent(), "Cannot get created round from arena! This is a bug.");
        return getRound().get();
    }

    @Override
    public Round createRound() throws IllegalStateException, OrphanedObjectException {
        checkState();
        Preconditions.checkState(!getRound().isPresent(), "Cannot create a round in an arena already hosting one");
        Preconditions.checkState(((SteelMinigame) getMinigame()).getConfigValue(ConfigNode.DEFAULT_LIFECYCLE_STAGES)
                        != null,
                "Illegal call to no-args createRound method: default lifecycle stages are not set");
        return createRound(((SteelMinigame) getMinigame()).getConfigValue(ConfigNode.DEFAULT_LIFECYCLE_STAGES));
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
        return Optional.of(sign);
    }

    @Override
    public void rollback() throws IllegalStateException, OrphanedObjectException {
        checkState();
        try {
            getRollbackHelper().popRollbacks();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to rollback arena " + getName(), ex);
        }
    }

    /**
     * Gets the {@link RollbackHelper} associated with this {@link SteelArena}.
     *
     * @return The {@link RollbackHelper} associated with this
     *     {@link SteelArena}
     */
    public RollbackHelper getRollbackHelper() {
        return rbHelper;
    }

    /**
     * Stores this arena into persistent storage.
     *
     * @throws InvalidConfigurationException If an exception occurs while
     *     configuring the persistent store
     * @throws IOException If an exception occurs while writing to the
     *     persistent store
     */
    @Override
    public void store() throws InvalidConfigurationException, IOException {
        File arenaStore = DataFiles.ARENA_STORE.getFile(getMinigame());
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(arenaStore);
        yaml.set(getId(), null); // for good measure
        ConfigurationSection cs = yaml.createSection(getId());
        cs.set(PERSISTENCE_NAME_KEY, getName());
        cs.set(PERSISTENCE_WORLD_KEY, getWorld());
        ConfigurationSection spawns = cs.createSection(PERSISTENCE_SPAWNS_KEY);
        for (Map.Entry<Integer, Location3D> entry : getSpawnPoints().entrySet()) {
            spawns.set(entry.getKey().toString(), entry.getValue().serialize());
        }
        cs.set(PERSISTENCE_BOUNDS_UPPER_KEY, getBoundary().getUpperBound().serialize());
        cs.set(PERSISTENCE_BOUNDS_LOWER_KEY, getBoundary().getLowerBound().serialize());
        ConfigurationSection metadata = cs.createSection(PERSISTENCE_METADATA_KEY);
        storeMetadata(metadata, getPersistableMetadata());
        yaml.save(arenaStore);
    }

    /**
     * Removes this arena from persistent storage.
     *
     * @throws InvalidConfigurationException If an exception occurs while
     *     configuring the persistent store
     * @throws IOException If an exception occurs while writing to the
     *     persistent store
     */
    public void removeFromStore() throws InvalidConfigurationException, IOException {
        File arenaStore = DataFiles.ARENA_STORE.getFile(getMinigame());
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(arenaStore);
        yaml.set(getId(), null);
        yaml.save(arenaStore);
    }

    /**
     * Stores the given {@link Metadata} recursively into the given
     * {@link ConfigurationSection}.
     *
     * @param section The {@link ConfigurationSection} to store to
     * @param data The {@link Metadata} to store
     */
    private void storeMetadata(ConfigurationSection section, PersistableMetadata data) {
        for (String key : data.getAllKeys()) {
            Optional<?> value = getPersistableMetadata().get(key);
            Preconditions.checkState(value.isPresent(), "Value for key " + key + " is not present");

            if (value.get() instanceof String) {
                section.set(key, value.get());
            } else if (value.get() instanceof PersistableMetadata) {
                storeMetadata(section.createSection(key), (PersistableMetadata)value.get());
            }
        }
    }

    /**
     * Configures this {@link SteelArena} from the given
     * {@link ConfigurationSection}.
     *
     * @param section The section containing data for this {@link SteelArena}
     */
    public void configure(ConfigurationSection section) {
        {
            ConfigurationSection spawnSection = section.getConfigurationSection(PERSISTENCE_SPAWNS_KEY);
            for (String key : spawnSection.getKeys(false)) {
                try {
                    int index = Integer.parseInt(key);
                    getSpawnPointMap().put(index, Location3D.deserialize(spawnSection.getString(key)));
                } catch (IllegalArgumentException ignored) {
                    SteelCore.logWarning("Invalid spawn at index " + key + " for arena \"" + getId() + "\"");
                }
            }
        }

        if (section.isConfigurationSection(PERSISTENCE_METADATA_KEY)) {
            loadMetadata(section.getConfigurationSection(PERSISTENCE_METADATA_KEY), null);
        }
    }

    /**
     * Loads data recursively from the given {@link ConfigurationSection} into
     * the given {@link PersistableMetadata}.
     *
     * <p>If {@code parent} is {@code null}, it will default to this arena's
     * global {@link PersistableMetadata}.</p>
     *
     * @param section The {@link ConfigurationSection} to load data from
     * @param parent The {@link PersistableMetadata} object ot load data into
     */
    private void loadMetadata(ConfigurationSection section, PersistableMetadata parent) {
        if (parent == null) {
            parent = getPersistableMetadata();
        }

        for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                loadMetadata(section.getConfigurationSection(key), parent.createStructure(key));
            } else if (section.isString(key)) {
                parent.set(key, section.getString(key));
            }
        }
    }

}
