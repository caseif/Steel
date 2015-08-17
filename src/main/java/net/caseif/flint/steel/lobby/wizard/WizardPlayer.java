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

import static net.caseif.flint.steel.lobby.wizard.WizardMessages.EM_COLOR;
import static net.caseif.flint.steel.lobby.wizard.WizardMessages.ERROR_COLOR;
import static net.caseif.flint.steel.lobby.wizard.WizardMessages.INFO_COLOR;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.lobby.type.ChallengerListingLobbySign;
import net.caseif.flint.lobby.type.StatusLobbySign;
import net.caseif.flint.steel.SteelMain;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implements {@link IWizardPlayer}.
 *
 * @author Max Roncacé
 */
class WizardPlayer implements IWizardPlayer {

    private UUID uuid;
    private Location3D location;
    private WizardManager manager;

    private WizardStage stage;
    private WizardCollectedData data;

    private List<String[]> withheldMessages = new ArrayList<>();

    /**
     * Creates a new {@link WizardPlayer} with the given {@link UUID} for the
     * given {@link WizardManager}.
     *
     * @param uuid The {@link UUID} of the player backing this
     *     {@link WizardPlayer}
     * @param manager The parent {@link WizardManager} of the new
     *     {@link WizardManager}
     */
    WizardPlayer(UUID uuid, Location3D location, WizardManager manager) {
        this.uuid = uuid;
        this.location = location;
        this.manager = manager;

        this.stage = WizardStage.GET_ARENA;
        this.data = new WizardCollectedData();
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public Location3D getLocation() {
        return location;
    }

    @Override
    public WizardManager getParent() {
        return manager;
    }



    @Override
    public String[] accept(String input) {
        if (input.equalsIgnoreCase("cancel")) {
            getParent().removePlayer(getUniqueId());
            playbackWithheldMessages();
            return new String[]{WizardMessages.CANCELLED};
        }
        switch (stage) {
            case GET_ARENA: {
                Optional<Arena> arena = getParent().getOwner().getArena(input);
                if (arena.isPresent()) {
                    data.setArena(input);
                    stage = WizardStage.GET_TYPE;
                    return new String[]{WizardMessages.DIVIDER, WizardMessages.GET_TYPE,
                            WizardMessages.GET_TYPE_STATUS, WizardMessages.GET_TYPE_LISTING};
                } else {
                    return new String[]{WizardMessages.BAD_ARENA};
                }
            }
            case GET_TYPE: {
                try {
                    int i = Integer.parseInt(input);
                    switch (i) {
                        case 1: {
                            data.setSignType(LobbySign.Type.STATUS);
                            stage = WizardStage.CONFIRMATION;
                            return constructConfirmation();
                        }
                        case 2: {
                            data.setSignType(LobbySign.Type.CHALLENGER_LISTING);
                            stage = WizardStage.GET_INDEX;
                            return new String[]{WizardMessages.DIVIDER, WizardMessages.GET_INDEX};
                        }
                        default: {
                            break; // continue to block end
                        }
                    }
                } catch (NumberFormatException ignored) {
                    // continue to block end
                }
                return new String[]{WizardMessages.BAD_TYPE};
            }
            case GET_INDEX: {
                try {
                    int i = Integer.parseInt(input);
                    if (i > 0) {
                        data.setIndex(i - 1);
                        stage = WizardStage.CONFIRMATION;
                        return constructConfirmation();
                    } // else: continue to block end
                } catch (NumberFormatException ex) {
                    // continue to block end
                }
                return new String[]{WizardMessages.BAD_INDEX};
            }
            case CONFIRMATION: {
                if (input.equalsIgnoreCase("yes")) {
                    Optional<Arena> arena = getParent().getOwner().getArena(data.getArena());
                    if (arena.isPresent()) {
                            switch (data.getSignType()) {
                                case STATUS: {
                                    try {
                                        Optional<StatusLobbySign> sign
                                                = arena.get().createStatusLobbySign(getLocation());
                                        if (sign.isPresent()) {
                                            getParent().removePlayer(getUniqueId());
                                            playbackWithheldMessages();
                                            return new String[]{WizardMessages.DIVIDER, WizardMessages.FINISH};
                                        } else {
                                            return new String[]{WizardMessages.DIVIDER, WizardMessages.GENERIC_ERROR};
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        return new String[]{WizardMessages.DIVIDER, WizardMessages.GENERIC_ERROR,
                                                ERROR_COLOR + ex.getMessage()};
                                    }
                                }
                                case CHALLENGER_LISTING: {
                                    Optional<ChallengerListingLobbySign> sign = arena.get()
                                            .createChallengerListingLobbySign(getLocation(), data.getIndex());
                                    if (sign.isPresent()) {
                                        getParent().removePlayer(getUniqueId());
                                        playbackWithheldMessages();
                                        return new String[]{WizardMessages.DIVIDER, WizardMessages.FINISH};
                                    } else {
                                        return new String[]{WizardMessages.DIVIDER, WizardMessages.GENERIC_ERROR};
                                    }
                                }
                                default: {
                                    throw new AssertionError("Invalid sign type in wizard data. "
                                            + "Report this immediately.");
                                }
                            }
                    } else {
                        getParent().removePlayer(getUniqueId());
                        playbackWithheldMessages();
                        return new String[]{WizardMessages.DIVIDER, WizardMessages.ARENA_REMOVED};
                    }
                } else if (input.equalsIgnoreCase("no")) {
                    stage = WizardStage.GET_ARENA;
                    return new String[]{WizardMessages.DIVIDER, WizardMessages.RESET, WizardMessages.DIVIDER,
                            WizardMessages.GET_ARENA};
                } else {
                    return new String[]{WizardMessages.BAD_CONFIRMATION};
                }
            }
            default: {
                throw new AssertionError("Cannot process input for wizard player. Report this immediately.");
            }
        }
    }

    public void withholdMessage(String sender, String message) {
        withheldMessages.add(new String[]{sender, message});
    }

    private void playbackWithheldMessages() {
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

    private String[] constructConfirmation() {
        ArrayList<String> msgs = new ArrayList<>();
        msgs.add(WizardMessages.DIVIDER);
        msgs.add(WizardMessages.CONFIRM_1);
        msgs.add(INFO_COLOR + "Arena ID: " + EM_COLOR + data.getArena());
        msgs.add(INFO_COLOR + "Sign type: " + EM_COLOR + data.getSignType().toString());
        if (data.getSignType() == LobbySign.Type.CHALLENGER_LISTING) {
            msgs.add(INFO_COLOR + "Sign index: " + EM_COLOR + (data.getIndex() + 1));
        }
        msgs.add(WizardMessages.CONFIRM_2);
        String[] arr = new String[msgs.size()];
        msgs.toArray(arr);
        return arr;
    }

}
