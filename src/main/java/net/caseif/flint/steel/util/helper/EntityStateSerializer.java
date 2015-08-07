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

import org.bukkit.Art;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.util.EulerAngle;

/**
 * Static utility class for serialization of entity state.
 *
 * @author Max Roncacé
 */
public class EntityStateSerializer {

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

    public static ConfigurationSection serializeState(Entity entity) {
        ConfigurationSection cs = new YamlConfiguration().createSection("thank 4 good bones and calsium");

        if (entity instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) entity;
            cs.set(ARMOR_STAND_HELMET, stand.getHelmet());
            cs.set(ARMOR_STAND_CHESTPLATE, stand.getChestplate());
            cs.set(ARMOR_STAND_LEGGINGS, stand.getLeggings());
            cs.set(ARMOR_STAND_BOOTS, stand.getBoots());
            cs.set(ARMOR_STAND_HAND, stand.getItemInHand());
            cs.set(ARMOR_STAND_POSE_HEAD, serializeEulerAngle(stand.getHeadPose()));
            cs.set(ARMOR_STAND_POSE_BODY, serializeEulerAngle(stand.getBodyPose()));
            cs.set(ARMOR_STAND_POSE_ARM_LEFT, serializeEulerAngle(stand.getLeftArmPose()));
            cs.set(ARMOR_STAND_POSE_ARM_RIGHT, serializeEulerAngle(stand.getRightArmPose()));
            cs.set(ARMOR_STAND_POSE_LEG_LEFT, serializeEulerAngle(stand.getLeftLegPose()));
            cs.set(ARMOR_STAND_POSE_LEG_RIGHT, serializeEulerAngle(stand.getRightLegPose()));
            cs.set(ARMOR_STAND_ARMS, stand.hasArms());
            cs.set(ARMOR_STAND_BASE_PLATE, stand.hasBasePlate());
            cs.set(ARMOR_STAND_GRAVITY, stand.hasGravity());
            cs.set(ARMOR_STAND_SMALL, stand.isSmall());
            cs.set(ARMOR_STAND_VISIBLE, stand.isVisible());
        } else if (entity instanceof Hanging) {
            cs.set(HANGING_FACING, ((Hanging) entity).getFacing().name());
            if (entity instanceof ItemFrame) {
                cs.set(ITEM_FRAME_ITEM, ((ItemFrame) entity).getItem());
                cs.set(ITEM_FRAME_ROTATION, ((ItemFrame) entity).getRotation().name());
            } else if (entity instanceof Painting) {
                cs.set(PAINTING_ART, ((Painting) entity).getArt().name());
            }
        }

        return cs;
    }

    public static void deserializeState(Entity entity, ConfigurationSection serial) {
        if (entity instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) entity;
            stand.setHelmet(serial.getItemStack(ARMOR_STAND_HELMET));
            stand.setChestplate(serial.getItemStack(ARMOR_STAND_CHESTPLATE));
            stand.setLeggings(serial.getItemStack(ARMOR_STAND_LEGGINGS));
            stand.setBoots(serial.getItemStack(ARMOR_STAND_BOOTS));
            stand.setItemInHand(serial.getItemStack(ARMOR_STAND_HAND));
            stand.setHeadPose(deserializeEulerAngle(serial.getString(ARMOR_STAND_POSE_HEAD)));
            stand.setBodyPose(deserializeEulerAngle(serial.getString(ARMOR_STAND_POSE_BODY)));
            stand.setLeftArmPose(deserializeEulerAngle(serial.getString(ARMOR_STAND_POSE_ARM_LEFT)));
            stand.setRightArmPose(deserializeEulerAngle(serial.getString(ARMOR_STAND_POSE_ARM_RIGHT)));
            stand.setLeftLegPose(deserializeEulerAngle(serial.getString(ARMOR_STAND_POSE_LEG_LEFT)));
            stand.setRightLegPose(deserializeEulerAngle(serial.getString(ARMOR_STAND_POSE_LEG_RIGHT)));
            stand.setArms(serial.getBoolean(ARMOR_STAND_ARMS));
            stand.setBasePlate(serial.getBoolean(ARMOR_STAND_BASE_PLATE));
            stand.setGravity(serial.getBoolean(ARMOR_STAND_GRAVITY));
            stand.setSmall(serial.getBoolean(ARMOR_STAND_SMALL));
            stand.setVisible(serial.getBoolean(ARMOR_STAND_VISIBLE));
        } else if (entity instanceof Hanging) {
            BlockFace facing = BlockFace.valueOf(serial.getString(HANGING_FACING));
            if (facing != null) {
                ((Hanging) entity).setFacingDirection(facing);
            } else {
                SteelCore.logVerbose("Invalid serialized BlockFace value for hanging entity with UUID "
                        + entity.getUniqueId().toString());
            }
            if (entity instanceof ItemFrame) {
                ((ItemFrame) entity).setItem(serial.getItemStack(ITEM_FRAME_ITEM));
                Rotation rotation = Rotation.valueOf(serial.getString(ITEM_FRAME_ROTATION));
                // rotation doesn't sound like a word anymore
                if (rotation != null) {
                    ((ItemFrame) entity).setRotation(rotation);
                } else {
                    SteelCore.logVerbose("Invalid serialized Rotation value for item frame with UUID "
                            + entity.getUniqueId().toString());
                }
            } else if (entity instanceof Painting) {
                Art art = Art.valueOf(serial.getString("art"));
                if (art != null) {
                    ((Painting) entity).setArt(art);
                } else {
                    SteelCore.logVerbose("Invalid serialized Art value for item frame with UUID "
                            + entity.getUniqueId().toString());
                }
            }
        }
    }

    private static String serializeEulerAngle(EulerAngle angle) {
        return "{" + angle.getX() + "," + angle.getY() + "," + angle.getZ() + "}";
    }

    private static EulerAngle deserializeEulerAngle(String serial) throws IllegalArgumentException {
        if (serial.startsWith("{") && serial.endsWith("}")) {
            String[] arr = serial.substring(1, serial.length() - 1).split(",");
            if (arr.length == 3) {
                try {
                    double x = Double.parseDouble(arr[0]);
                    double y = Double.parseDouble(arr[0]);
                    double z = Double.parseDouble(arr[0]);
                    return new EulerAngle(x, y, z);
                } catch (NumberFormatException ignored) { } // continue to the IllegalArgumentException at the bottom
            }
        }
        throw new IllegalArgumentException("Invalid serial for EulerAngle");
    }

}
