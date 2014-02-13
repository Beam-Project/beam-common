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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.inchat.common.util.Exceptions;

/**
 * This {@link KeyPairStore} allows to store {@link  KeyPair}s encrypted on the
 * local file system. It uses PBKDF2 to strengthen the user password.<p>
 * This code is inspired by Jerry Orrs article "Secure Password Storage - Lots
 * of don'ts, a few dos, and a concrete Java SE example". See:
 * http://blog.jerryorr.com/2012/05/secure-password-storage-lots-of-donts.html.
 * He licensed his original code under Public Domain.
 */
public class KeyPairStore {

    public final static String PUBILC_KEY_FILE_EXTENSION = ".public";
    public final static String PRIVATE_KEY_FILE_EXTENSION = ".private";
    public final static String SALT_FILE_EXTENSION = ".salt";
    
    public final static String PBKDF_ALGORITHM_NAME = "PBKDF2WithHmacSHA256";
    public final static int NUMBER_OF_ITERATIONS = 20000;
    public final static String SALT_RANDOM_ALGORITHM_NAME = "SHA1PRNG";
    public final static int SALT_LENGTH_IN_BYTES = 8;
    public final static String SYMMETRIC_ALGORITHM_NAME = "AES";
    public final static int KEY_LENGTH_IN_BITS = 256;
    public final static String KEY_ALGORITHM_NAME = "EC";
    
    String password;
    String filename;
    KeyPair keyPair;
    Key aesKey;
    byte[] salt;
    byte[] encryptedPrivateKey;
    byte[] encryptedPublicKey;

    /**
     * Configures the {@link KeyPairStore} with a {@code password} and a
     * {@code filename}. The password will be strengthened using PBKDF2 to
     * generate a strong key for the AES cipher which encrypts the public and
     * private key.
     *
     * @param password The password, set by the user. This may not be empty.
     * @param filename The filename of the store files. This may not be null.
     * @throws IllegalArgumentException If at least one argument is empty.
     */
    public KeyPairStore(String password, String filename) {
        Exceptions.verifyArgumentNotEmpty(password);
        Exceptions.verifyArgumentNotEmpty(filename);

        this.password = password;
        this.filename = filename;

        BouncyCastleIntegrator.initBouncyCastleProvider();
    }

    /**
     * Encrypts and stores the given {@code keyPair} to the configured filename.
     * There will be 3 files (ending with {@code .public}, {@code .private}
     * respectively {@code .salt}).
     *
     * @param keyPair The key pair to store. This may not be null.
     * @throws IllegalArgumentException If the argument is null.
     * @throws IllegalStateException If anything during encryption or file
     * writing goes wrong.
     */
    public void storeKeys(KeyPair keyPair) {
        Exceptions.verifyArgumentNotNull(keyPair);

        this.keyPair = keyPair;

        generateSalt();
        strengthenPasswordToAesKey();
        encryptKeys();

        writeFiles();
    }

    private void generateSalt() {
        try {
            SecureRandom random = SecureRandom.getInstance(SALT_RANDOM_ALGORITHM_NAME);
            salt = new byte[SALT_LENGTH_IN_BYTES];
            random.nextBytes(salt);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Could not generate salt for the store key: " + ex.getMessage());
        }
    }

    private void strengthenPasswordToAesKey() {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, NUMBER_OF_ITERATIONS, KEY_LENGTH_IN_BITS);
            SecretKey secretKey = keyFactory.generateSecret(keySpec);
            aesKey = new SecretKeySpec(secretKey.getEncoded(), SYMMETRIC_ALGORITHM_NAME);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("Could not strengthen the store password with PBKDF2: " + ex.getMessage());
        }
    }

    private void encryptKeys() {
        AesCbcCipher cipher = new AesCbcCipher(aesKey.getEncoded());

        encryptedPrivateKey = cipher.encrypt(keyPair.getPrivate().getEncoded());
        encryptedPublicKey = cipher.encrypt(keyPair.getPublic().getEncoded());
    }

    private void writeFiles() {
        writeFile(filename + PRIVATE_KEY_FILE_EXTENSION, encryptedPrivateKey);
        writeFile(filename + PUBILC_KEY_FILE_EXTENSION, encryptedPublicKey);
        writeFile(filename + SALT_FILE_EXTENSION, salt);
    }

    private void writeFile(String filename, byte[] content) {
        File file = new File(filename);

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            outputStream.write(content);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not write the file: " + ex.getMessage());
        }
    }

    /**
     * Reads and decrypts the stored key pair.
     *
     * @return The decrypted key pair.
     * @throws IllegalStateException If anything during file reading or
     * decryption or goes wrong.
     */
    public KeyPair readKeys() {
        readFiles();

        strengthenPasswordToAesKey();
        decryptKeys();

        return keyPair;
    }

    private void readFiles() {
        encryptedPrivateKey = readFile(filename + PRIVATE_KEY_FILE_EXTENSION);
        encryptedPublicKey = readFile(filename + PUBILC_KEY_FILE_EXTENSION);
        salt = readFile(filename + SALT_FILE_EXTENSION);
    }

    private void decryptKeys() {
        AesCbcCipher cipher = new AesCbcCipher(aesKey.getEncoded());
        byte[] decryptedPrivateKey = cipher.decrypt(encryptedPrivateKey);
        byte[] decryptedPublicKey = cipher.decrypt(encryptedPublicKey);

        try {
            KeyFactory fact = KeyFactory.getInstance(KEY_ALGORITHM_NAME, BouncyCastleIntegrator.PROVIDER_NAME);
            PrivateKey privateKey = fact.generatePrivate(new PKCS8EncodedKeySpec(decryptedPrivateKey));
            PublicKey publicKey = fact.generatePublic(new X509EncodedKeySpec(decryptedPublicKey));

            keyPair = new KeyPair(publicKey, privateKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException ex) {
            throw new IllegalStateException("Could not create KeyPair from the decrypted key material: " + ex.getMessage());
        }
    }

    private byte[] readFile(String filename) {
        File file = new File(filename);

        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] fileContent = new byte[(int) file.length()];
            inputStream.read(fileContent);
            return fileContent;
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the file '" + filename + "' correctly: " + e.getMessage());
        }
    }

}
