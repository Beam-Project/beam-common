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
 * targeted recipient.
 */
public class Message {

    public final static String VERSION = "1.0";
    String version;
    Participant recipient;
    Map<String, byte[]> content = new HashMap<>();

    /**
     * Sets the version.
     *
     * @param version
     * @throws IllegalArgumentException If the argument is null.
     */
    public void setVersion(String version) {
        Exceptions.verifyArgumentsNotNull(version);

        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Sets the recipient of this {@link Message}. This is the the next server
     * or at the end the user.
     *
     * @param recipient
     * @throws IllegalArgumentException If the argument is null.
     */
    public void setRecipient(Participant recipient) {
        Exceptions.verifyArgumentsNotNull(recipient);

        this.recipient = recipient;
    }

    public Participant getRecipient() {
        return recipient;
    }

    /**
     * Puts the content as reference to the internally used {@link Map}. The
     * array is NOT copied.
     * <p>
     * Content can be overwritten using equal {@code key}s.
     *
     * @param key The key of the field.
     * @param content The content bytes.
     * @throws IllegalArgumentException If at least one argument is null.
     */
    public void putContent(MessageField key, byte[] content) {
        Exceptions.verifyArgumentsNotNull(key, content);

        this.content.put(key.toString(), content);
    }

    /**
     * Returns the {@link Map} that contains the content.
     *
     * @return The original map.
     */
    public Map<String, byte[]> getContent() {
        return content;
    }

    /**
     * Reads the key from the content, if it was added.
     *
     * @param key The name of the content field.
     * @return The content.
     * @throws MessageContentException If the key could not be found.
     * @throws IllegalArgumentException If the argument is null.
     */
    public byte[] getContent(MessageField key) {
        Exceptions.verifyArgumentsNotNull(key);

        return content.get(key.toString());
    }

    /**
     * Tells if a given key is found in the content block.
     *
     * @param key The name of the content field.
     * @return True, if the field exists, otherwise false.
     * @throws IllegalArgumentException If the argument is null.
     */
    public boolean containsContent(MessageField key) {
        Exceptions.verifyArgumentsNotNull(key);

        return content.containsKey(key.toString());
    }
}
