package net.caseif.flint.steel.listener.block;

import net.caseif.flint.Minigame;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.arena.SteelArena;
import net.caseif.flint.steel.util.MiscUtil;

import com.google.common.base.Optional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Listener for events logged by the rollback engine.
 */
public class RollbackListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        for (Minigame mg : SteelCore.getMinigames().values()) {
            Optional<Challenger> challenger = mg.getChallenger(event.getPlayer().getUniqueId());
            if (challenger.isPresent()) {
                if (challenger.get().getRound().getArena().getWorld().equals(event.getBlock().getWorld().getName())) {
                    if (!challenger.get().getRound().getArena().getBoundary().isPresent()
                            || challenger.get().getRound().getArena().getBoundary().get().contains(
                            MiscUtil.convertLocation(event.getBlock().getLocation()))) {
                        try {
                            ((SteelArena)challenger.get().getRound().getArena()).logBlockChange(
                                    event.getBlock().getLocation(),
                                    event.getBlock().getState()
                            );
                        } catch (IOException | SQLException ex) {
                            SteelCore.logSevere("Failed to log block break in arena "
                                    + challenger.get().getRound().getArena().getName());
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}
