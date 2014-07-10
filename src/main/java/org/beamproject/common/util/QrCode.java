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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

/**
 * This class can be used to encode Strings to QR codes.
 */
public class QrCode {

    /**
     * Encodes the given {@code data} to a square QR code of the with and height
     * of {@code dimensionInPx}.
     *
     * @param data The data to encode.
     * @param dimensionInPx The side length of the QR code.
     * @return The image.
     * @throws IllegalArgumentException If the data is null (it can be empty
     * though) or the dimension is negative.
     */
    public static BufferedImage encode(String data, int dimensionInPx) {
        if (data == null || dimensionInPx < 0) {
            throw new IllegalArgumentException("The data may not be null, the dimension may not be negative.");
        }

        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, dimensionInPx, dimensionInPx, hints);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException ex) {
            throw new ImageException("Could not encode text to a QR code: " + ex.getMessage());
        }
    }

}
