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

import org.bukkit.ChatColor;

/**
 * Static utility class for wizard messages.
 */
public class WizardMessages {

    static final ChatColor INFO_COLOR = ChatColor.DARK_AQUA;
    static final ChatColor ERROR_COLOR = ChatColor.RED;
    static final ChatColor EM_COLOR = ChatColor.GOLD;

    // informational messages
    static final String WELCOME = INFO_COLOR + "Welcome to the lobby sign wizard!";
    static final String CHAT_WITHHOLDING = INFO_COLOR + "Chat messages will be withheld until the wizard is complete.";
    static final String GET_ARENA = INFO_COLOR + "To start, please type "
            + "the name of the arena you would like to create a lobby sign for. (You may type " + EM_COLOR + " cancel "
            + INFO_COLOR + " at any time to exit the wizard.";
    static final String GET_TYPE = INFO_COLOR + "Next, please select the type of lobby sign you would like "
            + "to create from the list below (type a number):";
    static final String GET_TYPE_STATUS = EM_COLOR + "1) " + INFO_COLOR + "Status - Status signs display "
            + "basic information about the round contained by an arena such as timer info and player count.";
    static final String GET_TYPE_LISTING = EM_COLOR + "2) " + INFO_COLOR + "Player Listing - Player listing "
            + "signs display a portion of the players in a round. If this is selected, you will be asked next to "
            + "define which portion of players to display.";
    static final String GET_INDEX = INFO_COLOR + " Next, please select which portion of players you would "
            + "like the player listing sign to display. ("
            + EM_COLOR + "1" + INFO_COLOR + " will display players " + EM_COLOR + "1-4" + INFO_COLOR + ", "
            + EM_COLOR + "2" + INFO_COLOR + " will display players " + EM_COLOR + "5-8" + INFO_COLOR + ", "
            + "and so on.)";
    static final String CONFIRM_1 = INFO_COLOR + "Okay! Your lobby sign will be created with the following "
            + "info:";
    static final String CONFIRM_2 = INFO_COLOR + "Is this okay? "
            + "(Type " + EM_COLOR + "yes" + INFO_COLOR + " or " + EM_COLOR + "no" + INFO_COLOR + ".)";
    static final String RESET = INFO_COLOR + "The wizard will now reset.";
    static final String FINISH = INFO_COLOR + "Your lobby sign was successfully created! The wizard will now exit.";
    static final String MESSAGE_PLAYBACK = INFO_COLOR + "Playing back withheld chat messages...";

    static final String CANCELLED = ERROR_COLOR + "Lobby sign creation cancelled.";

    // error messages
    static final String BAD_ARENA = ERROR_COLOR + "No arena by that ID exists! Please enter another arena ID.";
    static final String BAD_TYPE = ERROR_COLOR + "Invalid sign type! Please select a valid option.";
    static final String BAD_INDEX = ERROR_COLOR + "Invalid sign index! Please enter a number greater than or equal to "
            + "1.";
    static final String BAD_CONFIRMATION = ERROR_COLOR + "Please type " + EM_COLOR + "yes" + INFO_COLOR + " or "
            + EM_COLOR + "no" + INFO_COLOR + ".";
    static final String ARENA_REMOVED = ERROR_COLOR + "The selected arena has been removed. The wizard will now exit.";
    static final String GENERIC_ERROR = ERROR_COLOR + "An internal exception occurred while creating the lobby sign. "
            + "The wizard will now exit.";

    // other stuff
    static final String DIVIDER;

    static {
        final int dividerLength = 36;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dividerLength; i++) {
            sb.append("-");
        }
        DIVIDER = sb.toString();
    }

}
