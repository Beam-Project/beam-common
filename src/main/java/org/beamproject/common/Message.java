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

import java.util.HashMap;
import java.util.Map;
import org.beamproject.common.util.Exceptions;

/**
 * A message contains all necessary information to transport the content to the
 * targeted participant.
 */
public class Message {

    public final static String DEFAUTL_VERSION = "1.0";
    String version;
    Participant participant;
    Map<String, byte[]> content = new HashMap<>();

    /**
     * Sets the version.
     *
     * @param version
     * @throws IllegalArgumentException If the argument is null.
     */
    public void setVersion(String version) {
        Exceptions.verifyArgumentNotNull(version);

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
        Exceptions.verifyArgumentNotNull(participant);

        this.participant = participant;
    }

    public Participant getParticipant() {
        return participant;
    }

    /**
     * Appends the content as reference to the internally used {@link Map}. The
     * array is NOT copied.
     *
     * @param key The key of the field.
     * @param content The content bytes.
     * @throws IllegalArgumentException If at least one argument is null.
     */
    public void appendContent(MessageField key, byte[] content) {
        Exceptions.verifyArgumentsNotNull(key, content);

        this.content.put(key.toString(), content);
    }

    public Map<String, byte[]> getContent() {
        return content;
    }

    public byte[] getContent(MessageField key) {
        Exceptions.verifyArgumentNotNull(key);

        return getContent().get(key.toString());
    }
}
