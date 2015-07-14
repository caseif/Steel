package net.caseif.flint.steel.util.io;

import net.caseif.flint.Minigame;
import net.caseif.flint.steel.SteelCore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for working with Flint data files.
 */
public class DataFiles {

    static final String ROOT_DATA_DIR = "flint_data";

    public static final CoreDataFile OFFLINE_PLAYER_STORE = new CoreDataFile("offline_players.yml");
    public static final CoreDataFile PLAYER_INVENTORY_DIR = new CoreDataFile("inventories");
    public static final CoreDataFile PLAYER_LOCATION_STORE = new CoreDataFile("locs.yml");

    static final List<DataFile> files = new ArrayList<>();

    static void register(DataFile dataFile) {
        files.add(dataFile);
    }

    /**
     * Creates non-existent {@link CoreDataFile}s.
     */
    public static void createCoreDataFiles() {
        createMinigameDataFiles(null);
    }

    /**
     * Creates non-existent {@link MinigameDataFile}s for the given
     * {@link Minigame}.
     *
     * @param minigame The {@link Minigame} to create {@link MinigameDataFile}s
     *     for
     */
    public static void createMinigameDataFiles(Minigame minigame) {
        for (DataFile df : files) {
            if ((minigame != null && df instanceof MinigameDataFile)
                    || (minigame == null && df instanceof CoreDataFile)) {
                File file = minigame != null ? ((MinigameDataFile)df).getFile(minigame) : ((CoreDataFile)df).getFile();
                if (!file.exists()) {
                    boolean result;
                    try {
                        if (file.isDirectory()) {
                            result = file.mkdir();
                        } else {
                            result = file.createNewFile();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        result = false;
                    }
                    if (!result) {
                        SteelCore.logSevere("Failed to create " + ROOT_DATA_DIR + File.pathSeparatorChar
                                + df.getFileName());
                    }
                }
            }
        }
    }

}
