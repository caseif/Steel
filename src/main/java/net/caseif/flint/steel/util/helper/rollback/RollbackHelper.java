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
package net.caseif.flint.steel.util.helper.rollback;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.util.helper.rollback.CommonRollbackHelper;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.arena.SteelArena;
import net.caseif.flint.steel.util.file.DataFiles;
import net.caseif.flint.steel.util.helper.LocationHelper;
import net.caseif.flint.steel.util.helper.rollback.serialization.BlockStateSerializer;
import net.caseif.flint.steel.util.helper.rollback.serialization.EntityStateSerializer;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.inventory.InventoryHolder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

/**
 * Static utility class for rollback-related functionality.
 *
 * @author Max Roncacé
 */
public final class RollbackHelper extends CommonRollbackHelper {

    /**
     * Creates a new {@link RollbackHelper} backing the given
     * {@link SteelArena}.
     *
     * @param arena The {@link SteelArena} to be backed by the new
     *     {@link RollbackHelper}
     */
    public RollbackHelper(SteelArena arena) {
        super(arena, DataFiles.ROLLBACK_STORE.getFile(arena.getMinigame()),
                DataFiles.ROLLBACK_STATE_STORE.getFile(arena.getMinigame()));
    }
    /**
     * Logs a rollback change at the given location.
     *
     * @param location The location of the change
     * @param originalState The state of the rollback before the change
     * @throws IOException If an exception occurs while reading to or from the
     *     rollback database
     * @throws SQLException If an exception occurs while manipulating the
     *     rollback database
     */
    @SuppressWarnings("deprecation")
    public void logBlockChange(Location location, BlockState originalState) throws IOException, SQLException {
        JsonObject state = BlockStateSerializer.serializeState(originalState).orNull();
        logChange(RECORD_TYPE_BLOCK_CHANGED, LocationHelper.convertLocation(location), null,
                originalState.getType().name(), originalState.getRawData(), state);
    }

    private void logEntityCreation(Entity entity) throws IOException, SQLException {
        logEntitySomething(entity, true);
    }

    private void logEntityChange(Entity entity) throws IOException, SQLException {
        logEntitySomething(entity, false);
    }

    private void logEntitySomething(Entity entity, boolean newlyCreated) throws IOException, SQLException {
        JsonObject state = !newlyCreated ? EntityStateSerializer.serializeState(entity) : null;
        logChange(newlyCreated ? RECORD_TYPE_ENTITY_CREATED : RECORD_TYPE_ENTITY_CHANGED,
                LocationHelper.convertLocation(entity.getLocation()), entity.getUniqueId(), entity.getType().name(), -1,
                state);
    }

    public static void checkBlockChange(Location location, BlockState state, Event event) {
        Optional<Arena> arena = checkChangeAtLocation(LocationHelper.convertLocation(location));
        if (arena.isPresent() && arena.get().getRound().isPresent()) {
            try {
                ((SteelArena) arena.get()).getRollbackHelper().logBlockChange(location, state);
            } catch (IOException | SQLException ex) {
                throw new RuntimeException("Failed to log " + event.getEventName() + " for rollback in arena "
                        + arena.get().getName(), ex);
            }
        }
    }

    public static void checkEntityChange(Entity entity, boolean newlyCreated, Event event) {
        Optional<Arena> arena = checkChangeAtLocation(LocationHelper.convertLocation(entity.getLocation()));
        if (arena.isPresent() && arena.get().getRound().isPresent()) {
            try {
                if (newlyCreated) {
                    ((SteelArena) arena.get()).getRollbackHelper().logEntityCreation(entity);
                } else {
                    ((SteelArena) arena.get()).getRollbackHelper().logEntityChange(entity);
                }
            } catch (IOException | SQLException ex) {
                throw new RuntimeException("Failed to log " + event.getEventName() + " for rollback in arena "
                        + arena.get().getName(), ex);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void rollbackBlock(int id, Location3D location, String type, int data, JsonObject stateSerial) {
        Block b = LocationHelper.convertLocation(location).getBlock();
        Material m = Material.valueOf(type);
        if (m != null) {
            if (b.getState() instanceof InventoryHolder) {
                // Bukkit drops the items if they aren't cleared
                ((InventoryHolder) b.getState()).getInventory().clear();
            }
            b.setType(m);
            b.setData((byte) data);
            if (stateSerial != null) {
                BlockStateSerializer.deserializeState(b, stateSerial);
            }
        } else {
            SteelCore.logWarning("Rollback record with ID " + id + " in arena "
                    + getArena().getId() + " cannot be matched to a Material");
        }
    }

    private HashMap<UUID, Entity> entities;

    @Override
    public void rollbackEntityCreation(int id, UUID uuid) {
        if (entities.containsKey(uuid)) {
            entities.get(uuid).remove();
        } // else: probably already removed by a player or something else
    }

    @Override
    public void rollbackEntityChange(int id, UUID uuid, Location3D location, String type, JsonObject stateSerial) {
        EntityType entityType = EntityType.valueOf(type);
        if (entityType != null) {
            if (entities.containsKey(uuid)) {
                Entity e = entities.get(uuid);
                // teleport to bottom of map so it doesn't conflict since it isn't removed
                // until the next tick
                e.teleport(e.getLocation().subtract(0, e.getLocation().getY() + 1, 0));
                e.remove(); // clean slate
            }
            Location loc = LocationHelper.convertLocation(location);
            Entity e = loc.getWorld().spawnEntity(loc, entityType);
            if (stateSerial != null) {
                EntityStateSerializer.deserializeState(e, stateSerial);
            }
        } else {
            CommonCore.logWarning("Invalid entity type for rollback record with ID " + id
                    + " in arena " + getArena().getId());
        }
    }

    @Override
    public void cacheEntities() {
        World w = Bukkit.getWorld(getArena().getWorld());
        // hash entities by UUID before iterating records for faster lookup
        entities = new HashMap<>();
        for (Entity entity : w.getEntities()) {
            entities.put(entity.getUniqueId(), entity);
        }
    }
}
