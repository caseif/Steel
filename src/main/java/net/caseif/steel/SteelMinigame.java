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
package net.caseif.steel;

import net.caseif.flint.Arena;
import net.caseif.flint.Minigame;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.locale.LocaleManager;
import net.caseif.flint.round.Round;
import net.caseif.flint.challenger.Challenger;
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
 * Implementation of {@link Minigame}.
 *
 * @author Max Roncacé
 */
public class SteelMinigame implements Minigame {

    private Plugin plugin;

    private Map<ConfigNode<?>, Object> configValues = new HashMap<>();
    private BiMap<String, Arena> arenas = HashBiMap.create();
    BiMap<Arena, Round> rounds = HashBiMap.create(); // guarantees values aren't duplicated

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
        throw new UnsupportedOperationException(); //TODO
    }
}
