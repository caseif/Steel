package net.caseif.steel.event.round;

import net.caseif.flint.event.round.RoundChangeLifecycleStageEvent;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;

/**
 * Implementation of {@link RoundChangeLifecycleStageEvent}.
 *
 * @author Max Roncacé
 */
public class SteelRoundChangeLifecycleStageEvent extends SteelRoundEvent implements RoundChangeLifecycleStageEvent {

    private LifecycleStage before;
    private LifecycleStage after;

    protected SteelRoundChangeLifecycleStageEvent(Round round, LifecycleStage before, LifecycleStage after) {
        super(round);
        this.before = before;
        this.after = after;
    }

    @Override
    public LifecycleStage getStageBefore() {
        return before;
    }

    @Override
    public LifecycleStage getStageAfter() {
        return after;
    }
}
