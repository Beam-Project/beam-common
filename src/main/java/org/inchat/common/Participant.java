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
import org.inchat.common.util.Exceptions;

/**
 * Represents a instance in the network that does something with messages. For
 * example, a Participant could be a user or a server.
 */
public class Participant {

    public final static int ID_LENGTH_IN_BYTES = 32;
    byte[] id;
    KeyPair keyPair;

    /**
     * Creates a new {@link Participant}, initialized with it's id.
     *
     * @param id May not be null.
     * @throws IllegalArgumentException If the argument is null.
     */
    public Participant(byte[] id) {
        Exceptions.verifyArgumentNotNull(id);

        if (id.length != ID_LENGTH_IN_BYTES) {
            throw new IllegalArgumentException("The agument has to be exactly " + ID_LENGTH_IN_BYTES + " bytes in length.");
        }

        this.id = id;
    }

    public byte[] getId() {
        return id;
    }

    /**
     * Sets the {@link KeyPair} of this {@link Participant}. If this is a remote
     * {@link Participant}, only the pulic key might be set in the key pair.
     *
     * @param keyPair The key pair.
     * @throws IllegalArgumentException If the argument is null.
     */
    public void setKeyPair(KeyPair keyPair) {
        Exceptions.verifyArgumentNotNull(keyPair);
        
        this.keyPair = keyPair;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }
}
