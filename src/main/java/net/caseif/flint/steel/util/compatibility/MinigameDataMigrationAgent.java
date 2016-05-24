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

import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.metadata.persist.CommonPersistentMetadata;
import net.caseif.flint.common.util.helper.MetadataHelper;
import net.caseif.flint.metadata.persist.PersistentMetadata;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.util.file.SteelDataFiles;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Utility class for migrating data from older revisions of the plugin to the
 * current format.
 */
public class MinigameDataMigrationAgent extends DataMigrationAgent {

    private final Minigame mg;

    public MinigameDataMigrationAgent(Minigame mg) {
        this.mg = mg;
    }

    @Override
    public void migrateData() {
        migrateArenaStore();
    }

    private void migrateArenaStore() {
        File oldFile = SteelDataFiles.OLD_ARENA_STORE.getFile(mg);
        if (oldFile.exists()) {
            SteelCore.logInfo("Detected old arena store for minigame " + mg.getPlugin() + ". Attempting to migrate...");
            File newFile = SteelDataFiles.ARENA_STORE.getFile(mg);
            try {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.load(oldFile);

                JsonObject json = new JsonObject();

                for (String key : yaml.getKeys(false)) {
                    if (!yaml.isConfigurationSection(key)) {
                        SteelCore.logWarning("Found invalid key \"" + key + "\" in old arena store. Not migrating...");
                        continue;
                    }

                    ConfigurationSection arenaSec = yaml.getConfigurationSection(key);
                    String name = arenaSec.getString(CommonArena.PERSISTENCE_NAME_KEY);
                    String world = arenaSec.getString(CommonArena.PERSISTENCE_WORLD_KEY);
                    ConfigurationSection spawns = arenaSec.getConfigurationSection(CommonArena.PERSISTENCE_SPAWNS_KEY);
                    String lowBound = arenaSec.getString(CommonArena.PERSISTENCE_BOUNDS_LOWER_KEY);
                    String highBound = arenaSec.getString(CommonArena.PERSISTENCE_BOUNDS_UPPER_KEY);

                    JsonObject spawnsJson = new JsonObject();
                    for (String spawnKey : spawns.getKeys(false)) {
                        spawnsJson.add(spawnKey, new JsonPrimitive(spawns.getString(spawnKey)));
                    }

                    JsonObject arenaJson = new JsonObject();
                    arenaJson.addProperty(CommonArena.PERSISTENCE_NAME_KEY, name);
                    arenaJson.addProperty(CommonArena.PERSISTENCE_WORLD_KEY, world);
                    arenaJson.add(CommonArena.PERSISTENCE_SPAWNS_KEY, spawnsJson);
                    arenaJson.addProperty(CommonArena.PERSISTENCE_BOUNDS_LOWER_KEY, lowBound);
                    arenaJson.addProperty(CommonArena.PERSISTENCE_BOUNDS_UPPER_KEY, highBound);

                    ConfigurationSection metadataSec
                            = arenaSec.getConfigurationSection(CommonArena.PERSISTENCE_METADATA_KEY);
                    PersistentMetadata metadata = metadataSec != null ? buildMetadata(metadataSec) : null;
                    if (metadata != null) {
                        JsonObject metaJson = new JsonObject();
                        MetadataHelper.storeMetadata(metaJson, metadata);
                        arenaJson.add(CommonArena.PERSISTENCE_METADATA_KEY, metaJson);
                    }

                    json.add(key, arenaJson);
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
        }
    }

    @Override
    protected File getOldDir() {
        return SteelDataFiles.MG_OLD_DATA_DIR.getFile(mg);
    }

    private PersistentMetadata buildMetadata(ConfigurationSection cs) {
        PersistentMetadata meta = new CommonPersistentMetadata();
        for (String key : cs.getKeys(false)) {
            if (cs.isConfigurationSection(key)) {
                meta.set(key, buildMetadata(cs.getConfigurationSection(key)));
            } else {
                meta.set(key, cs.getString(key));
            }
        }

        return meta;
    }

}
