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
package net.caseif.flint.steel.util.helper.rollback.serialization;

import net.caseif.flint.metadata.persist.Serializer;

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
