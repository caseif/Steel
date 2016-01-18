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

import net.caseif.flint.util.physical.Location3D;

import java.util.UUID;

/**
 * Represents a player currently in the lobby wizard.
 *
 * @author Max Roncacé
 */
interface IWizardPlayer {

    /**
     * Gets the {@link UUID} of this {@link IWizardPlayer}.
     *
     * @return The {@link UUID} of this {@link IWizardPlayer}
     */
    UUID getUniqueId();

    /**
     * Gets the {@link Location3D location} that is the target of this
     * {@link IWizardPlayer}.
     *
     * @return The {@link Location3D location} that is the target of this
     * {@link IWizardPlayer}.
     */
    Location3D getLocation();

    /**
     * Gets the parent {@link WizardManager} of this {@link IWizardPlayer}.
     *
     * @return The parent {@link WizardManager} of this {@link IWizardPlayer}
     */
    WizardManager getParent();

    /**
     * Accepts the given string as input and returns a response string.
     *
     * @param input The input to consider
     * @return The response to the given input
     */
    String[] accept(String input);

    /**
     * Marks the given message as withheld and to be played back when the player
     * exits the wizard.
     *
     * @param sender The sender of the message
     * @param message The message
     */
    void withholdMessage(String sender, String message);

}
