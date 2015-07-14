package net.caseif.flint.steel.util.io;

import net.caseif.flint.Minigame;

import org.bukkit.Bukkit;

import java.io.File;

/**
 * Represents a {@link Minigame}-specific data file.
 *
 * @author Max Roncacé
 */
public class MinigameDataFile extends DataFile {

    public MinigameDataFile(String fileName, boolean isDirectory) {
        super(fileName, isDirectory);
    }

    public MinigameDataFile(String fileName) {
        super(fileName);
    }

    /**
     * Gets the {@link File} backing this {@link MinigameDataFile} for the given
     * {@link Minigame}.
     *
     * @param minigame The {@link Minigame} to retrieve a {@link File} for
     * @return The {@link File} backing this {@link MinigameDataFile} for the
     *     given {@link Minigame}.
     */
    public File getFile(Minigame minigame) {
        return new File(Bukkit.getPluginManager().getPlugin(minigame.getPlugin()).getDataFolder(),
                DataFiles.ROOT_DATA_DIR + File.pathSeparatorChar + getFileName());
    }

}
