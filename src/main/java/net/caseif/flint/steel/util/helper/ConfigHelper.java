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
        File configYml = new File(SteelMain.getInstance().getDataFolder(), "config.yml");
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
                    } catch (ParseException ex) {
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
