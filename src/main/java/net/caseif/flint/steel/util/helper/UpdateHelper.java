/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2022, Max Roncace <me@caseif.net>
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

package net.caseif.flint.steel.util.helper;

import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.SteelMain;
import net.caseif.flint.steel.lib.net.gravitydevelopment.updater.Updater;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UpdateHelper {

    private static final int CURSEFORGE_PROJECT_ID = 95203;

    private static final Field UPDATER_DELIMETER;
    private static final Field UPDATER_TYPE;

    private static final Method UPDATER_RUN_UPDATER;

    static {
        try {
            UPDATER_DELIMETER = Updater.class.getDeclaredField("DELIMETER");
            UPDATER_DELIMETER.setAccessible(true);
            UPDATER_TYPE = Updater.class.getDeclaredField("type");
            UPDATER_TYPE.setAccessible(true);
            UPDATER_RUN_UPDATER = Updater.class.getDeclaredMethod("runUpdater");
            UPDATER_RUN_UPDATER.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException ex) {
            throw new RuntimeException("Failed to initialize updater!");
        }
    }

    public static void run() {
        try {
            if (SteelMain.getInstance().getConfig().getBoolean("enable-updater")) {
                Updater checker = new Updater(SteelMain.getInstance(), CURSEFORGE_PROJECT_ID,
                        SteelMain.getInstance().getFile(), Updater.UpdateType.NO_DOWNLOAD,
                        true);
                if (checker.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE) {
                    String remoteVersion = checker.getLatestName().split((String) UPDATER_DELIMETER.get(null))[1];
                    int remoteMajor = Integer.parseInt(remoteVersion.split("\\.")[0]);
                    int localMajor
                            = Integer.parseInt(SteelMain.getInstance().getDescription().getVersion().split("\\.")[0]);
                    if (remoteMajor == localMajor) {
                        UPDATER_TYPE.set(checker, Updater.UpdateType.DEFAULT);
                        UPDATER_RUN_UPDATER.invoke(checker);
                    } else if (remoteMajor > localMajor) {
                        SteelCore.logInfo("A new major version (" + remoteVersion + ") of Steel is available! Keep in "
                                + "mind this may contain breaking changes, so ensure that your minigame plugins are "
                                + "compatible with the new major version before manually upgrading.");
                    } else {
                        SteelCore.logWarning("Local major version is greater than remote - something is not right");
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NumberFormatException ex) {
            throw new RuntimeException("Failed to run updater", ex);
        }
    }
}
