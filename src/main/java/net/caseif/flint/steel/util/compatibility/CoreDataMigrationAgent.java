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
package net.caseif.flint.steel.util.compatibility;

import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.util.file.SteelDataFiles;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Utility class for migrating data from older revisions of the plugin to the
 * current format.
 */
public class CoreDataMigrationAgent implements DataMigrationAgent {

    private static final String OFFLINE_PLAYER_LIST_KEY = "offline";

    @Override
    public void migrateData() {
        migrateOfflinePlayerStore();
        migrateLocationStore();
    }

    private void migrateOfflinePlayerStore() {
        File oldFile = SteelDataFiles.OLD_OFFLINE_PLAYER_STORE.getFile();
        if (oldFile.exists()) {
            SteelCore.logInfo("Detected old offline player store. Attempting to migrate...");
            File newFile = SteelDataFiles.OFFLINE_PLAYER_STORE.getFile();
            try {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.load(oldFile);

                List<String> uuidList = yaml.getStringList(OFFLINE_PLAYER_LIST_KEY);
                JsonArray json = new JsonArray();
                for (String uuid : uuidList) {
                    json.add(new JsonPrimitive(uuid));
                }

                Files.deleteIfExists(newFile.toPath());
                Files.createFile(newFile.toPath());
                try (FileWriter writer = new FileWriter(newFile)) {
                    writer.write(json.toString());
                }
            } catch (InvalidConfigurationException | IOException ex) {
                SteelCore.logSevere("Failed to migrate offline player store!");
                ex.printStackTrace();
            }

            relocateOldFile(oldFile.toPath());
            SteelCore.logInfo("Old file has been relocated to "
                    + SteelDataFiles.OLD_OFFLINE_PLAYER_STORE.getFile().toPath().toString() + ".");
        }
    }

    private void migrateLocationStore() {
        File oldFile = SteelDataFiles.OLD_PLAYER_LOCATION_STORE.getFile();
        if (oldFile.exists()) {
            SteelCore.logInfo("Detected old location store. Attempting to migrate...");
            File newFile = SteelDataFiles.PLAYER_LOCATION_STORE.getFile();
            try {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.load(oldFile);

                JsonObject json = new JsonObject();
                for (String key : yaml.getKeys(false)) {
                    json.addProperty(key, yaml.getString(key));
                }

                Files.deleteIfExists(newFile.toPath());
                Files.createFile(newFile.toPath());
                try (FileWriter writer = new FileWriter(newFile)) {
                    writer.write(json.toString());
                }
            } catch (InvalidConfigurationException | IOException ex) {
                SteelCore.logSevere("Failed to migrate location store!");
                ex.printStackTrace();
            }

            relocateOldFile(oldFile.toPath());
            SteelCore.logInfo("Old file has been relocated to "
                    + SteelDataFiles.OLD_PLAYER_LOCATION_STORE.getFile().toPath().toString() + ".");
        }
    }

    private void relocateOldFile(Path oldFile) {
        try {
            Path oldDir = SteelDataFiles.CORE_OLD_DATA_DIR.getFile().toPath();
            if (!Files.exists(oldDir)) {
                Files.createDirectory(oldDir);
            }

            Path copyPath = oldDir.resolve(oldFile.getFileName());
            Files.deleteIfExists(copyPath);
            Files.move(oldFile, copyPath);
        } catch (IOException ex) { // we're fucked, basically
            SteelCore.logSevere("Failed to relocate " + oldFile.getFileName().toString() + "! This is very bad.");
            throw new RuntimeException(ex);
        }
    }

}
