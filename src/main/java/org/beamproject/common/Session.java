/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-server.
 *
 * beam-server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.common;

import org.beamproject.common.crypto.Handshake;
import org.beamproject.common.crypto.HandshakeChallenger;
import org.beamproject.common.crypto.HandshakeResponder;
import org.beamproject.common.util.Exceptions;
import org.beamproject.common.util.Timestamps;
import org.joda.time.DateTime;

/**
 * Stores a currently valid session between this and an other
 * {@link Participant}.
 *
 * @see Handshake
 * @see HandshakeChallenger
 * @see HandshakeResponder
 */
public class Session {

    Participant remoteParticipant;
    byte[] key;
    DateTime latestInteractionTime;

    /**
     * Creates a new instance of {@link Session}, initialized with the given
     * remoteParticipant and the already agreed session key.
     * <p>
     * To create the session key, the remoteParticipant and this participant
     * have to complete a {@link Handshake}.
     * <p>
     * This instance also stores a {@link DateTime} with the time of creation.
     * It will be updated every time the session is refreshed using a HEARTBEAT
     * message.
     *
     * @param remoteParticipant The remoteParticipant who holds this session
     * key.
     * @param key The key of this session.
     * @throws IllegalArgumentException If at least one argument is null.
     */
    public Session(Participant remoteParticipant, byte[] key) {
        Exceptions.verifyArgumentsNotNull(remoteParticipant, key);

        this.remoteParticipant = remoteParticipant;
        this.key = key;
        latestInteractionTime = Timestamps.getUtcTimestamp();
    }

    /**
     * @return The remoteParticipant who holds this session.
     */
    public Participant getRemoteParticipant() {
        return remoteParticipant;
    }

    /**
     * @return The key of this session.
     */
    public byte[] getKey() {
        return key;
    }

    /**
     * Sets the latest interaction timestamp of this {@link Session}.
     *
     * @param latestInteractionTime The latest interaction timestamp.
     * @throws IllegalArgumentException If the argument is null.
     */
    public void setLastestInteractionTime(DateTime latestInteractionTime) {
        Exceptions.verifyArgumentsNotNull(latestInteractionTime);

        this.latestInteractionTime = latestInteractionTime;
    }

    /**
     * @return The latest interaction timestamp of this {@link Session}.
     */
    public DateTime getLatestInteractionTime() {
        return latestInteractionTime;
    }

    /**
     * Invalidates this session. The key byte array is set to zeros. The
     * reference to the remote participant is set to null.
     */
    public void invalidateSession() {
        for (int i = 0; i < key.length; i++) {
            key[i] = 0;
        }

        remoteParticipant = null;
    }
}
