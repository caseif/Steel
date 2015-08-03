package net.caseif.flint.steel.util.helper;

import net.caseif.flint.steel.arena.SteelArena;
import net.caseif.flint.steel.util.io.DataFiles;

import org.bukkit.Location;
import org.bukkit.block.BlockState;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Static helper class for rollback functionality.
 *
 * @author Max Roncac�
 */
public final class RollbackHelper {

    public static final String ROLLBACK_STORE_BLOCK_TABLE = "blocks";

    private SteelArena arena;

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
    public static void createRollbackDatabase(SteelArena arena) throws IOException, SQLException {
        File file = new File(DataFiles.ROLLBACK_PROFILE_DIR.getFile(arena.getMinigame()), arena.getId() + ".db");
        if (file.exists()) {
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file.getPath())) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS `" + ROLLBACK_STORE_BLOCK_TABLE + "` ("
                        + "`id` INTEGER NOT NULL PRIMARY KEY,"
                        + "`world` VARCHAR"
                        + "`x` INTEGER NOT NULL,"
                        + "`y` INTEGER NOT NULL,"
                        + "`z` INTEGER NOT NULL,"
                        + "`type` VARCHAR(32) NOT NULL,"
                        + "`data` INTEGER NOT NULL"
                        + ")");
            }
        }
    }

    /**
     * Logs a block change at the given location.
     *
     * @param location The location of the change
     * @param originalState The state of the block before the change
     * @throws IOException If an exception occurs while reading to or from the
     *     rollback database
     * @throws SQLException If an exception occurs while manipulating the
     *     rollback database
     */
    @SuppressWarnings("deprecation")
    public void logBlockChange(Location location, BlockState originalState)
            throws IOException, SQLException {
        File rollbackStore = new File(DataFiles.ARENA_STORE.getFile(getArena().getMinigame()), getArena().getId()
                + ".db");
        if (!rollbackStore.exists()) {
            //noinspection ResultOfMethodCallIgnored
            rollbackStore.createNewFile();
        }
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + rollbackStore.getPath())) {
            Statement st = conn.createStatement();
            try (ResultSet rs = st.executeQuery("SELECT * FROM `" + RollbackHelper.ROLLBACK_STORE_BLOCK_TABLE + "`"
                    + " WHERE world='" + location.getWorld().getName() + "'"
                    + " && x=" + location.getX()
                    + " && y=" + location.getY()
                    + " && z=" + location.getZ())) {
                if (!rs.next()) { // if no results
                    st.executeUpdate("INSERT INTO `" + ROLLBACK_STORE_BLOCK_TABLE + "` "
                            + "(`world`, `x`, `y`, `z`, `type`, `data`) VALUES ("
                            + "'" + location.getWorld().getName() + "',"
                            + location.getBlockX() + ","
                            + location.getBlockY() + ","
                            + location.getBlockZ() + ","
                            + "'" + originalState.getType().name() + "',"
                            + originalState.getRawData()
                            + ")");
                    //TODO: store state in separate file
                } // else: do nothing since it's already been changed from its original state
            }
        }
    }

}
