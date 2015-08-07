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

import net.caseif.flint.steel.SteelCore;

import com.google.common.base.Optional;
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
public class BlockStateSerializer {

    private static final String INVENTORY_KEY = "inventory";

    private static final String SIGN_LINES_KEY = "lines";

    private static final String BANNER_BASE_COLOR_KEY = "base-color";
    private static final String BANNER_PATTERNS_KEY = "patterns";
    private static final String BANNER_PATTERN_COLOR_KEY = "color";
    private static final String BANNER_PATTERN_TYPE_KEY = "type";

    private static final String SPAWNER_TYPE_KEY = "spawner-type";
    private static final String SPAWNER_DELAY_KEY = "spawner-delay";

    private static final String NOTE_OCTAVE_KEY = "octave";
    private static final String NOTE_TONE_KEY = "tone";
    private static final String NOTE_SHARPED_KEY = "sharped";

    private static final String JUKEBOX_DISC_KEY = "disc";

    private static final String SKULL_OWNER_KEY = "owner";
    private static final String SKULL_ROTATION_KEY = "rotation";

    private static final String COMMAND_NAME_KEY = "cmd-name";
    private static final String COMMAND_CMD_KEY = "command";

    private static final String FLOWER_TYPE_KEY = "flower-type";
    private static final String FLOWER_DATA_KEY = "flower-data";

    @SuppressWarnings("deprecation")
    public static Optional<ConfigurationSection> serializeState(BlockState state) {
        ConfigurationSection cs = new YamlConfiguration().createSection("thank mr skeltal");

        // http://minecraft.gamepedia.com/Block_entity was used as a reference for this method

        if (state instanceof InventoryHolder) {
            cs.set(INVENTORY_KEY, InventoryHelper.serializeInventory(((InventoryHolder) state).getInventory()));
        }

        if (state instanceof Sign) {
            cs.set(SIGN_LINES_KEY, Arrays.asList(((Sign) state).getLines()));
        } else if (state instanceof Banner) {
            cs.set(BANNER_BASE_COLOR_KEY, ((Banner) state).getBaseColor().name());
            ConfigurationSection patternSection = cs.createSection(BANNER_PATTERNS_KEY);
            List<Pattern> patterns = ((Banner) state).getPatterns();
            for (int i = 0; i < patterns.size(); i++) {
                ConfigurationSection subSection = patternSection.createSection("" + i);
                subSection.set(BANNER_PATTERN_COLOR_KEY, patterns.get(i).getColor().name());
                subSection.set(BANNER_PATTERN_TYPE_KEY, patterns.get(i).getPattern().name());
            }
        } else if (state instanceof CreatureSpawner) {
            cs.set(SPAWNER_TYPE_KEY, ((CreatureSpawner) state).getSpawnedType().name());
            cs.set(SPAWNER_DELAY_KEY, ((CreatureSpawner) state).getDelay());
        } else if (state instanceof NoteBlock) {
            cs.set(NOTE_OCTAVE_KEY, ((NoteBlock) state).getNote().getOctave());
            cs.set(NOTE_TONE_KEY, ((NoteBlock) state).getNote().getTone().name());
        } else if (state instanceof Jukebox) {
            if (((Jukebox) state).isPlaying()) {
                cs.set(JUKEBOX_DISC_KEY, ((Jukebox) state).getPlaying());
            }
        } else if (state instanceof Skull) {
            cs.set(SKULL_OWNER_KEY, ((Skull) state).getOwner());
            cs.set(SKULL_ROTATION_KEY, ((Skull) state).getRotation().name());
        } else if (state instanceof CommandBlock) {
            cs.set(COMMAND_NAME_KEY, ((CommandBlock) state).getName());
            cs.set(COMMAND_CMD_KEY, ((CommandBlock) state).getCommand());
        } else if (state instanceof FlowerPot) {
            cs.set(FLOWER_TYPE_KEY, ((FlowerPot) state).getContents().getItemType().name());
            cs.set(FLOWER_DATA_KEY, ((FlowerPot) state).getContents().getData());
        }

        if (cs.getKeys(false).size() > 0) {
            return Optional.of(cs);
        } else {
            return Optional.absent();
        }
    }

    @SuppressWarnings("deprecation")
    public static void deserializeState(Block block, ConfigurationSection serial) {
        BlockState state = block.getState();
        boolean missingData = false;
        boolean malformedData = false;

        if (state instanceof InventoryHolder) {
            if (serial.isConfigurationSection(INVENTORY_KEY)) {
                ((InventoryHolder) state).getInventory().setContents(
                        InventoryHelper.deserializeInventory(serial.getConfigurationSection(INVENTORY_KEY))
                );
            }
        }

        if (state instanceof Sign) {
            if (serial.isList(SIGN_LINES_KEY)) {
                List<String> lines = serial.getStringList(SIGN_LINES_KEY);
                for (int i = 0; i < lines.size(); i++) {
                    ((Sign) state).setLine(i, lines.get(i));
                }
            } else {
                missingData = true;
            }
        } else if (state instanceof Banner) {
            if (serial.isSet(BANNER_BASE_COLOR_KEY)) {
                DyeColor color = DyeColor.valueOf(serial.getString(BANNER_BASE_COLOR_KEY));
                if (color != null) {
                    ((Banner) state).setBaseColor(color);
                } else {
                    malformedData = true;
                }
            } else {
                missingData = true;
            }
            if (serial.isConfigurationSection(BANNER_PATTERNS_KEY)) {
                ConfigurationSection patterns = serial.getConfigurationSection(BANNER_PATTERNS_KEY);
                for (String key : patterns.getKeys(false)) {
                    ConfigurationSection subSection = patterns.getConfigurationSection(key);
                    DyeColor color = DyeColor.valueOf(subSection.getString(BANNER_PATTERN_COLOR_KEY));
                    PatternType type = PatternType.valueOf(subSection.getString(BANNER_PATTERN_TYPE_KEY));
                    if (color != null && type != null) {
                        ((Banner) state).addPattern(new Pattern(color, type));
                    } else {
                        malformedData = true;
                    }
                }
            } else {
                missingData = true;
            }
        } else if (state instanceof CreatureSpawner) {
            if (serial.isSet(SPAWNER_TYPE_KEY)) {
                EntityType type = EntityType.valueOf(serial.getString(SPAWNER_TYPE_KEY));
                if (type != null) {
                    ((CreatureSpawner) state).setSpawnedType(type);
                } else {
                    malformedData = true;
                }
            } else {
                missingData = true;
            }
        } else if (state instanceof NoteBlock) {
            if (serial.isInt(NOTE_OCTAVE_KEY) && serial.isSet(NOTE_TONE_KEY)) {
                Note.Tone tone = Note.Tone.valueOf(serial.getString(NOTE_TONE_KEY));
                if (tone != null) {
                    ((NoteBlock) state).setNote(
                            new Note(serial.getInt(NOTE_OCTAVE_KEY), tone, serial.getBoolean(NOTE_SHARPED_KEY))
                    );
                } else {
                    malformedData = true;
                }
            } else {
                missingData = true;
            }
        } else if (state instanceof Jukebox) {
            if (serial.isSet(JUKEBOX_DISC_KEY)) {
                Material disc = Material.valueOf(serial.getString(JUKEBOX_DISC_KEY));
                if (disc != null) {
                    ((Jukebox) state).setPlaying(disc);
                } else {
                    malformedData = true;
                }
            } else {
                missingData = true;
            }
        } else if (state instanceof Skull) {
            if (serial.isSet(SKULL_OWNER_KEY)) {
                ((Skull) state).setOwner(serial.getString(SKULL_OWNER_KEY));
            }
            if (serial.isSet(SKULL_ROTATION_KEY)) {
                BlockFace face = BlockFace.valueOf(serial.getString(SKULL_ROTATION_KEY));
                if (face != null) {
                    ((Skull) state).setRotation(face);
                } else {
                    malformedData = true;
                }
            } else {
                missingData = true;
            }
        } else if (state instanceof CommandBlock) {
            if (serial.isSet(COMMAND_CMD_KEY)) {
                        ((CommandBlock) state).setCommand(serial.getString(COMMAND_CMD_KEY));
            } else {
                missingData = true;
            }
            if (serial.isSet(COMMAND_NAME_KEY)) {
                ((CommandBlock) state).setName(serial.getString(COMMAND_NAME_KEY));
            } else {
                missingData = true;
            }
        } else if (state instanceof FlowerPot) {
            if (serial.isSet(FLOWER_TYPE_KEY)) {
                Material type = Material.valueOf(serial.getString(FLOWER_TYPE_KEY));
                if (type != null) {
                    byte data = serial.isSet(FLOWER_DATA_KEY) ? (byte) serial.getInt(FLOWER_DATA_KEY) : 0x0;
                    ((FlowerPot) state).setContents(new MaterialData(type, data));
                } else {
                    malformedData = true;
                }
            } else {
                missingData = true;
            }
        } else if (!(state instanceof InventoryHolder)) {
            SteelCore.logWarning("Failed to deserialize state data for rollback record for block at {"
                    + block.getX() + ", " + block.getY() + ", " + block.getZ() + "}");
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
