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

import org.inchat.common.crypto.EccKeyPairGenerator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ContactTest {

    private Contact contact;
    private Participant server;
    private Participant client;
    private final String name = "spock";

    @Before
    public void setUp() {
        server = new Participant(EccKeyPairGenerator.generate());
        client = new Participant(EccKeyPairGenerator.generate());

        contact = new Contact(server, client, name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        contact = new Contact(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullServer() {
        contact = new Contact(null, client, name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullClient() {
        contact = new Contact(server, null, name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullName() {
        contact = new Contact(server, client, null);
    }

    @Test
    public void testConstructorOnAssignment() {
        assertSame(server, contact.server);
        assertSame(client, contact.client);
        assertSame(name, contact.name);
    }

    @Test
    public void testGetServer() {
        assertSame(server, contact.getServer());
    }

    @Test
    public void testGetClient() {
        assertSame(client, contact.getClient());
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
