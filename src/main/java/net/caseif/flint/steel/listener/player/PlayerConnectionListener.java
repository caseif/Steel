package net.caseif.flint.steel.listener.player;

import net.caseif.flint.Minigame;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.round.SteelRound;
import net.caseif.flint.steel.util.PlayerUtil;
import net.caseif.flint.steel.util.io.DataFiles;

import com.google.common.base.Optional;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Listener for events relating to players' connections.
 *
 * @author Max Roncacé
 */
public class PlayerConnectionListener implements Listener {

    /**
     * Config key for the list of offline players that need to be reset on join.
     */
    private static final String OFFLINE_PLAYER_LIST_KEY = "offline";

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        for (Minigame mg : CommonCore.getMinigames().values()) {
            Optional<Challenger> ch = mg.getChallenger(uuid);
            if (ch.isPresent()) {
                // store the player to disk so their inventory and location can be popped later
                ((SteelRound)ch.get().getRound()).removeChallenger(ch.get(), true);

                try {
                    File offlinePlayers = DataFiles.OFFLINE_PLAYER_STORE.getFile();
                    YamlConfiguration yaml = new YamlConfiguration();
                    yaml.load(offlinePlayers);

                    List<String> players = yaml.getStringList(OFFLINE_PLAYER_LIST_KEY);
                    if (players == null) {
                        players = new ArrayList<>();
                    }
                    players.add(uuid.toString());
                    yaml.set(OFFLINE_PLAYER_LIST_KEY, players);
                    yaml.save(offlinePlayers);
                } catch (InvalidConfigurationException | IOException ex) {
                    ex.printStackTrace();
                    SteelCore.logSevere("Failed to store data for disconnecting challenger "
                            + event.getPlayer().getName());
                }

            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        try {
            File offlinePlayers = DataFiles.OFFLINE_PLAYER_STORE.getFile();
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.load(offlinePlayers);

            if (yaml.isSet(OFFLINE_PLAYER_LIST_KEY)) {
                List<String> players = yaml.getStringList(OFFLINE_PLAYER_LIST_KEY);
                // check whether the player left while in a round
                if (players.contains(uuid.toString())) {

                    // these two try-blocks are separate so they can both run even if one fails
                    try {
                        PlayerUtil.popInventory(event.getPlayer());
                    } catch (InvalidConfigurationException | IOException ex) {
                        ex.printStackTrace();
                        SteelCore.logSevere("Failed to pop inventory for player " + event.getPlayer().getName());
                    }

                    try {
                        PlayerUtil.popLocation(event.getPlayer());
                    } catch (InvalidConfigurationException | IOException ex) {
                        ex.printStackTrace();
                        SteelCore.logSevere("Failed to pop inventory for player " + event.getPlayer().getName());
                    }

                    players.remove(uuid.toString());
                    yaml.set(OFFLINE_PLAYER_LIST_KEY, players);
                    yaml.save(offlinePlayers);
                }
            }
        } catch (InvalidConfigurationException | IOException ex) {
            ex.printStackTrace();
            SteelCore.logSevere("Failed to load offline player data");
        }
    }

}
