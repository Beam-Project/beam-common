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
package org.beamproject.common.network;

import java.util.LinkedList;
import java.util.List;
import org.beamproject.common.Message;
import org.beamproject.common.util.Exceptions;

/**
 * A {@link Vertex} is a node of an beam <i>Transfer Graph</i>. It contains a
 * plaintext {@link Message} and a ciphertext. The latter is represented as byte
 * array, ready for sending out.<p>
 * Every {@link Vertex} can be used to traverse <i>up</i> and <i>down</i> in the
 * Transfer Graph.
 */
public class Vertex {

    Vertex parent;
    List<Vertex> children = new LinkedList<>();
    Message plaintext;
    byte[] ciphertext;

    /**
     * Creates a new {@link Vertex} with no content. Use the setter methods to
     * fill this instance.
     */
    public Vertex() {
    }

    /**
     * Creates a new {@link Vertex} using a {@link Message} as plaintext.
     *
     * @param plaintext May not be null.
     * @throws IllegalArgumentException If the argument is null.
     */
    public Vertex(Message plaintext) {
        Exceptions.verifyArgumentNotNull(plaintext);
        
        this.plaintext = plaintext;
    }

    /**
     * Tells whether or not this {@link Vertex} has children.
     *
     * @return true If there are children, false if not.
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Adds {@code child} to this {@link Vertex}. Before doing so, the child's
     * parent attribute is set to point to this instance, so that upward
     * traversing possible is.
     *
     * @param child The child to add.
     * @throws IllegalArgumentException If the argument is null.
     */
    public void addChild(Vertex child) {
        Exceptions.verifyArgumentNotNull(child);
        
        child.setParent(this);
        children.add(child);
    }

    /**
     * Returns all children. The {@link List} might be empty but it's never
     * {@code null}.
     *
     * @return The children of this {@link Vertex}.
     */
    public List<Vertex> getChildren() {
        return children;
    }

    void setParent(Vertex parent) {
        Exceptions.verifyArgumentNotNull(parent);
        
        this.parent = parent;
    }

    public Vertex getParent() {
        return parent;
    }

    /**
     * Sets the plaintext {@link Message} to this instance.
     *
     * @param plaintext May not be null.
     * @throws IllegalArgumentException If the argument is null.
     */
    public void setPlaintext(Message plaintext) {
        Exceptions.verifyArgumentNotNull(plaintext);
        
        this.plaintext = plaintext;
    }

    public Message getPlaintext() {
        return plaintext;
    }

    /**
     * Sets the ciphertext to this instance.
     *
     * @param ciphertext May not be null.
     * @throws IllegalArgumentException If the argument is null.
     */
    public void setCiphertext(byte[] ciphertext) {
        Exceptions.verifyArgumentNotNull(ciphertext);
        
        this.ciphertext = ciphertext;
    }

    public byte[] getCiphertext() {
        return ciphertext;
    }

}
