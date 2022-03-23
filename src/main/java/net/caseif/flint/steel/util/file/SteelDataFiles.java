/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2022, Max Roncace <me@caseif.net>
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

import net.caseif.flint.common.util.file.CommonDataFiles;
import net.caseif.flint.common.util.file.CoreDataFile;
import net.caseif.flint.common.util.file.MinigameDataFile;

/**
 * Utility class for working with Flint data files.
 */
public class SteelDataFiles extends CommonDataFiles {

    public static final CoreDataFile TELEMETRY_UUID_STORE = new CoreDataFile("uuid.txt");

    // for old file formats
    public static final CoreDataFile CORE_OLD_DATA_DIR = new CoreDataFile("old", true, false);
    public static final CoreDataFile OLD_OFFLINE_PLAYER_STORE = new CoreDataFile("offline_players.yml", false, false);
    public static final CoreDataFile OLD_PLAYER_LOCATION_STORE = new CoreDataFile("locs.yml", false, false);

    public static final MinigameDataFile MG_OLD_DATA_DIR = new MinigameDataFile("old", true, false);
    public static final MinigameDataFile OLD_ARENA_STORE = new MinigameDataFile("arenas.yml", false, false);
    public static final MinigameDataFile OLD_LOBBY_STORE = new MinigameDataFile("lobbies.yml", false, false);

}
