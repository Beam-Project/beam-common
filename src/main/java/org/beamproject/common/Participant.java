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
package org.beamproject.common;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Objects;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.beamproject.common.util.Base58;
import org.beamproject.common.util.Exceptions;

/**
 * Represents a instance in the network that does something with messages. For
 * example, a {@link Participant} could be a user or a server.
 * 
 * @see Server
 * @see User
 */
public class Participant implements Serializable {

    private static final long serialVersionUID = 1L;
    KeyPair keyPair;

    /**
     * Creates a new {@link Participant}, initialized with its key pair.
     *
     * @param keyPair The key pair of this {@link Participant}. If both private
     * and public key are known, initialize both. Otherwise (if its a remote
     * participant), only the public key is enough. This may not be null.
     * @throws IllegalArgumentException If the argument is null.
     */
    public Participant(KeyPair keyPair) {
        Exceptions.verifyArgumentsNotNull(keyPair);

        this.keyPair = keyPair;
    }

    /**
     * @return The public key.
     */
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    /**
     * @return The bytes of the {@link PublicKey}, X509 encoded.
     */
    public byte[] getPublicKeyAsBytes() {
        return getPublicKey().getEncoded();
    }

    /**
     * Gets the public key of this {@link Participant}. The bytes of the public
     * key, encoded as X509, are {@link Base58} encoded to a String.
     *
     * @return The public key, {@link Base58} encoded.
     */
    public String getPublicKeyAsBase58() {
        return Base58.encode(getPublicKeyAsBytes());
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    /**
     * @return The bytes of the {@link PublicKey}, PKCS8 encoded.
     */
    public byte[] getPrivateKeyAsBytes() {
        return getPrivateKey().getEncoded();
    }

    /**
     * Gets the private key of this {@link Participant}. The bytes of the
     * private key, encoded as PKCS8, are {@link Base58} encoded to a String.
     *
     * @return The private key, {@link Base58} encoded.
     */
    public String getPrivateKeyAsBase58() {
        return Base58.encode(getPrivateKeyAsBytes());
    }

    /**
     * @return The private key of this {@link Participant}, if available.
     * {@code null} otherwise.
     */
    public KeyPair getKeyPair() {
        return keyPair;
    }

    /**
     * Generates a new {@link Participant}, initialized with a new
     * {@link KeyPair}.
     *
     * @return The new {@link Participant}.
     */
    public static Participant generate() {
        return new Participant(EccKeyPairGenerator.generate());
    }

    /**
     * Compares this {@link Participant} to the other object. The objects are
     * only equals if both are of the same type, both have the same keys (both
     * {@link PublicKey}s and/or both {@link PrivateKey}s) AND if they are
     * pairwise the same. Two {@link Participant}s are also the same, when both
     * keyPairs are null.
     *
     * @param other Another object to compare with this one.
     * @return true, if the key pairs are equals, otherwise false.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other.getClass() != this.getClass()) {
            return false;
        }

        Participant otherParticipant = (Participant) other;
        KeyPair otherKeyPair = otherParticipant.getKeyPair();

        if (keyPair == null || otherKeyPair == null) {
            return keyPair == null && otherKeyPair == null;
        }

        if (keyPair.getPublic() != null
                && keyPair.getPrivate() == null
                && otherKeyPair.getPublic() != null
                && otherKeyPair.getPrivate() == null) {
            return Arrays.equals(keyPair.getPublic().getEncoded(), otherKeyPair.getPublic().getEncoded());
        }

        if (keyPair.getPublic() == null
                && keyPair.getPrivate() != null
                && otherKeyPair.getPublic() == null
                && otherKeyPair.getPrivate() != null) {
            return Arrays.equals(keyPair.getPrivate().getEncoded(), otherKeyPair.getPrivate().getEncoded());
        }

        if (keyPair.getPublic() != null
                && keyPair.getPrivate() != null
                && otherKeyPair.getPublic() != null
                && otherKeyPair.getPrivate() != null) {
            return Arrays.equals(keyPair.getPublic().getEncoded(), otherKeyPair.getPublic().getEncoded())
                    && Arrays.equals(keyPair.getPrivate().getEncoded(), otherKeyPair.getPrivate().getEncoded());
        }

        return false;
    }

    /**
     * Calculates the hash code for this {@link Participant} using the hash code
     * of the keyPair.
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.keyPair);
        return hash;
    }

}
