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

package net.caseif.flint.steel;

import net.caseif.flint.steel.listener.misc.LobbyListener;
import net.caseif.flint.steel.listener.player.PlayerConnectionListener;
import net.caseif.flint.steel.listener.player.PlayerWorldListener;
import net.caseif.flint.steel.listener.plugin.PluginListener;
import net.caseif.flint.steel.listener.rollback.RollbackBlockListener;
import net.caseif.flint.steel.listener.rollback.RollbackEntityListener;
import net.caseif.flint.steel.listener.rollback.RollbackInventoryListener;
import net.caseif.flint.steel.listener.rollback.breaking.v18.BreakingV18RollbackEntityListener;
import net.caseif.flint.steel.util.TelemetryRunner;
import net.caseif.flint.steel.util.compatibility.CoreDataMigrationAgent;
import net.caseif.flint.steel.util.file.SteelDataFiles;
import net.caseif.flint.steel.util.helper.ConfigHelper;

import net.gravitydevelopment.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;

/**
 * The main plugin class.
 *
 * @author Max Roncacé
 */
public class SteelMain extends JavaPlugin {

    private static final int CURSEFORGE_PROJECT_ID = 95203;

    private static SteelMain instance;

    @Override
    public void onEnable() {
        instance = this;
        SteelCore.initializeSteel();

        registerEvents();

        saveDefaultConfig();
        try {
            ConfigHelper.addMissingKeys();
        } catch (InvalidConfigurationException | IOException ex) {
            ex.printStackTrace();
            SteelCore.logWarning("Failed to write missing config keys");
        }
        SteelDataFiles.createCoreDataFiles();

        try {
            Class.forName("org.sqlite.JDBC"); // load the SQL driver
        } catch (ClassNotFoundException ex) {
            getLogger().severe("Failed to load SQL driver");
            ex.printStackTrace();
        }

        new CoreDataMigrationAgent().migrateData();

        initMetrics();
        initTelemetry();
        initUpdater();
    }

    @Override
    public void onDisable() {
    }

    public static SteelMain getInstance() {
        return instance;
    }

    public void initMetrics() {
        if (getConfig().getBoolean("enable-metrics")) {
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            } catch (IOException ex) {
                ex.printStackTrace();
                getLogger().severe("Failed to enable Plugin Metrics!");
            }
        }
    }

    public void initTelemetry() {
        if (getConfig().getBoolean("enable-metrics")) {
            Bukkit.getScheduler().runTask(this, new TelemetryRunner());
        }
    }

    public void initUpdater() {
        if (getConfig().getBoolean("enable-updater")) {
            new Updater(this, CURSEFORGE_PROJECT_ID, this.getFile(), Updater.UpdateType.DEFAULT, true);
        }
    }

    public void registerEvents() {
        // standard event registration
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(), getInstance());
        Bukkit.getPluginManager().registerEvents(new PlayerWorldListener(), getInstance());

        Bukkit.getPluginManager().registerEvents(new PluginListener(), getInstance());

        Bukkit.getPluginManager().registerEvents(new RollbackBlockListener(), getInstance());
        Bukkit.getPluginManager().registerEvents(new RollbackEntityListener(), getInstance());
        Bukkit.getPluginManager().registerEvents(new RollbackInventoryListener(), getInstance());

        Bukkit.getPluginManager().registerEvents(new LobbyListener(), getInstance());

        // breaking event registration (for newer event types)
        try {
            Bukkit.getPluginManager().registerEvents(new BreakingV18RollbackEntityListener(), getInstance());
        } catch (NoClassDefFoundError ex) {
            SteelCore.logVerbose("Server does not support 1.8 events - not registering");
        }
    }

}
