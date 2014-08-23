/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-common.
 *
 * beam-common is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-common is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.common.util;

import java.awt.image.BufferedImage;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class QrCodeTest {

    private final int DIMENSION_IN_PX = 150;

    @Test(expected = IllegalArgumentException.class)
    public void testEncodeOnNull() {
        QrCode.encode(null, DIMENSION_IN_PX);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncodeOnEmptyString() {
        QrCode.encode("", DIMENSION_IN_PX);
    }

    @Test
    public void testEncodeOnNegativeOrZeroDimension() {
        for (int i = -100; i < 0; i++) {
            try {
                QrCode.encode("data", i);
                fail("An exception shuold have been thrwon.");
            } catch (IllegalArgumentException ex) {
            }
        }

        try {
            QrCode.encode("data", Integer.MIN_VALUE);
            fail("An exception shuold have been thrwon.");
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    public void testEncode() {
        String data = "hello";
        BufferedImage code = QrCode.encode(data, DIMENSION_IN_PX);
        assertTrue(code.toString().contains("transparency = 1 transIndex   = -1 has alpha = false isAlphaPre = false BytePackedRaster: width = 150 height = 150 #channels 1 xOff = 0 yOff = 0"));
    }

}
