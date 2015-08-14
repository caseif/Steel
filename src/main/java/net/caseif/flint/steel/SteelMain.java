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

import net.caseif.flint.common.event.FlintSubscriberExceptionHandler;
import net.caseif.flint.steel.listener.player.PlayerConnectionListener;
import net.caseif.flint.steel.listener.player.PlayerWorldListener;
import net.caseif.flint.steel.listener.plugin.PluginListener;
import net.caseif.flint.steel.listener.rollback.RollbackBlockListener;
import net.caseif.flint.steel.listener.rollback.RollbackEntityListener;
import net.caseif.flint.steel.listener.rollback.RollbackInventoryListener;
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

    private static JavaPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        SteelCore.initialize();

        try {
            Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(), getPlugin());
            Bukkit.getPluginManager().registerEvents(new PlayerWorldListener(), getPlugin());

            Bukkit.getPluginManager().registerEvents(new PluginListener(), getPlugin());

            Bukkit.getPluginManager().registerEvents(new RollbackBlockListener(), getPlugin());
            Bukkit.getPluginManager().registerEvents(new RollbackEntityListener(), getPlugin());
            Bukkit.getPluginManager().registerEvents(new RollbackInventoryListener(), getPlugin());
        } catch (NoClassDefFoundError ignored) { // thrown if an event is unsupported on the current server software
        }

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
        //initUpdater(); //TODO
    }

    @Override
    public void onDisable() {
        FlintSubscriberExceptionHandler.deinitialize();
    }

    public static JavaPlugin getPlugin() {
        return plugin;
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
            new Updater(this, -1, this.getFile(), Updater.UpdateType.DEFAULT, true);
        }
    }


}
