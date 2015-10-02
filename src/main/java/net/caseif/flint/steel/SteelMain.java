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
package net.caseif.flint.steel;

import net.caseif.flint.steel.listener.misc.LobbyListener;
import net.caseif.flint.steel.listener.player.PlayerConnectionListener;
import net.caseif.flint.steel.listener.player.PlayerWorldListener;
import net.caseif.flint.steel.listener.plugin.PluginListener;
import net.caseif.flint.steel.listener.rollback.RollbackBlockListener;
import net.caseif.flint.steel.listener.rollback.RollbackEntityListener;
import net.caseif.flint.steel.listener.rollback.RollbackInventoryListener;
import net.caseif.flint.steel.listener.rollback.breaking.v18.BreakingV18RollbackEntityListener;
import net.caseif.flint.steel.util.file.DataFiles;
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
 * @version 1.0.0-SNAPSHOT
 */
public class SteelMain extends JavaPlugin {

    private static SteelMain instance;

    @Override
    public void onEnable() {
        instance = this;
        SteelCore.initialize();

        registerEvents();

        saveDefaultConfig();
        try {
            ConfigHelper.addMissingKeys();
        } catch (InvalidConfigurationException | IOException ex) {
            ex.printStackTrace();
            SteelCore.logWarning("Failed to write missing config keys");
        }
        DataFiles.createCoreDataFiles();

        try {
            Class.forName("org.sqlite.JDBC"); // load the SQL driver
        } catch (ClassNotFoundException ex) {
            getLogger().severe("Failed to load SQL driver");
            ex.printStackTrace();
        }

        initMetrics();
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

    public void initUpdater() {
        if (getConfig().getBoolean("enable-updater")) {
            new Updater(this, 37669, this.getFile(), Updater.UpdateType.DEFAULT, true);
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
