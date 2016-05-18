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
package net.caseif.flint.steel.lobby.wizard;

import net.caseif.flint.common.lobby.wizard.CommonWizardManager;
import net.caseif.flint.common.lobby.wizard.IWizardManager;
import net.caseif.flint.common.lobby.wizard.IWizardPlayer;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.steel.minigame.SteelMinigame;
import net.caseif.flint.util.physical.Location3D;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * Manager for the integrated lobby wizard.
 */
public class WizardManager extends CommonWizardManager {

    /**
     * Creates a new {@link WizardManager} for the given {@link Minigame}.
     *
     * @param minigame The {@link Minigame} to back the new {@link WizardManager}
     */
    public WizardManager(Minigame minigame) {
        super(minigame);
    }

    @Override
    public void addPlayer(UUID uuid, Location3D location) {
        assert !wizardPlayers.containsKey(uuid);
        wizardPlayers.put(uuid, new WizardPlayer(uuid, location, this));
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            throw new AssertionError("Cannot get Bukkit player from UUID in wizard manager. Report this immediately.");
        }
    }

    //               _,._
    //   .||,       /_ _\\
    //  \.`',/      |'L'| |
    //  = ,. =      | -,| L
    //  / || \    ,-'\"/,'`.
    //    ||     ,'   `,,. `.
    //    ,|____,' , ,;' \| |
    //   (3|\    _/|/'   _| |
    //    ||/,-''  | >-'' _,\\
    //    ||'      ==\ ,-'  ,'
    //    ||       |  V \ ,|
    //    ||       |    |` |
    //    ||       |    |   \
    //    ||       |    \    \
    //    ||       |     |    \
    //    ||       |      \_,-'
    //    ||       |___,,--")_\
    //    ||         |_|   ccc/
    //    ||        ccc/
    //    ||

}
