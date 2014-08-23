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

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import static org.beamproject.common.crypto.BouncyCastleIntegrator.PROVIDER_NAME;
import static org.beamproject.common.crypto.BouncyCastleIntegrator.initBouncyCastleProvider;
import org.beamproject.common.util.Exceptions;

/**
 * The {@link PasswordCryptor} allows to encrypt/decrypt byte arrays. It uses
 * PBKDF2 to strengthen the password.<p>
 * This code is inspired by Jerry Orrs article "Secure Password Storage - Lots
 * of don'ts, a few dos, and a concrete Java SE example". See:
 * http://blog.jerryorr.com/2012/05/secure-password-storage-lots-of-donts.html.
 */
public class PasswordCryptor {

    public final static String PBKDF_ALGORITHM_NAME = "PBKDF2WithHmacSHA1";
    public final static int NUMBER_OF_ITERATIONS = 25000;
    public final static String SALT_RANDOM_ALGORITHM_NAME = "SHA1PRNG";
    public final static int SALT_LENGTH_IN_BYTES = 16;
    public final static String SYMMETRIC_ALGORITHM_NAME = "AES";
    public final static int KEY_LENGTH_IN_BITS = 256;
    char[] password;
    byte[] salt;
    Key aesKey;
    AesCipher cipher;

    /**
     * Creates a new instance of {@link PasswordCryptor}, initialized with the
     * given password and salt. The internally used AES key is derived by the
     * password and the salt using PBKDF2.
     *
     * @param password The password to use (provided by the user).
     * @param salt The salt to use, required by PBKDF2.
     * @throws IllegalStateException If PBKDF2 cannot be used to strengthen the
     * password.
     */
    public PasswordCryptor(char[] password, byte[] salt) {
        Exceptions.verifyArgumentsNotNull(password, salt);
        this.password = password;
        this.salt = salt;

        initBouncyCastleProvider();
        strengthenPasswordToAesKey();
        createCipher();
    }

    private void strengthenPasswordToAesKey() {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF_ALGORITHM_NAME, PROVIDER_NAME);
            KeySpec keySpec = new PBEKeySpec(password, salt, NUMBER_OF_ITERATIONS, KEY_LENGTH_IN_BITS);
            SecretKey secretKey = keyFactory.generateSecret(keySpec);
            aesKey = new SecretKeySpec(secretKey.getEncoded(), SYMMETRIC_ALGORITHM_NAME);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException ex) {
            throw new IllegalStateException("Could not strengthen the store password with PBKDF2: " + ex.getMessage());
        }
    }

    private void createCipher() {
        cipher = new AesCipher(aesKey.getEncoded());
    }

    /**
     * Encrypts the given bytes using the configured password and the salt.
     *
     * @param plaintext The bytes to encrypt.
     * @return The encrypted bytes.
     * @throws IllegalArgumentException If the argument is null.
     */
    public byte[] encrypt(byte[] plaintext) {
        Exceptions.verifyArgumentsNotNull((Object) plaintext);

        return cipher.encrypt(plaintext);
    }

    /**
     * Decrypts the given ciphertext with the configured password and the salt.
     *
     * @param ciphertext The bytes to decrypt.
     * @return The decrypted bytes.
     * @throws IllegalArgumentException If the argument is null.
     */
    public byte[] decrypt(byte[] ciphertext) {
        Exceptions.verifyArgumentsNotNull((Object) ciphertext);

        return cipher.decrypt(ciphertext);
    }

    /**
     * Changes the password of this {@link PasswordCryptor} instance. Therefore,
     * the old password character array is being filled with zeros, and a
     * reference to the given {@code newPassword} array is stored and from now
     * on used for encryption/decryption. Hence, the password is being
     * strengthened to an AES key and a new cipher is created.
     *
     * @param newPassword The new password to use.
     * @throws IllegalArgumentException If the argument is null.
     */
    public void changePassword(char[] newPassword) {
        Exceptions.verifyArgumentsNotNull((Object) newPassword);

        fillOldPasswordWithZeros();

        password = newPassword;

        strengthenPasswordToAesKey();
        createCipher();
    }

    private void fillOldPasswordWithZeros() {
        Arrays.fill(password, (char) 0);
    }

    /**
     * Generated salt that can be used with the encrypt method.
     *
     * @return the salt byte array.
     * @throws IllegalStateException If the salt cannot be generated or PBKDF2
     * cannot be used to strengthen the password.
     */
    public static byte[] generateSalt() {
        try {
            SecureRandom random = SecureRandom.getInstance(SALT_RANDOM_ALGORITHM_NAME);
            byte[] salt = new byte[SALT_LENGTH_IN_BYTES];
            random.nextBytes(salt);
            return salt;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Could not generate salt for the store key: " + ex.getMessage());
        }
    }

}
