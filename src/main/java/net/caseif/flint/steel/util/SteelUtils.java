package net.caseif.flint.steel.util;

import net.caseif.flint.Minigame;
import net.caseif.flint.common.util.PlatformUtils;
import net.caseif.flint.steel.SteelMain;
import net.caseif.flint.steel.SteelMinigame;

import java.io.File;

/**
 * Implements {@link PlatformUtils} from FlintCommon.
 *
 * @author Max Roncacé
 */
public class SteelUtils implements PlatformUtils {

    @Override
    public File getDataFolder() {
        return SteelMain.getPlugin().getDataFolder();
    }

    @Override
    public File getDataFolder(Minigame minigame) {
        return ((SteelMinigame)minigame).getBukkitPlugin().getDataFolder();
    }
}
