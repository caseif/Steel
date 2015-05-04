package net.caseif.steel;

import net.caseif.flint.Arena;
import net.caseif.flint.round.Round;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;
import net.caseif.steel.util.SteelMetadatable;

import com.google.common.base.Optional;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;

import java.util.Map;

/**
 * Implementation of {@link Arena}.
 *
 * @author Max Roncacé
 */
//TODO: no persistance yet, need to work on that
public class SteelArena extends SteelMetadatable implements Arena {

    private SteelMinigame parent;
    private String id;
    private String name;
    private String world;
    private HashBiMap<Integer, Location3D> spawns = HashBiMap.create();
    private Boundary boundary = null;

    public SteelArena(SteelMinigame parent, String id, String name, Location3D initialSpawn) {
        this.parent = parent;
        this.id = id;
        this.name = name;
        this.world = initialSpawn.getWorld().get();
        this.spawns.put(0, initialSpawn);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public Optional<Boundary> getBoundary() {
        return Optional.fromNullable(boundary);
    }

    @Override
    public void setBoundary(Boundary bound) {
        this.boundary = bound;
    }

    @Override
    public ImmutableBiMap<Integer, Location3D> getSpawnPoints() {
        return ImmutableBiMap.copyOf(spawns);
    }

    @Override
    public int addSpawnPoint(Location3D spawn) {
        int id;
        for (id = 0; id <= spawns.size(); id++) {
            if (!spawns.containsKey(id)) {
                spawns.put(id, spawn);
                return id;
            }
        }
        return -1;
    }

    @Override
    public void removeSpawnPoint(int index) {
        spawns.remove(index);
    }

    @Override
    public void removeSpawnPoint(Location3D location) {
        for (Map.Entry<Integer, Location3D> e : spawns.entrySet()) {
            if (e.getValue().equals(location)) {
                spawns.remove(e.getKey());
                return;
            }
        }
    }

    @Override
    public Optional<Round> getRound() {
        return Optional.fromNullable(parent.rounds.get(this));
    }

    @Override
    public Round createRound() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
