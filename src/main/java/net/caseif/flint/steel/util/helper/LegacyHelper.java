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

package net.caseif.flint.steel.util.helper;

import com.google.common.base.Preconditions;
import net.caseif.flint.common.util.agent.rollback.RollbackRecord;
import net.caseif.flint.steel.SteelCore;
import org.bukkit.block.Block;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LegacyHelper {

    private static boolean initialized;

    private static final boolean working;

    private static final Method method_Block_setData;

    static {
        Preconditions.checkState(SteelCore.isLegacy(),
                "Cannot use legacy helper class on non-legacy platform!");

        try {
            //noinspection JavaReflectionMemberAccess
            method_Block_setData = Block.class.getMethod("setData", byte.class);
        } catch (NoSuchMethodException e) {
            working = false;

            throw new RuntimeException("Failed to access Block#setData (is legacy detection broken?)");
        }

        working = true;
    }

    public LegacyHelper() {
        Preconditions.checkState(!initialized, "Cannot initialize singleton class LegacyHelper more than once.");

        initialized = true;
    }

    public void updateData(Block b, byte data) {
        Preconditions.checkState(SteelCore.isLegacy(),
                "Cannot use legacy helper class on non-legacy platform!");

        if (!working) {
            return;
        }

        try {
            method_Block_setData.invoke(b, data);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException("Failed to set block data (is legacy detection broken?)");
        }
    }

}
