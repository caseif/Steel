package net.caseif.steel.event.round;

import net.caseif.flint.Minigame;
import net.caseif.flint.event.round.RoundEndEvent;
import net.caseif.flint.event.round.RoundEvent;
import net.caseif.flint.round.Round;

/**
 * Implementation of {@link RoundEndEvent}.
 *
 * @author Max Roncacé
 */
public class SteelRoundEndEvent extends SteelRoundEvent implements RoundEndEvent {

    private boolean natural;

    protected SteelRoundEndEvent(Round round, boolean natural) {
        super(round);
        this.natural = natural;
    }

    @Override
    public boolean isNatural() {
        return natural;
    }
}
