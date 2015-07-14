package net.caseif.flint.steel.util.io;

/**
 * Represents a data file for use with Flint.
 *
 * @author Max Roncacé
 */
public abstract class DataFile {

    private String fileName;
    private boolean directory;

    /**
     * Constructs a new {@link DataFile}.
     *
     * @param fileName The name of the file backing the new
     *     {@link DataFile}
     * @param isDirectory Whether this {@link DataFile} is a directory
     */
    public DataFile(String fileName, boolean isDirectory) {
        this.fileName = fileName;
        this.directory = isDirectory;
        DataFiles.register(this);
    }

    /**
     * Constructs a new {@link DataFile}.
     *
     * @param fileName The name of the file backing the new
     *     {@link DataFile}
     */
    public DataFile(String fileName) {
        this(fileName, false);
    }

    /**
     * Returns whether this {@link CoreDataFile} is a directory.
     *
     * @return Whether this {@link CoreDataFile} is a directory.
     */
    public boolean isDirectory() {
        return directory;
    }

    /**
     * Returns the internal name of this {@link CoreDataFile}.
     *
     * @return The internal name of this {@link CoreDataFile}
     */
    String getFileName() {
        return fileName;
    }

}
