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

import java.security.KeyPair;
import org.inchat.common.crypto.Digest;
import org.inchat.common.util.Exceptions;

/**
 * Represents a instance in the network that does something with messages. For
 * example, a {@link Participant} could be a user or a server.
 */
public class Participant {

    KeyPair keyPair;

    /**
     * Creates a new {@link Participant}, initialized with its id.
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

    /**
     * Returns the id of this {@link Participant}. Actually, it's only the
     * SHA-256 digest of the public key.
     *
     * @return The id of this {@link Participant}.
     */
    public byte[] getId() {
        return Digest.digestWithSha256(keyPair.getPublic().getEncoded());
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }
}
