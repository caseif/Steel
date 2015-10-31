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
package net.caseif.flint.steel.util.helper.rollback.serialization;

import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.util.Support;
import net.caseif.flint.steel.util.helper.StorageHelper;

import com.google.gson.JsonObject;
import org.bukkit.Art;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
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

    public static JsonObject serializeState(Entity entity) {
        YamlConfiguration yaml = new YamlConfiguration();
        if (Support.ARMOR_STAND && entity instanceof ArmorStand) {
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

        return StorageHelper.yamlToJson(yaml);
    }

    public static void deserializeState(Entity entity, JsonObject serial) {
        YamlConfiguration yaml = StorageHelper.jsonToYaml(serial);
        if (Support.ARMOR_STAND && entity instanceof ArmorStand) {
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
            BlockFace facing = BlockFace.valueOf(yaml.getString(HANGING_FACING));
            if (facing != null) {
                ((Hanging) entity).setFacingDirection(facing, true);
            } else {
                SteelCore.logVerbose("Invalid serialized BlockFace value for hanging entity with UUID "
                        + entity.getUniqueId().toString());
            }

            if (entity instanceof ItemFrame) {
                ((ItemFrame) entity).setItem(yaml.getItemStack(ITEM_FRAME_ITEM));
                Rotation rotation = Rotation.valueOf(yaml.getString(ITEM_FRAME_ROTATION));
                if (rotation != null) {
                    ((ItemFrame) entity).setRotation(rotation);
                    // rotation doesn't sound like a word anymore
                } else {
                    SteelCore.logVerbose("Invalid serialized Rotation value for item frame with UUID "
                            + entity.getUniqueId().toString());
                }
            } else if (entity instanceof Painting) {
                Art art = Art.valueOf(yaml.getString("art"));
                if (art != null) {
                    ((Painting) entity).setArt(art, true);
                    // neither does art
                } else {
                    SteelCore.logVerbose("Invalid serialized Art value for item frame with UUID "
                            + entity.getUniqueId().toString());
                }
            }
        }
    }

}
