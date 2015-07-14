package net.caseif.flint.steel.util.io;

import net.caseif.flint.Minigame;
import net.caseif.flint.steel.SteelMain;

import java.io.File;

/**
 * Represents a global Flint data file.
 *
 * @author Max Roncacé
 */
public class CoreDataFile extends DataFile {

    public CoreDataFile(String fileName, boolean isDirectory) {
        super(fileName, isDirectory);
    }

    public CoreDataFile(String fileName) {
        super(fileName);
    }

    /**
     * Gets the {@link File} backing this {@link CoreDataFile} for the given
     * {@link Minigame}.
     *
     * @return The {@link File} backing this {@link CoreDataFile} for the
     *     given {@link Minigame}.
     */
    public File getFile() {
        return new File(SteelMain.getPlugin().getDataFolder(),
                DataFiles.ROOT_DATA_DIR + File.pathSeparatorChar + getFileName());
    }

}
