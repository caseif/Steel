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

import com.google.common.base.Preconditions;
import net.caseif.flint.FlintCore;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.component.CommonComponent;
import net.caseif.flint.common.util.agent.chat.IChatAgent;
import net.caseif.flint.common.util.agent.rollback.IRollbackAgent;
import net.caseif.flint.common.util.factory.FactoryRegistry;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.round.Round;
import net.caseif.flint.steel.util.SteelUtils;
import net.caseif.flint.steel.util.agent.chat.ChatAgent;
import net.caseif.flint.steel.util.factory.ArenaFactory;
import net.caseif.flint.steel.util.factory.LobbySignFactory;
import net.caseif.flint.steel.util.factory.MinigameFactory;
import net.caseif.flint.steel.util.factory.RollbackAgentFactory;
import net.caseif.flint.steel.util.factory.RoundFactory;
import net.caseif.flint.steel.util.helper.LegacyHelper;
import net.caseif.flint.steel.util.unsafe.SteelUnsafeUtil;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;

/**
 * Implements {@link FlintCore}.
 *
 * @author Max Roncacé
 */
public class SteelCore extends CommonCore {

    public static final boolean SPECTATOR_SUPPORT;

    private static boolean VERBOSE_LOGGING;

    private static final ChatAgent CHAT_AGENT = new ChatAgent();

    public static final int MC_113_TRANSFORMED = 1_013_000;
    public static final int MC_114_TRANSFORMED = 1_014_000;

    private static int mcVersion;
    private static boolean legacyMcVersion;
    private static LegacyHelper legacyHelper;

    static {
        INSTANCE = new SteelCore();

        boolean javacIsStupid = false;
        try {
            GameMode.valueOf("SPECTATOR");
            javacIsStupid = true;
        } catch (IllegalArgumentException ignored) {
        }
        SPECTATOR_SUPPORT = javacIsStupid;
    }

    static void initializeSteel() {
        checkIfLegacyMinecraftVersion();

        VERBOSE_LOGGING = SteelMain.getInstance().getConfig().getBoolean("verbose-logging");

        CommonCore.initializeCommon();
        registerFactories();
        SteelUnsafeUtil.initialize();

        PLATFORM_UTILS = new SteelUtils();
    }

    private static void checkIfLegacyMinecraftVersion() {
        String[] mcVersions = Bukkit.getBukkitVersion().split("-")[0].split("\\.");

        mcVersion = (Integer.parseInt(mcVersions[0]) * 1_000_000)
                + (Integer.parseInt(mcVersions[1]) * 1_000)
                + (mcVersions.length > 2 ? Integer.parseInt(mcVersions[2]) : 0);

        legacyMcVersion = mcVersion < MC_113_TRANSFORMED;

        if (legacyMcVersion) {
            legacyHelper = new LegacyHelper();
        } else {
            //TODO: remove when safe
            logWarning("This server is running Minecraft version 1.13 or later.");
            logWarning("Steel's support for this version may be incomplete or unstable.");
            logWarning("Please report any issues at https://github.com/caseif/Steel/issues");
        }
    }

    private static void registerFactories() {
        FactoryRegistry.registerFactory(Arena.class, new ArenaFactory());
        FactoryRegistry.registerFactory(LobbySign.class, new LobbySignFactory());
        FactoryRegistry.registerFactory(Minigame.class, new MinigameFactory());
        FactoryRegistry.registerFactory(IRollbackAgent.class, new RollbackAgentFactory());
        FactoryRegistry.registerFactory(Round.class, new RoundFactory());
    }

    public static boolean isLegacy() {
        return legacyMcVersion;
    }

    public static int getMcVersion() {
        return mcVersion;
    }

    public static LegacyHelper getLegacyHelper() {
        Preconditions.checkState(legacyMcVersion, "Cannot get legacy helper on non-legacy platform!");

        return legacyHelper;
    }

    @Override
    protected String getImplementationName0() {
        return SteelMain.getInstance().getName();
    }

    @Override
    protected void logInfo0(String message) {
        SteelMain.getInstance().getLogger().info(message);
    }

    @Override
    protected void logWarning0(String message) {
        SteelMain.getInstance().getLogger().warning(message);
    }

    @Override
    protected void logSevere0(String message) {
        SteelMain.getInstance().getLogger().severe(message);
    }

    @Override
    protected void logVerbose0(String message) {
        if (VERBOSE_LOGGING) {
            logInfo0("[VERBOSE] " + message);
        }
    }

    @Override
    protected void orphan0(final CommonComponent<?> component) {
        Bukkit.getScheduler().runTask(SteelMain.getInstance(), new Runnable() {
            public void run() {
                component.setOrphanFlag();
            }
        });
    }

    protected IChatAgent getChatAgent0() {
        return CHAT_AGENT;
    }

}
