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
package net.caseif.flint.steel.util.helper;

import net.caseif.flint.Minigame;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.arena.SteelArena;
import net.caseif.flint.steel.util.MiscUtil;
import net.caseif.flint.steel.util.io.DataFiles;

import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Helper class for rollback functionality.
 *
 * @author Max Roncacé
 */
public final class RollbackHelper {

    public static final String ROLLBACK_STORE_BLOCK_TABLE = "blocks";
    public static final String SQLITE_PROTOCOL = "jdbc:sqlite:";
    public static final Properties SQL_QUERIES = new Properties();

    private SteelArena arena;
    private File rollbackStore = new File(DataFiles.ARENA_STORE.getFile(getArena().getMinigame()),
            getArena().getId().concat(".db"));

    static {
        try (InputStream is = RollbackHelper.class.getResourceAsStream("sql-queries.properties")) {
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
        if (rollbackStore.exists()) {
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        rollbackStore.createNewFile();
        try (Connection conn = DriverManager.getConnection(SQLITE_PROTOCOL + rollbackStore.getAbsolutePath())) {
            try (
                    PreparedStatement st = conn.prepareStatement(SQL_QUERIES.getProperty("create-rollback-table")
                            .replace("{table}", ROLLBACK_STORE_BLOCK_TABLE));
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
        if (!rollbackStore.exists()) {
            //noinspection ResultOfMethodCallIgnored
            rollbackStore.createNewFile();
        }
        try (
                Connection conn = DriverManager.getConnection("jdbc:sqlite:" + rollbackStore.getPath());
                Statement st = conn.createStatement();
        ) {
            try (ResultSet rs = st.executeQuery(SQL_QUERIES.getProperty("query-rollback-table")
                            .replace("{world}", location.getWorld().getName())
                            .replace("{x}", "" + location.getBlockX())
                            .replace("{y}", "" + location.getBlockY())
                            .replace("{z}", "" + location.getBlockZ())
            )) {
                if (!rs.next()) { // if no results
                    Optional<ConfigurationSection> state = BlockStateSerializer.serializeState(originalState);
                    st.executeUpdate(SQL_QUERIES.getProperty("insert-rollback-record")
                                    .replace("{world}", location.getWorld().getName())
                                    .replace("{x}", "" + location.getBlockX())
                                    .replace("{y}", "" + location.getBlockY())
                                    .replace("{z}", "" + location.getBlockZ())
                                    .replace("{type}", originalState.getType().name())
                                    .replace("{data}", "" + originalState.getRawData())
                                    .replace("{state}", "" + (state.isPresent() ? 1 : 0)),
                            Statement.RETURN_GENERATED_KEYS);
                    if (state.isPresent()) {
                        try (ResultSet gen = st.getGeneratedKeys()) {
                            if (gen.next()) {
                                int id = gen.getInt(1);
                                File stateStore = new File(
                                        DataFiles.ROLLBACK_STATE_DIR.getFile(getArena().getMinigame()),
                                        getArena().getId().concat(".yml"));
                                YamlConfiguration yaml = new YamlConfiguration();
                                yaml.load(stateStore);
                                if (yaml.isSet(Integer.toString(id))) {
                                    throw new IllegalStateException("Tried to store state with id " + id + ", but "
                                            + "index was already present in rollback store! Something's gone terribly "
                                            + "wrong."); // technically should never happen but you never know
                                }
                                yaml.set(Integer.toString(id), state.get());
                                yaml.save(stateStore);
                            } else {
                                throw new SQLException("Failed to get generated key from INSERT query");
                            }
                        }
                    }
                } // else: do nothing since it's already been changed from its original state
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void popRollbacks() throws SQLException {
        if (rollbackStore.exists()) {
            try (
                    Connection conn = DriverManager.getConnection(SQLITE_PROTOCOL + rollbackStore.getAbsolutePath());
                    PreparedStatement st = conn.prepareStatement(SQL_QUERIES.getProperty("get-all-rollbacks")
                            .replace("{table}", ROLLBACK_STORE_BLOCK_TABLE));
                    ResultSet rs = st.executeQuery();
            ) {
                World w = Bukkit.getWorld(getArena().getWorld());
                while (rs.next()) {
                    try {
                        int id = rs.getInt("id");
                        String world = rs.getString("world");
                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        int z = rs.getInt("z");
                        String type = rs.getString("type");
                        int data = rs.getInt("data");
                        boolean state = rs.getBoolean("state");
                        if (world.equals(getArena().getWorld())) {
                            Block b = w.getBlockAt(x, y, z);
                            Material m = Material.valueOf(type);
                            if (m != null) {
                                b.setType(m);
                                b.setData((byte) data);
                                if (state) {
                                    File stateStore = new File(
                                            DataFiles.ROLLBACK_STATE_DIR.getFile(getArena().getMinigame()),
                                            getArena().getId().concat(".yml"));
                                    YamlConfiguration yaml = new YamlConfiguration();
                                    yaml.load(stateStore);
                                    if (yaml.isConfigurationSection("" + id)) {
                                        BlockStateSerializer
                                                .deserializeState(b, yaml.getConfigurationSection(getArena().getId()));
                                        yaml.set("" + id, null); // clear state log
                                    } else {
                                        SteelCore.logVerbose("Rollback record with ID " + id + " was marked as having "
                                                + "block entity state, but no corresponding state configuration was "
                                                + "found");
                                    }
                                }
                            } else {
                                SteelCore.logWarning("Rollback record with ID " + id + " in arena " + getArena().getId()
                                        + " cannot be matched to a Material");
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
        } else {
            throw new IllegalArgumentException("Rollback store does not exist");
        }
    }

    public static void checkBlockChange(Location location, BlockState originalState) {
        for (Minigame mg : SteelCore.getMinigames().values()) {
            for (Arena arena : mg.getArenas()) {
                if (arena.getWorld().equals(location.getWorld().getName())) {
                    if (arena.getBoundary().contains(
                            MiscUtil.convertLocation(location))) {
                        try {
                            ((SteelArena) arena).getRollbackHelper().logBlockChange(location, originalState);
                        } catch (InvalidConfigurationException | IOException | SQLException ex) {
                            throw new RuntimeException("Failed to log block change for rollback in arena "
                                    + arena.getName(), ex);
                        }
                    }
                }
            }
        }
    }

}
