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

package net.caseif.flint.steel.util.agent.rollback.serialization;

import net.caseif.flint.serialization.Serializer;

import org.bukkit.util.EulerAngle;

/**
 * {@link Serializer} for {@link EulerAngle} objects.
 *
 * @author Max Roncacé
 * @since 1.0
 */
public class EulerAngleSerializer implements Serializer<EulerAngle> {

    private static EulerAngleSerializer instance;

    private EulerAngleSerializer() {
    }

    public static EulerAngleSerializer getInstance() {
        return instance != null ? instance : (instance = new EulerAngleSerializer());
    }



    @Override
    public String serialize(EulerAngle angle) {
        return "(" + angle.getX() + "," + angle.getY() + "," + angle.getZ() + ")";
    }

    @Override
    public EulerAngle deserialize(String serial) throws IllegalArgumentException {
        if (serial.startsWith("(") && serial.endsWith(")")) {
            String[] arr = serial.substring(1, serial.length() - 1).split(",");
            if (arr.length == 3) {
                try {
                    double x = Double.parseDouble(arr[0]);
                    double y = Double.parseDouble(arr[0]);
                    double z = Double.parseDouble(arr[0]);
                    return new EulerAngle(x, y, z);
                } catch (NumberFormatException ignored) { } // continue to the IllegalArgumentException at the bottom
            }
        }
        throw new IllegalArgumentException("Invalid serial for EulerAngle");
    }

}
