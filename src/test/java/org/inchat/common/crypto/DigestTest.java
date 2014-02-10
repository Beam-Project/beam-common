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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class DigestTest {

    private final static String REFERENCE_DIGEST_PROVIDER = "SUN";
    private final static String HELLO_LOWER_CASE_AS_SHA256 = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";
    private byte[] bytePayload;
    private byte[] output;

    @Before
    public void setUp() {
        bytePayload = "hello".getBytes();
    }

    @Test
    public void testInstantiation() {
        Digest digest = new Digest();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDigestWithSha256OnNull() {
        bytePayload = null;
        output = Digest.digestWithSha256(bytePayload);
    }

    @Test
    public void testDigestWithSha256OnBouncyCastleSetUp() {
        Security.removeProvider(BouncyCastleIntegrator.PROVIDER_NAME);

        output = Digest.digestWithSha256(bytePayload);

        assertNotNull(Security.getProvider(BouncyCastleIntegrator.PROVIDER_NAME));
    }

    @Test
    public void testDigestWithSha256OnPrecomputedDigest() {
        output = Digest.digestWithSha256(bytePayload);
        assertArrayEquals(Hex.decode(HELLO_LOWER_CASE_AS_SHA256), output);
    }

    @Test
    public void testDigestWithSha256() throws NoSuchAlgorithmException, NoSuchProviderException {
        String textAddition = "This Is A Very Long Text To Compute...";
        String payloadText = "";
        int rounds = 100;
        MessageDigest reference = MessageDigest.getInstance(Digest.SHA256_DIGEST_NAME, REFERENCE_DIGEST_PROVIDER);

        for (int i = 0; i < rounds; i++) {
            bytePayload = payloadText.getBytes();

            output = Digest.digestWithSha256(bytePayload);
            assertArrayEquals(reference.digest(bytePayload), output);

            payloadText += textAddition;
        }
    }

}
