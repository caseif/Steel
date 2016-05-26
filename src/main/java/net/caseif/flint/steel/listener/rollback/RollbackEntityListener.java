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
package net.caseif.flint.steel.listener.rollback;

import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.util.helper.rollback.RollbackAgent;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener for entity events logged by the rollback engine.
 *
 * @author Max Roncacé
 */
public class RollbackEntityListener implements Listener {

    private static final List<EntityType> SUPPORTED_TYPES = new ArrayList<>();

    static {
        SUPPORTED_TYPES.add(EntityType.ITEM_FRAME);
        SUPPORTED_TYPES.add(EntityType.LEASH_HITCH);
        SUPPORTED_TYPES.add(EntityType.PAINTING);
        try {
            SUPPORTED_TYPES.add(EntityType.ARMOR_STAND);
        } catch (NoSuchFieldError ex) {
            SteelCore.logVerbose("Server does not support 1.8 entities - not registering");
        }
    }

    // BLOCK ROLLBACKS

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block b : event.blockList()) {
            RollbackAgent.checkBlockChange(b.getLocation(), b.getState(), event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    // covers enderman, falling blocks, and probably other stuff I'm forgetting
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        RollbackAgent.checkBlockChange(event.getBlock().getLocation(), event.getBlock().getState(), event);
    }

    // ENTITY ROLLBACKS

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        handleEntityEvent(event.getEntity(), true, event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        handleEntityEvent(event.getEntity(), false, event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        handleEntityEvent(event.getRightClicked(), false, event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
            handleEntityEvent(event.getEntity(), true, event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
            handleEntityEvent(event.getEntity(), false, event);
    }

    public static void handleEntityEvent(Entity entity, boolean newlyCreated, Event event) {
        if (SUPPORTED_TYPES.contains(entity.getType())) {
            RollbackAgent.checkEntityChange(entity, newlyCreated, event);
        }
    }

}
