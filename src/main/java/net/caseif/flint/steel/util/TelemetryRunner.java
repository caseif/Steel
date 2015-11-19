/*
 * New BSD License (BSD-new)
 *
 * Copyright (c) 2015 Maxim Roncacé
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.caseif.flint.steel.util;

import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.SteelMain;
import net.caseif.flint.steel.util.file.DataFiles;
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
        File uuidFile = DataFiles.TELEMETRY_UUID_STORE.getFile();
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
