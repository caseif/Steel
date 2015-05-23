package net.caseif.steel;

import net.caseif.flint.Arena;
import net.caseif.flint.common.CommonArena;
import net.caseif.flint.common.CommonMinigame;
import net.caseif.flint.common.round.CommonRound;
import net.caseif.flint.round.Round;
import net.caseif.flint.util.physical.Location3D;

import net.caseif.steel.round.SteelRound;

/**
 * Implements {@link Arena}.
 *
 * @author Max Roncacé
 */
public class SteelArena extends CommonArena {

    public SteelArena(CommonMinigame parent, String id, String name, Location3D initialSpawn) {
        super(parent, id, name, initialSpawn);
    }

    @Override
    public Round createRound() throws UnsupportedOperationException {
        if (parent.getRoundMap().containsKey(this)) {
            throw new UnsupportedOperationException("Round already exists in arena \"" + getName() + "\"");
        }
        parent.getRoundMap().put(this, new SteelRound(this));
        return getRound().get(); // should never be absent
    }
}
