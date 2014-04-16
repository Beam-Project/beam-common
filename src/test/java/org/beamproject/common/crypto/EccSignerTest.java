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

import java.security.KeyPair;
import java.security.Security;
import java.util.HashSet;
import org.beamproject.common.util.Base64;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;

public class EccSignerTest {

    private EccSigner signer;
    private KeyPair keyPair;
    private byte[] data = "hello world this is to sign".getBytes();
    private byte[] signature;

    @Before
    public void setUp() {
        signer = new EccSigner();
        keyPair = EccKeyPairGenerator.generate();
    }

    @Test
    public void testConstructorOnBouncyCastleIntegration() {
        Security.removeProvider(BouncyCastleIntegrator.PROVIDER_NAME);
        signer = new EccSigner();
        assertNotNull(Security.getProvider(BouncyCastleIntegrator.PROVIDER_NAME));
    }

    @Test
    public void testConstructorOnCreatingSignature() {
        signer = new EccSigner();
        assertNotNull(signer.signature);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignOnNulls() {
        signer.sign(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignOnNullData() {
        signer.sign(null, keyPair.getPrivate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignOnNullPrivateKey() {
        signer.sign(data, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyOnNulls() {
        signer.verify(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyOnNullData() {
        signer.verify(null, new byte[]{1, 2, 3}, keyPair.getPublic());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyOnNullDataSignature() {
        signer.verify(new byte[]{1, 2, 3}, null, keyPair.getPublic());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyOnNullPublicKey() {
        signer.verify(new byte[]{1, 2, 3}, new byte[]{1, 2, 3}, null);
    }

    @Test
    public void testSignAndVerify() {
        int minimalSignatureLengthInBytes = 100;
        signature = signer.sign(data, keyPair.getPrivate());
        assertNotNull(signature);
        assertTrue(signature.length >= minimalSignatureLengthInBytes);
        assertTrue(signer.verify(data, signature, keyPair.getPublic()));
    }

    @Test
    public void testSignOnRandomness() {
        int numberOfTries = 500;
        HashSet<String> signatures = new HashSet<>();

        for (int i = 0; i < numberOfTries; i++) {
            signature = signer.sign(data, keyPair.getPrivate());
            String asBase64 = Base64.encode(signature);
            assertFalse(signatures.contains(asBase64));
            signatures.add(asBase64);
        }
    }

    @Test
    public void testSignAndVerifyOnManipulatedSignature() {
        signature = signer.sign(data, keyPair.getPrivate());
        signature[50] = (byte) 123;
        assertFalse(signer.verify(data, signature, keyPair.getPublic()));
    }

    @Test
    public void testSignAndVerifyOnManipulatedData() {
        signature = signer.sign(data, keyPair.getPrivate());
        data[10] = (byte) 123;
        assertFalse(signer.verify(data, signature, keyPair.getPublic()));
    }

    @Test
    public void testSignAndVerifyOnWrongPublicKey() {
        signature = signer.sign(data, keyPair.getPrivate());
        KeyPair differentKeyPair = EccKeyPairGenerator.generate();
        assertFalse(signer.verify(data, signature, differentKeyPair.getPublic()));
    }

    @Ignore // Only needed to measure performance.
    @Test
    public void testSignAndVerifyBenchmark() {
        data = "046dbcc55dfb5e4ebf49eef9a04ef545e077b36e3960236c0c8fea95d90c42b0046dbcc55dfb5e4ebf49eef9a04ef545e077b36e3960236c0c8fea95d90c42b0046dbcc55dfb5e4ebf49eef9a04ef545e077b36e3960236c0c8fea95d90c42b0046dbcc55dfb5e4ebf49eef9a04ef545e077b36e3960236c0c8fea95d90c42b0046dbcc55dfb5e4ebf49eef9a04ef545e077b36e3960236c0c8fea95d90c42b0046dbcc55dfb5e4ebf49eef9a04ef545e077b36e3960236c0c8fea95d90c42b0046dbcc55dfb5e4ebf49eef9a04ef545e077b36e3960236c0c8fea95d90c42b0046dbcc55dfb5e4ebf49eef9a04ef545e077b36e3960236c0c8fea95d90c42b0046dbcc55dfb5e4ebf49eef9a04ef545e077b36e3960236c0c8fea95d90c42b0046dbcc55dfb5e4ebf49eef9a04ef545e077b36e3960236c0c8fea95d90c42b0046dbcc55dfb5e4ebf49eef9a04ef545e077b36e3960236c0c8fea95d90c42b0046dbcc55dfb5e4ebf49eef9a04ef545e077b36e3960236c0c8fea95d90c42b0046dbcc55dfb5e4ebf49eef9a04ef545e077b36e3960236c0c8fea95d90c42b0".getBytes();

        long startTime = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            signature = signer.sign(data, keyPair.getPrivate());
            signer.verify(data, signature, keyPair.getPublic());
        }

        long endTime = System.nanoTime();

        System.out.println("time: " + (endTime - startTime) / 10e8 + " s");
    }

}
