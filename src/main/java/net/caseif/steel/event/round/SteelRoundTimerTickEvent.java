package net.caseif.steel.event.round;

import net.caseif.flint.event.round.RoundTimerTickEvent;
import net.caseif.flint.round.Round;

/**
 * Implements {@link RoundTimerTickEvent}.
 *
 * @author Max Roncacé
 */
public class SteelRoundTimerTickEvent extends SteelRoundTimerChangeEvent implements RoundTimerTickEvent {

    public SteelRoundTimerTickEvent(Round round, long oldTime, long newTime) {
        super(round, oldTime, newTime);
    }

}
