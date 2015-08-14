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
package net.caseif.flint.steel.util.helper;

import net.caseif.flint.steel.SteelMain;

import com.google.common.io.Files;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Static utility class for config-related functionality.
 *
 * @author Max Roncacé
 */
public class ConfigHelper {

    public static void addMissingKeys() throws InvalidConfigurationException, IOException {
        BufferedReader is = new BufferedReader(new InputStreamReader(
                ConfigHelper.class.getResourceAsStream("/config.yml")
        ));
        File configYml = new File(SteelMain.getPlugin().getDataFolder(), "config.yml");
        YamlConfiguration yml = new YamlConfiguration();
        yml.load(configYml);
        StringBuilder sb = new StringBuilder();
        final char NEWLINE_CHAR = '\n';
        String line;
        while ((line = is.readLine()) != null) {
            if (!line.startsWith("#")) {
                if (line.contains(":")) {
                    //TODO: this method doesn't support nested keys, but it doesn't need to atm anyway
                    String key = line.split(":")[0];
                    String value = line.substring(key.length() + 1, line.length()).trim();
                    String newValue = yml.contains(key.trim()) ? yml.getString(key.trim()) : value;
                    boolean equal = false;
                    try {
                        equal = NumberFormat.getInstance().parse(value)
                                .equals(NumberFormat.getInstance().parse(newValue));
                    }
                    catch (ParseException ex) {
                        equal = value.equals(newValue);
                    }
                    if (!equal) {
                        String writeValue = yml.getString(key.trim());
                        try {
                            double d /* hehe */ = Double.parseDouble(writeValue);
                            writeValue = BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
                        } catch (NumberFormatException ignored) {
                        }
                        sb.append(key).append(": ").append(writeValue).append(NEWLINE_CHAR);
                        continue;
                    }
                }
            }
            sb.append(line).append(NEWLINE_CHAR);
        }
        if (!configYml.renameTo(new File(configYml.getParentFile(), "config.yml.old"))) {
            Files.copy(configYml, new File(configYml.getParentFile(), "config.yml.old"));
            //noinspection ResultOfMethodCallIgnored
            configYml.delete();
        }
        //noinspection ResultOfMethodCallIgnored
        configYml.createNewFile();
        FileWriter w = new FileWriter(configYml);
        w.append(sb.toString());
        w.flush();
    }

}
