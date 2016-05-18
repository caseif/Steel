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

import static net.caseif.flint.common.lobby.wizard.WizardMessages.INFO_COLOR;

import net.caseif.flint.common.lobby.wizard.IWizardManager;
import net.caseif.flint.common.lobby.wizard.CommonWizardPlayer;
import net.caseif.flint.common.lobby.wizard.WizardMessages;
import net.caseif.flint.steel.SteelMain;
import net.caseif.flint.steel.util.helper.LocationHelper;
import net.caseif.flint.util.physical.Location3D;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Implements {@link CommonWizardPlayer}.
 *
 * @author Max Roncacé
 */
class WizardPlayer extends CommonWizardPlayer {

    private Material origMaterial;
    private byte origData;

    /**
     * Creates a new {@link WizardPlayer} with the given {@link UUID} for the
     * given {@link WizardManager}.
     *
     * @param uuid The {@link UUID} of the player backing this
     *     {@link WizardPlayer}
     * @param manager The parent {@link WizardManager} of the new
     *     {@link WizardManager}
     */
    @SuppressWarnings("deprecation")
    WizardPlayer(UUID uuid, Location3D location, IWizardManager manager) {
        super(uuid, location, manager);
    }

    @Override
    public void playbackWithheldMessages() {
        Bukkit.getScheduler().runTask(SteelMain.getInstance(), new Runnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(uuid);
                player.sendMessage(INFO_COLOR
                        + WizardMessages.MESSAGE_PLAYBACK);
                for (String[] msg : withheldMessages) {
                    player.sendMessage("<" + msg[0] + "> " + msg[1]);
                }
            }
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void recordTargetBlockState() {
        assert LocationHelper.convertLocation(location).getBlock().getState() instanceof Sign;
        this.origMaterial = LocationHelper.convertLocation(location).getBlock().getType();
        this.origData = LocationHelper.convertLocation(location).getBlock().getState().getRawData();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void restoreTargetBlock() {
        Block b = LocationHelper.convertLocation(getLocation()).getBlock();
        b.setType(origMaterial);
        b.setData(origData);
    }

}
