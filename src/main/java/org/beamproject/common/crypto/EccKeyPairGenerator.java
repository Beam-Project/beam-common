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

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import static org.beamproject.common.crypto.BouncyCastleIntegrator.PROVIDER_NAME;
import static org.beamproject.common.crypto.BouncyCastleIntegrator.initBouncyCastleProvider;
import org.beamproject.common.util.Exceptions;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

/**
 * This Generator generates {@link KeyPair}s for ECIES encryption and
 * decryption.
 */
public abstract class EccKeyPairGenerator {

    public final static String ALGORITHM_NAME = "EC";
    public final static String SEC_CURVE_NAME = "secp384r1";

    /**
     * Generates a new {@link KeyPair} for the ECC curve {@code secp384r1}.
     *
     * @return The key pair.
     */
    public static KeyPair generate() {
        initBouncyCastleProvider();

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_NAME, PROVIDER_NAME);
            ECNamedCurveParameterSpec curveParameterSpec = ECNamedCurveTable.getParameterSpec(SEC_CURVE_NAME);
            keyPairGenerator.initialize(curveParameterSpec, new SecureRandom());

            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException ex) {
            throw new IllegalStateException("Could not generate a KeyPair: " + ex.getMessage());
        }
    }

    /**
     * Restores a {@link KeyPair} with the given {@link PublicKey} bytes. The
     * {@link PrivateKey} will not be set.
     *
     * @param publicKeyBytes The public key bytes, X509 encoded.
     * @return The key pair.
     * @throws IllegalArgumentException If the argument is null.
     * @throws IllegalStateException If the {@link PublicKey} could not be
     * generated with the given bytes.
     */
    public static KeyPair fromPublicKey(byte[] publicKeyBytes) {
        Exceptions.verifyArgumentsNotNull(publicKeyBytes);
        initBouncyCastleProvider();

        try {
            KeyFactory fact = KeyFactory.getInstance(ALGORITHM_NAME, PROVIDER_NAME);
            PublicKey publicKey = fact.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            return new KeyPair(publicKey, null);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException ex) {
            throw new IllegalStateException("Could not create KeyPair from the decrypted key material: " + ex.getMessage());
        }
    }

    /**
     * Restores a {@link KeyPair} with the given {@link PublicKey} and
     * {@link PrivateKey} bytes.
     *
     * @param publicKeyBytes The public key bytes, X509 encoded.
     * @param privateKeyBytes The private key bytes, PKCS8 encoded.
     * @return The key pair.
     * @throws IllegalArgumentException If at least one argument is null.
     * @throws IllegalStateException If one of the keys could not be generated
     * with the given bytes.
     */
    public static KeyPair fromBothKeys(byte[] publicKeyBytes, byte[] privateKeyBytes) {
        Exceptions.verifyArgumentsNotNull(publicKeyBytes, privateKeyBytes);
        initBouncyCastleProvider();

        try {
            KeyFactory fact = KeyFactory.getInstance(ALGORITHM_NAME, PROVIDER_NAME);
            PublicKey publicKey = fact.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            PrivateKey privateKey = fact.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

            return new KeyPair(publicKey, privateKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException ex) {
            throw new IllegalStateException("Could not create KeyPair from the decrypted key material: " + ex.getMessage());
        }
    }

}
