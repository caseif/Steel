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

import org.bukkit.Art;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;

/**
 * Static utility class for serialization of entity state.
 *
 * @author Max Roncacé
 */
public class EntityStateSerializer {

    private static final String PITCH = "pitch";
    private static final String YAW = "yaw";

    private static final String ARMOR_STAND_HELMET = "stand.helmet";
    private static final String ARMOR_STAND_CHESTPLATE = "stand.chestplate";
    private static final String ARMOR_STAND_LEGGINGS = "stand.leggings";
    private static final String ARMOR_STAND_BOOTS = "stand.boots";
    private static final String ARMOR_STAND_HAND = "stand.hand";
    private static final String ARMOR_STAND_POSE_HEAD = "stand.pose.head";
    private static final String ARMOR_STAND_POSE_BODY = "stand.pose.body";
    private static final String ARMOR_STAND_POSE_ARM_LEFT = "stand.pose.arm_left";
    private static final String ARMOR_STAND_POSE_ARM_RIGHT = "stand.pose.arm.right";
    private static final String ARMOR_STAND_POSE_LEG_LEFT = "stand.pose.leg.left";
    private static final String ARMOR_STAND_POSE_LEG_RIGHT = "stand.pose.leg.right";
    private static final String ARMOR_STAND_ARMS = "stand.arms";
    private static final String ARMOR_STAND_BASE_PLATE = "stand.base_plate";
    private static final String ARMOR_STAND_GRAVITY = "stand.gravity";
    private static final String ARMOR_STAND_SMALL = "stand.small";
    private static final String ARMOR_STAND_VISIBLE = "stand.visible";

    private static final String HANGING_FACING = "facing";

    private static final String ITEM_FRAME_ITEM = "item";
    private static final String ITEM_FRAME_ROTATION = "rotation";

    private static final String PAINTING_ART = "art";

    public static String serializeState(Entity entity) {
        YamlConfiguration yaml = new YamlConfiguration();
        if (entity instanceof ArmorStand) {
            EulerAngleSerializer eas = EulerAngleSerializer.getInstance();
            ArmorStand stand = (ArmorStand) entity;
            yaml.set(PITCH, stand.getLocation().getPitch());
            yaml.set(YAW, stand.getLocation().getYaw());
            yaml.set(ARMOR_STAND_HELMET, stand.getHelmet());
            yaml.set(ARMOR_STAND_CHESTPLATE, stand.getChestplate());
            yaml.set(ARMOR_STAND_LEGGINGS, stand.getLeggings());
            yaml.set(ARMOR_STAND_BOOTS, stand.getBoots());
            yaml.set(ARMOR_STAND_HAND, stand.getItemInHand());
            yaml.set(ARMOR_STAND_POSE_HEAD, eas.serialize(stand.getHeadPose()));
            yaml.set(ARMOR_STAND_POSE_BODY, eas.serialize(stand.getBodyPose()));
            yaml.set(ARMOR_STAND_POSE_ARM_LEFT, eas.serialize(stand.getLeftArmPose()));
            yaml.set(ARMOR_STAND_POSE_ARM_RIGHT, eas.serialize(stand.getRightArmPose()));
            yaml.set(ARMOR_STAND_POSE_LEG_LEFT, eas.serialize(stand.getLeftLegPose()));
            yaml.set(ARMOR_STAND_POSE_LEG_RIGHT, eas.serialize(stand.getRightLegPose()));
            yaml.set(ARMOR_STAND_ARMS, stand.hasArms());
            yaml.set(ARMOR_STAND_BASE_PLATE, stand.hasBasePlate());
            yaml.set(ARMOR_STAND_GRAVITY, stand.hasGravity());
            yaml.set(ARMOR_STAND_SMALL, stand.isSmall());
            yaml.set(ARMOR_STAND_VISIBLE, stand.isVisible());
        } else if (entity instanceof Hanging) {
            yaml.set(HANGING_FACING, ((Hanging) entity).getFacing().name());
            if (entity instanceof ItemFrame) {
                yaml.set(ITEM_FRAME_ITEM, ((ItemFrame) entity).getItem());
                yaml.set(ITEM_FRAME_ROTATION, ((ItemFrame) entity).getRotation().name());
            } else if (entity instanceof Painting) {
                yaml.set(PAINTING_ART, ((Painting) entity).getArt().name());
            }
        }

        return yaml.saveToString();
    }

    public static void deserializeState(Entity entity, String serial) throws InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(serial);

        if (entity instanceof ArmorStand) {
            EulerAngleSerializer eas = EulerAngleSerializer.getInstance();
            ArmorStand stand = (ArmorStand) entity;
            stand.setHelmet(yaml.getItemStack(ARMOR_STAND_HELMET));
            stand.setChestplate(yaml.getItemStack(ARMOR_STAND_CHESTPLATE));
            stand.setLeggings(yaml.getItemStack(ARMOR_STAND_LEGGINGS));
            stand.setBoots(yaml.getItemStack(ARMOR_STAND_BOOTS));
            stand.setItemInHand(yaml.getItemStack(ARMOR_STAND_HAND));
            stand.setHeadPose(eas.deserialize(yaml.getString(ARMOR_STAND_POSE_HEAD)));
            stand.setBodyPose(eas.deserialize(yaml.getString(ARMOR_STAND_POSE_BODY)));
            stand.setLeftArmPose(eas.deserialize(yaml.getString(ARMOR_STAND_POSE_ARM_LEFT)));
            stand.setRightArmPose(eas.deserialize(yaml.getString(ARMOR_STAND_POSE_ARM_RIGHT)));
            stand.setLeftLegPose(eas.deserialize(yaml.getString(ARMOR_STAND_POSE_LEG_LEFT)));
            stand.setRightLegPose(eas.deserialize(yaml.getString(ARMOR_STAND_POSE_LEG_RIGHT)));
            stand.setArms(yaml.getBoolean(ARMOR_STAND_ARMS));
            stand.setBasePlate(yaml.getBoolean(ARMOR_STAND_BASE_PLATE));
            stand.setGravity(yaml.getBoolean(ARMOR_STAND_GRAVITY));
            stand.setSmall(yaml.getBoolean(ARMOR_STAND_SMALL));
            stand.setVisible(yaml.getBoolean(ARMOR_STAND_VISIBLE));
        } else if (entity instanceof Hanging) {
            try {
                BlockFace facing = BlockFace.valueOf(yaml.getString(HANGING_FACING));
                ((Hanging) entity).setFacingDirection(facing, true);
            } catch (IllegalArgumentException ex) {
                SteelCore.logVerbose("Invalid serialized BlockFace value for hanging entity with UUID "
                        + entity.getUniqueId().toString());
            }

            if (entity instanceof ItemFrame) {
                ((ItemFrame) entity).setItem(yaml.getItemStack(ITEM_FRAME_ITEM));
                try {
                    Rotation rotation = Rotation.valueOf(yaml.getString(ITEM_FRAME_ROTATION));
                    ((ItemFrame) entity).setRotation(rotation);
                    // rotation doesn't sound like a word anymore
                } catch (IllegalArgumentException ex) {
                    SteelCore.logVerbose("Invalid serialized Rotation value for item frame with UUID "
                            + entity.getUniqueId().toString());
                }
            } else if (entity instanceof Painting) {
                try {
                    Art art = Art.valueOf(yaml.getString("art"));
                    ((Painting) entity).setArt(art, true);
                    // neither does art
                } catch (IllegalArgumentException ex) {
                    SteelCore.logVerbose("Invalid serialized Art value for item frame with UUID "
                            + entity.getUniqueId().toString());
                }
            }
        }
    }

}
