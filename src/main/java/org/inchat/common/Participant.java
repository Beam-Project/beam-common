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
package org.inchat.common;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.xml.bind.DatatypeConverter;
import org.inchat.common.util.Exceptions;

/**
 * Represents a instance in the network that does something with messages. For
 * example, a {@link Participant} could be a user or a server.
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
        Exceptions.verifyArgumentNotNull(keyPair);

        this.keyPair = keyPair;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public byte[] getPublicKeyAsBytes() {
        return getPublicKey().getEncoded();
    }

    public String getPublicKeyAsHex() {
        return DatatypeConverter.printHexBinary(getPublicKeyAsBytes()).toLowerCase();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public byte[] getPrivateKeyAsBytes() {
        return getPrivateKey().getEncoded();
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

}
