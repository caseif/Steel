package net.caseif.steel;

import net.caseif.flint.FlintCore;
import net.caseif.flint.Minigame;

/**
 * The implementation of {@link FlintCore}.
 *
 * @author Max Roncacé
 */
public class SteelCore extends FlintCore {

    static {
        INSTANCE = new SteelCore();
    }

    @Override
    public Minigame registerPlugin(String pluginId) {
        return new SteelMinigame(pluginId);
    }

}
