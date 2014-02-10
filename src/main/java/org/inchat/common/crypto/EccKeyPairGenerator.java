/*
 * Copyright (C) 2013, 2014 inchat.org
 *
 * This file is part of inchat-common.
 *
 * inchat-common is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * inchat-common is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.inchat.common.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

/**
 * This Generator generates {@link KeyPair}s for ECIES encryption and
 * decryption.
 */
public class EccKeyPairGenerator {

    public final static String ALGORITHM_NAME = "EC";
    public final static String SEC_CURVE_NAME = "secp384r1";

    /**
     * Generates a new {@link KeyPair} for the ECC curve {@code secp384r1}.
     *
     * @return The key pair.
     */
    public static KeyPair generate() {
        BouncyCastleIntegrator.initBouncyCastleProvider();

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_NAME, BouncyCastleIntegrator.PROVIDER_NAME);
            ECNamedCurveParameterSpec curveParameterSpec = ECNamedCurveTable.getParameterSpec(SEC_CURVE_NAME);
            keyPairGenerator.initialize(curveParameterSpec, new SecureRandom());

            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException ex) {
            throw new IllegalStateException("Could not generate a KeyPair: " + ex.getMessage());
        }
    }

}
