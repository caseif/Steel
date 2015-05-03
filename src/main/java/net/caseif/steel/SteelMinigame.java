package net.caseif.steel;

import net.caseif.flint.Arena;
import net.caseif.flint.Minigame;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.locale.LocaleManager;
import net.caseif.flint.round.Round;
import net.caseif.flint.round.challenger.Challenger;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implementaion of {@link Minigame}.
 *
 * @author Max Roncacé
 */
public class SteelMinigame implements Minigame {

    private Plugin plugin;

    private Map<ConfigNode<?>, Object> configValues = new HashMap<>();
    private BiMap<String, Arena> arenas = HashBiMap.create();
    private BiMap<Arena, Round> rounds = HashBiMap.create(); // guarantees values aren't duplicated

    public SteelMinigame(String plugin) {
        if (Bukkit.getPluginManager().isPluginEnabled(plugin)) {
            this.plugin = Bukkit.getPluginManager().getPlugin(plugin);
        } else {
            throw new IllegalArgumentException("Plugin \"" + plugin + "\" is not loaded!");
        }
    }

    @Override
    public String getPlugin() {
        return plugin.getName();
    }

    @Override
    @SuppressWarnings("unchecked") // only mutable through setConfigValue(), which guarantees types match
    public <T> T getConfigValue(ConfigNode<T> node) {
        return (T)configValues.getOrDefault(node, node.getDefaultValue());
    }

    @Override
    public <T> void setConfigValue(ConfigNode<T> node, T value) {
        configValues.put(node, value);
    }

    @Override
    public Set<Arena> getArenas() {
        return arenas.values();
    }

    @Override
    public Optional<Arena> getArena(String arenaName) {
        return Optional.fromNullable(arenas.get(arenaName));
    }

    @Override
    public Arena createArena(String id, Location3D spawnPoint) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Round> getRounds() {
        return rounds.values();
    }

    @Override
    public Set<Challenger> getChallengers() {
        Set<Challenger> challengers = new HashSet<>();
        for (Round r : getRounds()) { // >tfw no streams
            challengers.addAll(r.getChallengers());
        }
        return challengers;
    }

    @Override
    public Optional<Challenger> getChallenger(UUID uuid) {
        for (Round r : getRounds()) {
            if (r.getChallenger(uuid).isPresent()) {
                return r.getChallenger(uuid);
            }
        }
        return Optional.absent();
    }

    @Override
    public LocaleManager getLocaleManager() {
        throw new UnsupportedOperationException();
    }
}
