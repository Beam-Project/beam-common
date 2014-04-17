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

import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ContactTest {

    private Contact contact;
    private Participant server;
    private Participant user;
    private final String name = "spock";

    @Before
    public void setUp() {
        server = new Participant(EccKeyPairGenerator.generate());
        user = new Participant(EccKeyPairGenerator.generate());
        contact = new Contact(server, user, name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        contact = new Contact(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullServer() {
        contact = new Contact(null, user, name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullUser() {
        contact = new Contact(server, null, name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullName() {
        contact = new Contact(server, user, null);
    }

    @Test
    public void testConstructorOnAssignment() {
        assertSame(server, contact.server);
        assertSame(user, contact.user);
        assertSame(name, contact.name);
    }

    @Test
    public void testGetServer() {
        assertSame(server, contact.getServer());
    }

    @Test
    public void testGetUser() {
        assertSame(user, contact.getUser());
    }

    @Test
    public void testGetName() {
        assertSame(name, contact.getName());
    }

    @Test
    public void testToString() {
        assertEquals(contact.name, contact.toString());
        assertEquals(contact.name, "" + contact);
    }

}
