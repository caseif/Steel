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
package net.caseif.flint.steel.util.factory;

import net.caseif.flint.common.util.factory.IArenaFactory;
import net.caseif.flint.common.util.factory.IFactoryRegistry;
import net.caseif.flint.common.util.factory.ILobbySignFactory;
import net.caseif.flint.common.util.factory.IMinigameFactory;
import net.caseif.flint.common.util.factory.IRollbackAgentFactory;
import net.caseif.flint.common.util.factory.IRoundFactory;

public class FactoryRegistry implements IFactoryRegistry {

    private MinigameFactory minigameFac = new MinigameFactory();
    private ArenaFactory arenaFac = new ArenaFactory();
    private RoundFactory roundFac = new RoundFactory();
    private LobbySignFactory lsFac = new LobbySignFactory();
    private RollbackAgentFactory rollbackFac = new RollbackAgentFactory();

    @Override
    public IMinigameFactory getMinigameFactory() {
        return minigameFac;
    }

    @Override
    public IArenaFactory getArenaFactory() {
        return arenaFac;
    }

    @Override
    public IRoundFactory getRoundFactory() {
        return roundFac;
    }

    @Override
    public ILobbySignFactory getLobbySignFactory() {
        return lsFac;
    }

    @Override
    public IRollbackAgentFactory getRollbackAgentFactory() {
        return rollbackFac;
    }

}
