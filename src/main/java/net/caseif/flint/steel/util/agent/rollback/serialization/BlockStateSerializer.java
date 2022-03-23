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

package net.caseif.flint.steel.util.agent.rollback.serialization;

import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.util.helper.InventoryHelper;

import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.FlowerPot;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.List;

/**
 * Static utility class for serialization of block entity state.
 *
 * @author Max Roncacé
 */
public abstract class BlockStateSerializer {

    private static final String INVENTORY_KEY = "inventory";

    private static final String SIGN_LINES_KEY = "lines";

    @Deprecated
    private static final String BANNER_BASE_COLOR_KEY = "base-color";
    private static final String BANNER_PATTERNS_KEY = "patterns";
    private static final String BANNER_PATTERN_COLOR_KEY = "color";
    private static final String BANNER_PATTERN_TYPE_KEY = "type";

    private static final String SPAWNER_TYPE_KEY = "spawner-type";
    private static final String SPAWNER_DELAY_KEY = "spawner-delay";

    @Deprecated
    private static final String NOTE_OCTAVE_KEY = "octave";
    @Deprecated
    private static final String NOTE_TONE_KEY = "tone";
    @Deprecated
    private static final String NOTE_SHARPED_KEY = "sharped";

    private static final String JUKEBOX_DISC_KEY = "disc";

    private static final String SKULL_OWNER_KEY = "owner";
    @Deprecated
    private static final String SKULL_ROTATION_KEY = "rotation";

    private static final String COMMAND_NAME_KEY = "cmd-name";
    private static final String COMMAND_CMD_KEY = "command";

    @Deprecated
    private static final String FLOWER_TYPE_KEY = "flower-type";
    @Deprecated
    private static final String FLOWER_DATA_KEY = "flower-data";

    private static final String BLOCK_DATA_KEY = "block-data";

    public static Optional<String> serializeState(Block block) {
        YamlConfiguration yaml = new YamlConfiguration();

        // http://minecraft.gamepedia.com/Block_entity was used as a reference for this method

        BlockState state = block.getState();

        if (state instanceof InventoryHolder) {
            yaml.set(INVENTORY_KEY, InventoryHelper.serializeInventory(((InventoryHolder) state).getInventory()));
        }

        if (state instanceof Sign) {
            yaml.set(SIGN_LINES_KEY, Arrays.asList(((Sign) state).getLines()));
        } else if (state instanceof Banner) {
            ConfigurationSection patternSection = yaml.createSection(BANNER_PATTERNS_KEY);
            List<Pattern> patterns = ((Banner) state).getPatterns();
            for (int i = 0; i < patterns.size(); i++) {
                ConfigurationSection subSection = patternSection.createSection("" + i);
                subSection.set(BANNER_PATTERN_COLOR_KEY, patterns.get(i).getColor().name());
                subSection.set(BANNER_PATTERN_TYPE_KEY, patterns.get(i).getPattern().name());
            }
        } else if (state instanceof CreatureSpawner) {
            yaml.set(SPAWNER_TYPE_KEY, ((CreatureSpawner) state).getSpawnedType().name());
            yaml.set(SPAWNER_DELAY_KEY, ((CreatureSpawner) state).getDelay());
        } else if (state instanceof Jukebox) {
            if (((Jukebox) state).isPlaying()) {
                yaml.set(JUKEBOX_DISC_KEY, ((Jukebox) state).getPlaying());
            }
        } else if (state instanceof Skull) {
            yaml.set(SKULL_OWNER_KEY, ((Skull) state).getOwner());
        } else if (state instanceof CommandBlock) {
            yaml.set(COMMAND_NAME_KEY, ((CommandBlock) state).getName());
            yaml.set(COMMAND_CMD_KEY, ((CommandBlock) state).getCommand());
        }

        if (SteelCore.isLegacyMinecraftVersion()) {
            if (state instanceof Banner) {
                yaml.set(BANNER_BASE_COLOR_KEY, ((Banner) state).getBaseColor().name());
            } else if (state instanceof NoteBlock) {
                yaml.set(NOTE_OCTAVE_KEY, ((NoteBlock) state).getNote().getOctave());
                yaml.set(NOTE_TONE_KEY, ((NoteBlock) state).getNote().getTone().name());
                yaml.set(NOTE_SHARPED_KEY, ((NoteBlock) state).getNote().isSharped());
            } else if (state instanceof Skull) {
                yaml.set(SKULL_ROTATION_KEY, ((Skull) state).getRotation().name());
            } else if (state instanceof FlowerPot) {
                yaml.set(FLOWER_TYPE_KEY, ((FlowerPot) state).getContents().getItemType().name());
                yaml.set(FLOWER_DATA_KEY, ((FlowerPot) state).getContents().getData());
            }
        } else {
            yaml.set(BLOCK_DATA_KEY, block.getBlockData().getAsString());
        }

        if (yaml.getKeys(false).size() > 0) {
            return Optional.of(yaml.saveToString());
        }
        return Optional.absent();
    }

    public static void deserializeState(Block block, String serial) throws InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(serial);

        boolean hasBlockData = false;

        if (!SteelCore.isLegacyMinecraftVersion()) {
            if (yaml.isString(BLOCK_DATA_KEY)) {
                hasBlockData = true;

                block.setBlockData(Bukkit.createBlockData(yaml.getString(BLOCK_DATA_KEY)));
            }
        }

        BlockState state = block.getState();
        boolean missingData = false;
        boolean malformedData = false;

        if (state instanceof InventoryHolder) {
            if (yaml.isConfigurationSection(INVENTORY_KEY)) {
                ((InventoryHolder) state).getInventory().setContents(
                        InventoryHelper.deserializeInventory(yaml.getConfigurationSection(INVENTORY_KEY))
                );
            }
        }

        boolean recognizedState = true;
        if (state instanceof Sign) {
            if (yaml.isList(SIGN_LINES_KEY)) {
                List<String> lines = yaml.getStringList(SIGN_LINES_KEY);
                for (int i = 0; i < lines.size(); i++) {
                    ((Sign) state).setLine(i, lines.get(i));
                }
            } else {
                missingData = true;
            }
        } else if (state instanceof Banner) {
            if (yaml.isConfigurationSection(BANNER_PATTERNS_KEY)) {
                ConfigurationSection patterns = yaml.getConfigurationSection(BANNER_PATTERNS_KEY);
                for (String key : patterns.getKeys(false)) {
                    ConfigurationSection subSection = patterns.getConfigurationSection(key);
                    try {
                        DyeColor color = DyeColor.valueOf(subSection.getString(BANNER_PATTERN_COLOR_KEY));
                        PatternType type = PatternType.valueOf(subSection.getString(BANNER_PATTERN_TYPE_KEY));
                        ((Banner) state).addPattern(new Pattern(color, type));
                    } catch (IllegalArgumentException ex) {
                        malformedData = true;
                    }
                }
            } else {
                missingData = true;
            }
        } else if (state instanceof CreatureSpawner) {
            if (yaml.isSet(SPAWNER_TYPE_KEY)) {
                try {
                    EntityType type = EntityType.valueOf(yaml.getString(SPAWNER_TYPE_KEY));
                    ((CreatureSpawner) state).setSpawnedType(type);
                } catch (IllegalArgumentException ex) {
                    malformedData = true;
                }
            } else {
                missingData = true;
            }
        } else if (state instanceof Jukebox) {
            if (yaml.isSet(JUKEBOX_DISC_KEY)) {
                try {
                    Material disc = Material.valueOf(yaml.getString(JUKEBOX_DISC_KEY));
                    ((Jukebox) state).setPlaying(disc);
                } catch (IllegalArgumentException ex) {
                    malformedData = true;
                }
            } else {
                missingData = true;
            }
        } else if (state instanceof Skull) {
            if (yaml.isSet(SKULL_OWNER_KEY)) {
                ((Skull) state).setOwner(yaml.getString(SKULL_OWNER_KEY));
            }
        } else if (state instanceof CommandBlock) {
            if (yaml.isSet(COMMAND_CMD_KEY)) {
                ((CommandBlock) state).setCommand(yaml.getString(COMMAND_CMD_KEY));
            } else {
                missingData = true;
            }
            if (yaml.isSet(COMMAND_NAME_KEY)) {
                ((CommandBlock) state).setName(yaml.getString(COMMAND_NAME_KEY));
            } else {
                missingData = true;
            }
        } else if (!(state instanceof InventoryHolder)) {
            recognizedState = false;

            if (!hasBlockData) {
                SteelCore.logWarning("Failed to deserialize state data for rollback record for block at {"
                        + block.getX() + ", " + block.getY() + ", " + block.getZ() + "}");
            }
        }

        if (SteelCore.isLegacyMinecraftVersion()) {
            if (state instanceof Banner) {
                if (yaml.isSet(BANNER_BASE_COLOR_KEY)) {
                    try {
                        DyeColor color = DyeColor.valueOf(yaml.getString(BANNER_BASE_COLOR_KEY));
                        ((Banner) state).setBaseColor(color);
                    } catch (IllegalArgumentException ex) {
                        malformedData = true;
                    }
                } else {
                    missingData = true;
                }
            } else if (state instanceof NoteBlock) {
                if (yaml.isInt(NOTE_OCTAVE_KEY) && yaml.isSet(NOTE_TONE_KEY)) {
                    try {
                        Note.Tone tone = Note.Tone.valueOf(yaml.getString(NOTE_TONE_KEY));
                        ((NoteBlock) state).setNote(
                                new Note(yaml.getInt(NOTE_OCTAVE_KEY), tone, yaml.getBoolean(NOTE_SHARPED_KEY))
                        );
                    } catch (IllegalArgumentException ex) {
                        malformedData = true;
                    }
                } else {
                    missingData = true;
                }
            } else if (state instanceof Skull) {
                if (yaml.isSet(SKULL_ROTATION_KEY)) {
                    try {
                        BlockFace face = BlockFace.valueOf(yaml.getString(SKULL_ROTATION_KEY));
                        ((Skull) state).setRotation(face);
                    } catch (IllegalArgumentException ex) {
                        malformedData = true;
                    }
                } else {
                    missingData = true;
                }
            } else if (state instanceof FlowerPot) {
                if (yaml.isSet(FLOWER_TYPE_KEY)) {
                    try {
                        byte data = yaml.isSet(FLOWER_DATA_KEY) ? (byte) yaml.getInt(FLOWER_DATA_KEY) : 0x0;
                        Material type = Material.valueOf(yaml.getString(FLOWER_TYPE_KEY));
                        ((FlowerPot) state).setContents(new MaterialData(type, data));
                    } catch (IllegalArgumentException ex) {
                        malformedData = true;
                    }
                } else {
                    missingData = true;
                }
            }
        }

        if (recognizedState) {
            state.update(true);
        }

        if (missingData) {
            SteelCore.logVerbose("Block with type " + block.getType().name() + " at {" + block.getX() + ", "
                    + block.getY() + ", " + block.getZ() + "} is missing important state data");
        }
        if (malformedData) {
            SteelCore.logVerbose("Block with type " + block.getType().name() + " at {" + block.getX() + ", "
                    + block.getY() + ", " + block.getZ() + "} has malformed state data");
        }
    }

}
