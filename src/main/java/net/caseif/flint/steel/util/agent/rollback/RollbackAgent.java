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

package net.caseif.flint.steel.util.agent.rollback;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.util.agent.rollback.CommonRollbackAgent;
import net.caseif.flint.common.util.agent.rollback.RollbackRecord;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.SteelMain;
import net.caseif.flint.steel.arena.SteelArena;
import net.caseif.flint.steel.util.agent.rollback.serialization.BlockStateSerializer;
import net.caseif.flint.steel.util.agent.rollback.serialization.EntityStateSerializer;
import net.caseif.flint.steel.util.helper.LocationHelper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.inventory.InventoryHolder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Static utility class for rollback-related functionality.
 *
 * @author Max Roncacé
 */
public final class RollbackAgent extends CommonRollbackAgent {

    /**
     * Creates a new {@link RollbackAgent} backing the given
     * {@link SteelArena}.
     *
     * @param arena The {@link SteelArena} to be backed by the new
     *     {@link RollbackAgent}
     */
    public RollbackAgent(CommonArena arena) {
        super(arena);
    }

    @Override
    protected void delay(Runnable runnable) {
        Bukkit.getScheduler().runTask(SteelMain.getInstance(), runnable);
    }

    /**
     * Logs a rollback change at the given location.
     *
     * @param block The block which was changed
     * @throws IOException If an exception occurs while reading to or from the
     *     rollback database
     * @throws SQLException If an exception occurs while manipulating the
     *     rollback database
     */
    @SuppressWarnings("deprecation")
    public void logBlockChange(Block block) throws IOException, SQLException {
        String state = BlockStateSerializer.serializeState(block).orNull();
        logChange(RollbackRecord.createBlockRecord(-1, LocationHelper.convertLocation(block.getLocation()),
                block.getState().getType().name(), block.getState().getRawData(), state));
    }

    private void logEntityCreation(Entity entity) throws IOException, SQLException {
        logEntitySomething(entity, true);
    }

    private void logEntityChange(Entity entity) throws IOException, SQLException {
        logEntitySomething(entity, false);
    }

    private void logEntitySomething(Entity entity, boolean newlyCreated) throws IOException, SQLException {
        String state = !newlyCreated ? EntityStateSerializer.serializeState(entity) : null;
        if (newlyCreated) {
            logChange(RollbackRecord.createEntityCreationRecord(-1, entity.getUniqueId(), entity.getWorld().getName()));
        } else {
            logChange(RollbackRecord.createEntityChangeRecord(-1, entity.getUniqueId(),
                    LocationHelper.convertLocation(entity.getLocation()), entity.getType().name(), state));
        }
    }

    public static void checkBlockChange(Block block, Event event) {
        List<Arena> arenas = checkChangeAtLocation(LocationHelper.convertLocation(block.getLocation()));
        for (Arena arena : arenas) {
            try {
                ((SteelArena) arena).getRollbackAgent().logBlockChange(block);
            } catch (IOException | SQLException ex) {
                throw new RuntimeException("Failed to log " + event.getEventName() + " for rollback in arena "
                        + arena.getDisplayName(), ex);
            }
        }
    }

    public static void checkEntityChange(Entity entity, boolean newlyCreated, Event event) {
        List<Arena> arenas = checkChangeAtLocation(LocationHelper.convertLocation(entity.getLocation()));
        for (Arena arena : arenas) {
            try {
                if (newlyCreated) {
                    ((SteelArena) arena).getRollbackAgent().logEntityCreation(entity);
                } else {
                    ((SteelArena) arena).getRollbackAgent().logEntityChange(entity);
                }
            } catch (IOException | SQLException ex) {
                throw new RuntimeException("Failed to log " + event.getEventName() + " for rollback in arena "
                        + arena.getDisplayName(), ex);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void rollbackBlock(RollbackRecord record)
            throws IOException {
        Block b = LocationHelper.convertLocation(record.getLocation()).getBlock();
        Material m;
        try {
            m = Material.valueOf(record.getTypeData());
        } catch (IllegalArgumentException ex) {
            SteelCore.logWarning("Rollback record with ID " + record.getId() + " in arena "
                    + getArena().getId() + " cannot be matched to a Material");
            return;
        }
        if (b.getState() instanceof InventoryHolder) {
            // Bukkit drops the items if they aren't cleared
            ((InventoryHolder) b.getState()).getInventory().clear();
        }
        b.setType(m);

        if (SteelCore.isLegacy()) {
            SteelCore.getLegacyHelper().updateData(b, (byte) record.getData());
        }

        if (record.getStateSerial() != null) {
            try {
                BlockStateSerializer.deserializeState(b, record.getStateSerial());
            } catch (InvalidConfigurationException ex) {
                throw new IOException(ex);
            }
        }
    }

    private HashMap<UUID, Entity> entities;

    @Override
    public void rollbackEntityCreation(RollbackRecord record) {
        if (entities.containsKey(record.getUuid())) {
            entities.get(record.getUuid()).remove();
        } // else: probably already removed by a player or something else
    }

    @Override
    public void rollbackEntityChange(final RollbackRecord record) throws IOException {
        final EntityType entityType;
        try {
            entityType = EntityType.valueOf(record.getTypeData());
        } catch (IllegalArgumentException ex) {
            CommonCore.logWarning("Invalid entity type for rollback record with ID " + record.getId()
                    + " in arena " + getArena().getId());
            return;
        }

        if (entities.containsKey(record.getUuid())) {
            Entity e = entities.get(record.getUuid());
            // teleport to bottom of map so it doesn't conflict since it isn't removed
            // until the next tick
            e.teleport(e.getLocation().subtract(0, e.getLocation().getY() + 1, 0));
            e.remove(); // clean slate
        }

        Bukkit.getScheduler().runTask(SteelMain.getInstance(), new Runnable() {
            @Override
            public void run() {
                Location loc = LocationHelper.convertLocation(record.getLocation());
                Entity e = loc.getWorld().spawnEntity(loc, entityType);
                if (record.getStateSerial() != null) {
                    try {
                        EntityStateSerializer.deserializeState(e, record.getStateSerial());
                    } catch (InvalidConfigurationException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
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
