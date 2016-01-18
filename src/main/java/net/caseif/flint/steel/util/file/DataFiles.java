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
package net.caseif.flint.steel.util.file;

import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.steel.SteelCore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for working with Flint data files.
 */
public class DataFiles {

    static final List<DataFile> FILES = new ArrayList<>();

    static final String ROOT_DATA_DIR = "flint_data";

    public static final CoreDataFile OFFLINE_PLAYER_STORE = new CoreDataFile("offline_players.yml");
    public static final CoreDataFile PLAYER_INVENTORY_DIR = new CoreDataFile("inventories", true);
    public static final CoreDataFile PLAYER_LOCATION_STORE = new CoreDataFile("locs.yml");
    public static final CoreDataFile TELEMETRY_UUID_STORE = new CoreDataFile("uuid.txt");

    public static final MinigameDataFile ARENA_STORE = new MinigameDataFile("arenas.yml");
    public static final MinigameDataFile LOBBY_STORE = new MinigameDataFile("lobbies.yml");
    public static final MinigameDataFile ROLLBACK_STORE = new MinigameDataFile("rollback.db");
    public static final MinigameDataFile ROLLBACK_STATE_STORE = new MinigameDataFile("rollback_state.json");

    static void register(DataFile dataFile) {
        FILES.add(dataFile);
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
        for (DataFile df : FILES) {
            if ((minigame != null && df instanceof MinigameDataFile)
                    || (minigame == null && df instanceof CoreDataFile)) {
                File file = minigame != null
                        ? ((MinigameDataFile) df).getFile(minigame)
                        : ((CoreDataFile) df).getFile();
                if (file.isDirectory() != df.isDirectory()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
                if (!file.exists()) {
                    boolean result = false;
                    try {
                        boolean parent = true;
                        if (!file.getParentFile().exists()) {
                            parent = file.getParentFile().mkdirs();
                        }
                        if (parent) {
                            if (df.isDirectory()) {
                                result = file.mkdir();
                            } else {
                                result = file.createNewFile();
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        result = false;
                    }
                    if (!result) {
                        SteelCore.logSevere("Failed to create " + (minigame == null ? "core" : "minigame")
                                + " data file " + ROOT_DATA_DIR + File.separatorChar + df.getFileName());
                    }
                }
            }
        }
    }

}
