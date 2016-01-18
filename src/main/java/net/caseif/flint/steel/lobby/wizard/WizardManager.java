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

import net.caseif.flint.component.Component;
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
public class WizardManager implements Component<SteelMinigame> {

    private SteelMinigame minigame;

    private final HashMap<UUID, IWizardPlayer> wizardPlayers = new HashMap<>();

    /**
     * Creates a new {@link WizardManager} for the given {@link Minigame}.
     *
     * @param minigame The {@link Minigame} to back the new {@link WizardManager}
     */
    public WizardManager(Minigame minigame) {
        this.minigame = (SteelMinigame) minigame;
    }

    @Override
    public SteelMinigame getOwner() {
        return minigame;
    }

    /**
     * Gets whether the player with the given {@link UUID} is present in this
     * {@link WizardManager}.
     *
     * @param uuid The {@link UUID} of the player to look up
     * @return Whether the player is present in this {@link WizardManager}
     */
    public boolean isWizardPlayer(UUID uuid) {
        return wizardPlayers.containsKey(uuid);
    }

    /**
     * Adds a player to this {@link WizardManager}.
     *
     * @param uuid The {@link UUID} of the player
     * @param location The {@link Location3D location} targeted by the player
     */
    public void addWizardPlayer(UUID uuid, Location3D location) {
        assert !wizardPlayers.containsKey(uuid);
        wizardPlayers.put(uuid, new WizardPlayer(uuid, location, this));
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            throw new AssertionError("Cannot get Bukkit player from UUID in wizard manager. Report this immediately.");
        }
        player.sendMessage(new String[]{WizardMessages.WELCOME, WizardMessages.CHAT_WITHHOLDING, WizardMessages.DIVIDER,
                WizardMessages.GET_ARENA});
    }

    /**
     * Accepts input from the player with the given {@link UUID}.
     *
     * @param uuid The {@link UUID} of the player to accept input from
     * @param input The input to accept
     * @return The response to the input
     * @throws IllegalArgumentException If the player with the given
     *     {@link UUID} is not currently engaged in a wizard
     */
    public String[] accept(UUID uuid, String input) {
        if (wizardPlayers.containsKey(uuid)) {
            return wizardPlayers.get(uuid).accept(input);
        } else {
            throw new IllegalArgumentException("Player with UUID " + uuid.toString() + " is not engaged in a wizard");
        }
    }

    public void withholdMessage(UUID uuid,  String sender, String message) {
        if (wizardPlayers.containsKey(uuid)) {
            wizardPlayers.get(uuid).withholdMessage(sender, message);
        } else {
            throw new IllegalArgumentException("Player with UUID " + uuid.toString() + " is not engaged in a wizard");
        }
    }

    /**
     * Removes the player with the given {@link UUID} from this
     * {@link WizardManager}.
     *
     * @param uuid The {@link UUID} of the player to remove
     */
    public void removePlayer(UUID uuid) {
        wizardPlayers.remove(uuid);
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
