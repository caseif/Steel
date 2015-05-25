package net.caseif.steel.round;

import net.caseif.steel.event.round.SteelRoundTimerTickEvent;
import net.caseif.steel.util.MiscUtil;

import com.google.common.base.Optional;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;

/**
 * Used as the {@link Runnable} for {@link Round} timers.
 *
 * @author Max Roncacé
 */
public class RoundWorker implements Runnable {

    private SteelRound round;

    public RoundWorker(SteelRound round) {
        this.round = round;
    }

    public void run() {
        boolean stageSwitch = round.getTime() >= round.getLifecycleStage().getDuration();
        SteelRoundTimerTickEvent event = new SteelRoundTimerTickEvent(round, round.getTime(),
                stageSwitch ? 0 : round.getTime() + 1);
        MiscUtil.callEvent(event);
        if (stageSwitch) {
            Optional<LifecycleStage> nextStage = round.getNextLifecycleStage();
            if (nextStage.isPresent()) {
                round.setTime(0);
                round.setLifecycleStage(nextStage.get());
            } else {
                round.end();
                return;
            }
        } else {
            round.setTime(round.getTime() + 1, false);
        }
    }

}
