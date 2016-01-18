/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016, Max Roncace <me@caseif.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.caseif.flint.steel.util.file;

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
