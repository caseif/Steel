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
package net.caseif.flint.steel.listener.rollback;

import net.caseif.flint.steel.util.helper.rollback.RollbackHelper;

import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
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

    private static final List<EntityType> SUPPORTED_TYPES = new ArrayList<EntityType>();

    static {
        SUPPORTED_TYPES.add(EntityType.ARMOR_STAND);
        SUPPORTED_TYPES.add(EntityType.ITEM_FRAME);
        SUPPORTED_TYPES.add(EntityType.LEASH_HITCH);
        SUPPORTED_TYPES.add(EntityType.PAINTING);
    }

    // BLOCK ROLLBACKS

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block b : event.blockList()) {
            RollbackHelper.checkBlockChange(b.getLocation(), b.getState(), event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    // covers enderman, falling blocks, and probably other stuff I'm forgetting
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        RollbackHelper.checkBlockChange(event.getBlock().getLocation(), event.getBlock().getState(), event);
    }

    // ENTITY ROLLBACKS

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (SUPPORTED_TYPES.contains(event.getEntity().getType())) {
            RollbackHelper.checkEntityChange(event.getEntity(), true, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (SUPPORTED_TYPES.contains(event.getEntity().getType())) {
            RollbackHelper.checkEntityChange(event.getEntity(), false, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (SUPPORTED_TYPES.contains(event.getRightClicked().getType())) {
            RollbackHelper.checkEntityChange(event.getRightClicked(), false, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (SUPPORTED_TYPES.contains(event.getEntity().getType())) {
            RollbackHelper.checkEntityChange(event.getEntity(), true, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (SUPPORTED_TYPES.contains(event.getEntity().getType())) {
            RollbackHelper.checkEntityChange(event.getEntity(), false, event);
        }
    }

}
