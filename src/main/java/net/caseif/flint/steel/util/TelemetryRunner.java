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
package net.caseif.flint.steel.util;

import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.SteelMain;
import net.caseif.flint.steel.util.file.SteelDataFiles;

import net.caseif.jtelemetry.JTelemetry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * Runner class for telemetry data submission.
 */
public class TelemetryRunner implements Runnable {

    private static final String KEY_UUID = "uuid";
    private static final String KEY_VERSION = "version";
    private static final String KEY_API_LEVEL = "api";
    private static final String KEY_JAVA_VERSION = "java";
    private static final String KEY_MINIGAME_COUNT = "mgCount";
    private static final String KEY_MINIGAMES = "minigames";

    private static final String TELEMETRY_SERVER = "http://telemetry.caseif.net/steel.php";

    private final JTelemetry jt = new JTelemetry(TELEMETRY_SERVER);

    @Override
    public void run() {
        JTelemetry.Payload payload = jt.createPayload();

        UUID uuid;
        try {
            uuid = getUuid();
        } catch (IOException ex) {
            SteelCore.logSevere("Encountered IOException while getting telemetry UUID - not submitting data");
            ex.printStackTrace();
            return;
        }

        payload.addData(KEY_UUID, uuid.toString());
        payload.addData(KEY_VERSION, SteelMain.getInstance().getDescription().getVersion());
        payload.addData(KEY_API_LEVEL, SteelCore.getApiRevision());
        payload.addData(KEY_JAVA_VERSION, System.getProperty("java.version"));
        payload.addData(KEY_MINIGAME_COUNT, SteelCore.getMinigames().size());

        String[] plugins = new String[SteelCore.getMinigames().size()];
        int i = 0;
        for (String plugin : SteelCore.getMinigames().keySet()) {
            plugins[i] = plugin;
            i++;
        }
        payload.addData(KEY_MINIGAMES, plugins);

        try {
            JTelemetry.HttpResponse response = payload.submit();
            if (response.getStatusCode() / 100 != 2) { // not 2xx response code
                SteelCore.logWarning("Telemetry server responded with non-success status code ("
                        + response.getStatusCode() + " " + response.getMessage() + "). Please report this.");
            }
        } catch (IOException ex) {
            SteelCore.logSevere("Encountered IOException while submitting telemetry data to remote server");
            ex.printStackTrace();
        }
    }

    private static UUID getUuid() throws IOException {
        File uuidFile = SteelDataFiles.TELEMETRY_UUID_STORE.getFile();
        if (!uuidFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            uuidFile.createNewFile();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(uuidFile))) {
            String uuid = reader.readLine();
            try {
                if (uuid == null) {
                    throw new IllegalArgumentException();
                }
                return UUID.fromString(uuid);
            } catch (IllegalArgumentException ex) {
                UUID newUuid = UUID.randomUUID();
                try (FileWriter writer = new FileWriter(uuidFile)) {
                    writer.write(newUuid.toString());
                }
                return newUuid;
            }
        }
    }

}
