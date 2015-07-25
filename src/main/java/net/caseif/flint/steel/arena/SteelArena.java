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

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.CommonMinigame;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.metadata.Metadata;
import net.caseif.flint.metadata.persist.PersistableMetadata;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.round.SteelRound;
import net.caseif.flint.steel.util.io.DataFiles;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Implements {@link Arena}.
 *
 * @author Max Roncacé
 */
public class SteelArena extends CommonArena {

    public static final String PERSISTENCE_NAME_KEY = "name";
    public static final String PERSISTENCE_WORLD_KEY = "world";
    public static final String PERSISTENCE_SPAWNS_KEY = "spawns";
    public static final String PERSISTENCE_BOUNDS_UPPER_KEY = "bound.upper";
    public static final String PERSISTENCE_BOUNDS_LOWER_KEY = "bound.lower";
    public static final String PERSISTENCE_METADATA_KEY = "metadata";

    private static final String ROLLBACK_STORE_BLOCK_TABLE = "blocks";

    public SteelArena(CommonMinigame parent, String id, String name, Location3D initialSpawn, Boundary boundary) {
        super(parent, id, name, initialSpawn, boundary);
    }

    @Override
    public Round createRound(ImmutableSet<LifecycleStage> stages) throws IllegalStateException {
        Preconditions.checkState(!getRound().isPresent(), "Cannot create a round in an arena already hosting one");
        Preconditions.checkState(!stages.isEmpty(), "LifecycleStage set must not be empty");
        parent.getRoundMap().put(this, new SteelRound(this, stages));
        Preconditions.checkState(getRound().isPresent(), "Cannot get created round from arena! This is a bug.");
        return getRound().get();
    }

    @Override
    public Round createRound() throws IllegalArgumentException, IllegalStateException {
        Preconditions.checkState(!getRound().isPresent(), "Cannot create a round in an arena already hosting one");
        Preconditions.checkArgument(parent.getConfigValue(ConfigNode.DEFAULT_LIFECYCLE_STAGES) != null,
                "Illegal call to no-args createRound method: default lifecycle stages are not set");
        return createRound(parent.getConfigValue(ConfigNode.DEFAULT_LIFECYCLE_STAGES));
    }

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
        cs.set(PERSISTENCE_BOUNDS_UPPER_KEY, getBoundary().getUpperBound());
        cs.set(PERSISTENCE_BOUNDS_LOWER_KEY, getBoundary().getLowerBound());
        ConfigurationSection metadata = cs.createSection(PERSISTENCE_METADATA_KEY);
        storeMetadata(metadata, getPersistableMetadata());
        yaml.save(arenaStore);
    }

    /**
     * Stores the given {@link Metadata} recursively into the given
     * {@link ConfigurationSection}
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

    public void configure(ConfigurationSection section) {
        {
            ConfigurationSection spawnSection = section.getConfigurationSection(PERSISTENCE_SPAWNS_KEY);
            for (String key : spawnSection.getKeys(false)) {
                try {
                    int index = Integer.parseInt(key);
                    spawns.put(index, Location3D.deserialize(spawnSection.getString(key)));
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
     * <p>If <code>parent</code> is <code>null</code>, it will default to this
     * arena's global {@link PersistableMetadata}.</p>
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

    public void logBlockChange(Location location, BlockState originalState)
            throws IOException, SQLException {
        File rollbackStore = new File(DataFiles.ARENA_STORE.getFile(getMinigame()), getId() + ".db");
        if (!rollbackStore.exists()) {
            //noinspection ResultOfMethodCallIgnored
            rollbackStore.createNewFile();
        }
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + rollbackStore.getPath())) {
            Statement st = conn.createStatement();
            st.execute("SELECT * FROM `" + ROLLBACK_STORE_BLOCK_TABLE + "`"
                    + " WHERE x=" + location.getX()
                    + " && y=" + location.getY()
                    + " && z=" + location.getZ());
        }
    }

}
