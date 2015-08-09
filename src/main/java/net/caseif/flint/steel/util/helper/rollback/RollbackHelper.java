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
package net.caseif.flint.steel.util.helper.rollback;

import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.arena.SteelArena;
import net.caseif.flint.steel.util.file.DataFiles;
import net.caseif.flint.steel.util.helper.LocationHelper;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

/**
 * Static utility class for rollback-related functionality.
 *
 * @author Max Roncacé
 */
public final class RollbackHelper {

    public static final String SQLITE_PROTOCOL = "jdbc:sqlite:";
    public static final Properties SQL_QUERIES = new Properties();

    private static final int RECORD_TYPE_BLOCK_CHANGED = 0;
    private static final int RECORD_TYPE_ENTITY_CREATED = 1;
    private static final int RECORD_TYPE_ENTITY_CHANGED = 2;

    private File rollbackStore;
    private File stateStore;

    private SteelArena arena;

    static {
        try (InputStream is = RollbackHelper.class.getResourceAsStream("/sql-queries.properties")) {
            SQL_QUERIES.load(is);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load SQL query strings", ex);
        }
    }

    /**
     * Creates a new {@link RollbackHelper} backing the given
     * {@link SteelArena}.
     *
     * @param arena The {@link SteelArena} to be backed by the new
     *     {@link RollbackHelper}
     */
    public RollbackHelper(SteelArena arena) {
        this.arena = arena;
        rollbackStore = DataFiles.ROLLBACK_STORE.getFile(arena.getMinigame());
        stateStore = DataFiles.ROLLBACK_STATE_STORE.getFile(arena.getMinigame());
    }

    /**
     * Returns the {@link SteelArena} associated with this
     * {@link RollbackHelper}.
     *
     * @return The {@link SteelArena} associated with this
     * {@link RollbackHelper}.
     */
    public SteelArena getArena() {
        return arena;
    }

    /**
     * Creates a rollback database for the arena backing this
     * {@link RollbackHelper}.
     *
     * @throws IOException If an exception occurs while creating the database
     *     file
     * @throws SQLException If an exception occurs while manipulating the
     *     database
     */
    public void createRollbackDatabase() throws IOException, SQLException {
        if (!rollbackStore.exists()) {
            //noinspection ResultOfMethodCallIgnored
            rollbackStore.createNewFile();
        }
        if (!stateStore.exists()) {
            //noinspection ResultOfMethodCallIgnored
            stateStore.createNewFile();
        }
        try (Connection conn = DriverManager.getConnection(SQLITE_PROTOCOL + rollbackStore.getAbsolutePath())) {
            try (
                    PreparedStatement st = conn.prepareStatement(SQL_QUERIES.getProperty("create-rollback-table")
                            .replace("{table}", getArena().getId()));
            ) {
                st.executeUpdate();
            }
        }
    }

    /**
     * Logs a rollback change at the given location.
     *
     * @param location The location of the change
     * @param originalState The state of the rollback before the change
     * @throws InvalidConfigurationException If an exception occurs while
     *     storing the state of the rollback
     * @throws IOException If an exception occurs while reading to or from the
     *     rollback database
     * @throws SQLException If an exception occurs while manipulating the
     *     rollback database
     */
    @SuppressWarnings("deprecation")
    public void logBlockChange(Location location, BlockState originalState)
            throws InvalidConfigurationException, IOException, SQLException {
        ConfigurationSection state = BlockStateSerializer.serializeState(originalState).orNull();
        logChange(RECORD_TYPE_BLOCK_CHANGED, location, null, originalState.getType().name(), originalState.getRawData(),
                state);
    }

    public void logEntityCreation(Entity entity) throws InvalidConfigurationException, IOException, SQLException {
        logEntitySomething(entity, true);
    }

    public void logEntityChange(Entity entity) throws InvalidConfigurationException, IOException, SQLException {
        logEntitySomething(entity, false);
    }

    private void logEntitySomething(Entity entity, boolean newlyCreated)
            throws InvalidConfigurationException, IOException, SQLException {
        ConfigurationSection state = !newlyCreated ? EntityStateSerializer.serializeState(entity) : null;
        logChange(newlyCreated ? RECORD_TYPE_ENTITY_CREATED : RECORD_TYPE_ENTITY_CHANGED, entity.getLocation(),
                entity.getUniqueId(), entity.getType().name(), -1, state);
    }

    private void logChange(int recordType, Location location, UUID uuid, String type, int data,
                           ConfigurationSection state) throws InvalidConfigurationException, IOException, SQLException {
        Preconditions.checkNotNull(location, "Location required for all record types");
        switch (recordType) {
            case RECORD_TYPE_BLOCK_CHANGED:
                Preconditions.checkNotNull(type, "Type required for BLOCK_CHANGED record type");
                break;
            case RECORD_TYPE_ENTITY_CREATED:
                Preconditions.checkNotNull(uuid, "UUID required for ENTITY_CREATED record type");
                Preconditions.checkNotNull(type, "Type required for ENTITY_CREATED record type");
                break;
            case RECORD_TYPE_ENTITY_CHANGED:
                Preconditions.checkNotNull(type, "Type required for ENTITY_CHANGED record type");
                Preconditions.checkNotNull(state, "State required for ENTITY_CHANGED record type");
                break;
            default:
                throw new IllegalArgumentException("Undefined record type");
        }
        if (!rollbackStore.exists()) {
            //noinspection ResultOfMethodCallIgnored
            rollbackStore.createNewFile();
        }
        try (
                Connection conn = DriverManager.getConnection("jdbc:sqlite:" + rollbackStore.getPath());
        ) {
            String querySql;
            switch (recordType) {
                case RECORD_TYPE_BLOCK_CHANGED:
                    querySql = SQL_QUERIES.getProperty("query-by-location")
                            .replace("{world}", location.getWorld().getName())
                            .replace("{x}", "" + location.getBlockX())
                            .replace("{y}", "" + location.getBlockY())
                            .replace("{z}", "" + location.getBlockZ());
                    break;
                case RECORD_TYPE_ENTITY_CHANGED:
                    querySql = SQL_QUERIES.getProperty("query-by-uuid")
                            .replace("{uuid}", uuid.toString());
                    break;
                default:
                    querySql = null;
                    break;
            }
            if (querySql != null) {
                querySql = querySql.replace("{table}", getArena().getId());
                try (
                        PreparedStatement query = conn.prepareStatement(querySql);
                        ResultSet queryResults = query.executeQuery();
                ) {
                    if (queryResults.next()) {
                        return; // subject has already been modified; no need to re-record
                    }
                }
            }

            String updateSql;
            switch (recordType) {
                case RECORD_TYPE_BLOCK_CHANGED:
                    updateSql = SQL_QUERIES.getProperty("insert-block-rollback-record")
                            .replace("{world}", location.getWorld().getName())
                            .replace("{x}", "" + location.getBlockX())
                            .replace("{y}", "" + location.getBlockY())
                            .replace("{z}", "" + location.getBlockZ())
                            .replace("{type}", type)
                            .replace("{data}", "" + data);
                    break;
                case RECORD_TYPE_ENTITY_CREATED:
                    updateSql = SQL_QUERIES.getProperty("insert-entity-created-rollback-record")
                            .replace("{world}", location.getWorld().getName())
                            .replace("{uuid}", uuid.toString());
                    break;
                case RECORD_TYPE_ENTITY_CHANGED:
                    updateSql = SQL_QUERIES.getProperty("insert-entity-changed-rollback-record")
                            .replace("{world}", location.getWorld().getName())
                            .replace("{x}", "" + location.getBlockX())
                            .replace("{y}", "" + location.getBlockY())
                            .replace("{z}", "" + location.getBlockZ())
                            .replace("{uuid}", uuid.toString())
                            .replace("{type}", type);
                    break;
                default:
                    throw new AssertionError("Inconsistency detected in method: recordType is in an illegal state");
            }
            if (updateSql != null) {
                // replace non-negotiable values
                updateSql = updateSql
                        .replace("{table}", getArena().getId())
                        .replace("{state}", "" + (state != null))
                        .replace("{record_type}", "" + recordType);
            }
            int id;
            try (
                    PreparedStatement ps = conn.prepareStatement(updateSql, Statement.RETURN_GENERATED_KEYS);
                    ResultSet gen = ps.getGeneratedKeys();
            ) {
                if (gen.next()) {
                    id = gen.getInt(1);
                } else {
                    throw new SQLException("Failed to get generated key from update query");
                }
            }
            if (state != null) {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.load(stateStore);
                ConfigurationSection arenaSec = yaml.isConfigurationSection(getArena().getId())
                        ? yaml.getConfigurationSection(getArena().getId())
                        : yaml.createSection(getArena().getId());
                if (arenaSec.isSet(Integer.toString(id))) {
                    throw new IllegalStateException("Tried to store state with id " + id + ", but "
                            + "index was already present in rollback store! Something's gone terribly "
                            + "wrong."); // technically should never happen but you never know
                }
                arenaSec.set(Integer.toString(id), state);
                yaml.save(stateStore);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void popRollbacks() throws SQLException {
        if (rollbackStore.exists()) {
            try (
                    Connection conn = DriverManager.getConnection(SQLITE_PROTOCOL + rollbackStore.getAbsolutePath());
                    PreparedStatement st = conn.prepareStatement(SQL_QUERIES.getProperty("get-all-records")
                            .replace("{table}", getArena().getId()));
                    ResultSet rs = st.executeQuery();
            ) {
                World w = Bukkit.getWorld(getArena().getWorld());

                // hash entities by UUID before iterating records for faster lookup
                HashMap<UUID, Entity> entities = new HashMap<>();
                for (Entity entity : w.getEntities()) {
                    entities.put(entity.getUniqueId(), entity);
                }

                while (rs.next()) {
                    try {
                        int id = rs.getInt("id");
                        String world = rs.getString("world");
                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        int z = rs.getInt("z");
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        String type = rs.getString("type");
                        int data = rs.getInt("data");
                        boolean state = rs.getBoolean("state");
                        int recordType = rs.getInt("record_type");

                        if (world.equals(getArena().getWorld())) {
                            ConfigurationSection stateSerial = null;
                            if (state) {
                                YamlConfiguration yaml = new YamlConfiguration();
                                yaml.load(stateStore);
                                if (yaml.isConfigurationSection("" + id)) {
                                    stateSerial = yaml.getConfigurationSection(getArena().getId());
                                } else {
                                    SteelCore.logVerbose("Rollback record with ID " + id + " was marked as having "
                                            + "state, but no corresponding serial was found");
                                }
                            }

                            switch (recordType) {
                                case RECORD_TYPE_BLOCK_CHANGED:
                                    Block b = w.getBlockAt(x, y, z);
                                    Material m = Material.valueOf(type);
                                    if (m != null) {
                                        b.setType(m);
                                        b.setData((byte) data);
                                        if (stateSerial != null) {
                                            BlockStateSerializer.deserializeState(b, stateSerial);
                                        }
                                    } else {
                                        SteelCore.logWarning("Rollback record with ID " + id + " in arena "
                                                + getArena().getId() + " cannot be matched to a Material");
                                    }
                                    break;
                                case RECORD_TYPE_ENTITY_CREATED:
                                    if (entities.containsKey(uuid)) {
                                        entities.get(uuid).remove();
                                    } // else: probably already removed by a player or something else
                                    break;
                                case RECORD_TYPE_ENTITY_CHANGED:
                                    EntityType entityType = EntityType.valueOf(type);
                                    if (entityType != null) {
                                        if (entities.containsKey(uuid)) {
                                            entities.get(uuid).remove(); // clean slate
                                        }
                                        Entity e = w.spawnEntity(new Location(w, x, y, z), entityType);
                                        if (stateSerial != null) {
                                            EntityStateSerializer.deserializeState(e, stateSerial);
                                        }
                                    } else {
                                        SteelCore.logWarning("Invalid entity type for rollback record with ID " + id
                                                + " in arena " + getArena().getId());
                                    }
                                    break;
                                default:
                                    SteelCore.logWarning("Invalid rollback record type at ID " + id);
                            }

                        } else {
                            SteelCore.logVerbose("Rollback record with ID " + id + " in arena " + getArena().getId()
                                    + " has a mismtching world name - refusing to roll back");
                        }
                    } catch (InvalidConfigurationException | IOException | SQLException ex) {
                        SteelCore.logSevere("Failed to read rollback record in arena " + getArena().getId());
                        ex.printStackTrace();
                    }
                }
            }
            //noinspection ResultOfMethodCallIgnored
            rollbackStore.delete();
            //noinspection ResultOfMethodCallIgnored
            stateStore.delete();
        } else {
            throw new IllegalArgumentException("Rollback store does not exist");
        }
    }

    private static Optional<Arena> checkChangeAtLocation(Location location) {
        for (Minigame mg : SteelCore.getMinigames().values()) {
            for (Arena arena : mg.getArenas()) {
                if (arena.getWorld().equals(location.getWorld().getName())) {
                    if (arena.getBoundary().contains(
                            LocationHelper.convertLocation(location))) {
                        return Optional.of(arena);
                    }
                }
            }
        }
        return Optional.absent();
    }

    public static void checkBlockChange(Location location, BlockState state, Event event) {
        Optional<Arena> arena = checkChangeAtLocation(location);
        if (arena.isPresent()) {
            try {
                ((SteelArena) arena.get()).getRollbackHelper().logBlockChange(location, state);
            } catch (InvalidConfigurationException | IOException | SQLException ex) {
                throw new RuntimeException("Failed to log " + event.getEventName() + " for rollback in arena "
                        + arena.get().getName(), ex);
            }
        }
    }

    public static void checkEntityChange(Entity entity, boolean newlyCreated, Event event) {
        Optional<Arena> arena = checkChangeAtLocation(entity.getLocation());
        if (arena.isPresent()) {
            try {
                if (newlyCreated) {
                    ((SteelArena) arena.get()).getRollbackHelper().logEntityCreation(entity);
                } else {
                    ((SteelArena) arena.get()).getRollbackHelper().logEntityChange(entity);
                }
            } catch (InvalidConfigurationException | IOException | SQLException ex) {
                throw new RuntimeException("Failed to log " + event.getEventName() + " for rollback in arena "
                        + arena.get().getName(), ex);
            }
        }
    }

}
