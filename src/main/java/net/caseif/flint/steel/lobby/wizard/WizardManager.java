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
package net.caseif.flint.steel.lobby.wizard;

import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.util.MinigameElement;
import net.caseif.flint.util.physical.Location3D;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * Manager for the integrated lobby wizard.
 */
public class WizardManager implements MinigameElement {

    private Minigame minigame;

    private final HashMap<UUID, IWizardPlayer> wizardPlayers = new HashMap<>();

    /**
     * Creates a new {@link WizardManager} for the given {@link Minigame}.
     *
     * @param minigame The {@link Minigame} to back the new {@link WizardManager}
     */
    public WizardManager(Minigame minigame) {
        this.minigame = minigame;
    }

    @Override
    public Minigame getMinigame() {
        return minigame;
    }

    @Override
    public String getPlugin() {
        return getMinigame().getPlugin();
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
