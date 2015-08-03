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
    public static final CoreDataFile PLAYER_INVENTORY_DIR = new CoreDataFile("inventories", true);
    public static final CoreDataFile PLAYER_LOCATION_STORE = new CoreDataFile("locs.yml");

    public static final MinigameDataFile ARENA_STORE = new MinigameDataFile("arenas.yml");

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
