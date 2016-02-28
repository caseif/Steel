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
package net.caseif.flint.steel.util;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

/**
 * Static utility class for determining server feature support.
 *
 * @author Max Roncacé
 */
public class Support {

    public static final boolean ARMOR_STAND;
    public static final boolean BANNER;

    static {
        boolean temp = false;
        try {
            EntityType.valueOf("ARMOR_STAND");
            temp = true;
        } catch (IllegalArgumentException ignored) {
        }
        ARMOR_STAND = temp;

        temp = false;
        try {
            Material.valueOf("BANNER");
            temp = true;
        } catch (IllegalArgumentException ignored) {
        }
        BANNER = temp;
    }

}
