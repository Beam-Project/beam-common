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

/**
 * A message contains all necessary information to transport the content to the
 * targeted participant.
 */
public class Message {

    String version;
    Participant participant;
    byte[] content;

    /**
     * Sets the version.
     *
     * @param version
     * @throws IllegalArgumentException If the argument is null.
     */
    public void setVersion(String version) {
        if (version == null) {
            throw new IllegalArgumentException("The argument may not be null.");
        }

        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Sets the {@link Participant} of this {@link Message}. This is the
     * <i>recipient</i> (the next server or at the end the target client).
     *
     * @param participant
     * @throws IllegalArgumentException If the argument is null.
     */
    public void setParticipant(Participant participant) {
        if (participant == null) {
            throw new IllegalArgumentException("The argument may not be null.");
        }

        this.participant = participant;
    }

    public Participant getParticipant() {
        return participant;
    }

    /**
     * Sets the content as reference. The array is NOT copied.
     *
     * @param content
     * @throws IllegalArgumentException If the argument is null.
     */
    public void setContent(byte[] content) {
        if (content == null) {
            throw new IllegalArgumentException("The argument may not be null.");
        }

        this.content = content;
    }

    public byte[] getContent() {
        return content;
    }
}
