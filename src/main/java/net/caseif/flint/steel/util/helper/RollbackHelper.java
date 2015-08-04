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
import org.bukkit.Location;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.FlowerPot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/**
 * Helper class for rollback functionality.
 *
 * @author Max Roncacé
 */
public final class RollbackHelper {

    public static final String ROLLBACK_STORE_BLOCK_TABLE = "blocks";

    public static Properties SQL_QUERIES = new Properties();

    private SteelArena arena;

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
        File file = new File(DataFiles.ROLLBACK_PROFILE_DIR.getFile(arena.getMinigame()), arena.getId() + ".db");
        if (file.exists()) {
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file.getPath())) {
            try (
                    PreparedStatement st = conn.prepareStatement(SQL_QUERIES.getProperty("create-rollback-table")
                            .replace("{table}", ROLLBACK_STORE_BLOCK_TABLE));
            ) {
                st.execute();
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
        File rollbackStore = new File(DataFiles.ARENA_STORE.getFile(getArena().getMinigame()), getArena().getId()
                .concat(".db"));
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
                    st.executeUpdate(SQL_QUERIES.getProperty("insert-rollback-record")
                                    .replace("{world}", location.getWorld().getName())
                                    .replace("{x}", "" + location.getBlockX())
                                    .replace("{y}", "" + location.getBlockY())
                                    .replace("{z}", "" + location.getBlockZ())
                                    .replace("{type}", originalState.getType().name())
                                    .replace("{data}", "" + originalState.getRawData()),
                            Statement.RETURN_GENERATED_KEYS);
                    Optional<ConfigurationSection> state = serializeState(originalState);
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
    private Optional<ConfigurationSection> serializeState(BlockState state) {
        ConfigurationSection cs = new YamlConfiguration().createSection("thank mr skeltal");

        // http://minecraft.gamepedia.com/Block_entity was used as a reference for this method

        if (state instanceof InventoryHolder) {
            cs.set("inventory", InventoryHelper.serializeInventory(((InventoryHolder) state).getInventory()));
        }

        if (state instanceof Sign) {
            for (int i = 0; i < ((Sign) state).getLines().length; i++) {
                cs.set("" + i, ((Sign) state).getLine(i));
            }
        } else if (state instanceof Banner) {
            cs.set("base", ((Banner) state).getBaseColor().name());
            ConfigurationSection patternSection = cs.createSection("patterns");
            List<Pattern> patterns = ((Banner) state).getPatterns();
            for (int i = 0; i < patterns.size(); i++) {
                ConfigurationSection subSection = patternSection.createSection("" + i);
                subSection.set("color", patterns.get(i).getColor().name());
                subSection.set("type", patterns.get(i).getPattern().name());
            }
        } else if (state instanceof CreatureSpawner) {
            cs.set("type", ((CreatureSpawner) state).getSpawnedType().name());
            cs.set("delay", ((CreatureSpawner) state).getDelay());
        } else if (state instanceof NoteBlock) {
            cs.set("octave", ((NoteBlock) state).getNote().getOctave());
            cs.set("tone", ((NoteBlock) state).getNote().getTone().name());
        } else if (state instanceof Jukebox) {
            if (((Jukebox) state).isPlaying()) {
                cs.set("disc", ((Jukebox) state).getPlaying());
            }
        }
        else if (state instanceof Skull) {
            cs.set("owner", ((Skull) state).getOwner());
            cs.set("rotation", ((Skull) state).getRotation());
        } else if (state instanceof CommandBlock) {
            cs.set("name", ((CommandBlock) state).getName());
            cs.set("command", ((CommandBlock) state).getCommand());
        } else if (state instanceof FlowerPot) {
            cs.set("type", ((FlowerPot) state).getContents().getItemType().name());
            cs.set("data", ((FlowerPot) state).getContents().getData());
        }

        if (cs.getKeys(false).size() > 0) {
            return Optional.of(cs);
        } else {
            return Optional.absent();
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
