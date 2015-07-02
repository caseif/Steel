package net.caseif.flint.steel.listener;

import net.caseif.flint.Minigame;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.round.SteelRound;

import com.google.common.base.Optional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Listener for {@link Player}-related events.
 *
 * @author Max Roncacé
 */
public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        for (Minigame mg : ((CommonCore)CommonCore.getInstance()).getMinigames().values()) {
            Optional<Challenger> ch = mg.getChallenger(uuid);
            if (ch.isPresent()) {
                ((SteelRound)ch.get().getRound()).removeChallenger(ch.get(), true);
            }
        }
    }

}
