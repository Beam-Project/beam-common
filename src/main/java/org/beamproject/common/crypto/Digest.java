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
package org.beamproject.common.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import org.beamproject.common.util.Exceptions;

/**
 * This class provides needed Digests.
 */
public class Digest {

    public final static String SHA256_DIGEST_NAME = "SHA-256";

    /**
     * Digests the given {@code payload} using the {@code SHA-256} hash
     * algorithm provided by Bouncy Castle.
     *
     * @param payload The payload to digest. This may not be null.
     * @return The calculated digest.
     * @throws IllegalArgumentException If the argument is null.
     * @throws IllegalStateException If the digest could not be set up.
     */
    public static byte[] digestWithSha256(byte[] payload) {
        Exceptions.verifyArgumentNotNull(payload);
        
        BouncyCastleIntegrator.initBouncyCastleProvider();

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(SHA256_DIGEST_NAME, BouncyCastleIntegrator.PROVIDER_NAME);
            return messageDigest.digest(payload);
        } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
            throw new IllegalStateException("The digest could not be computed: " + ex.getMessage());
        }
    }
    
}
