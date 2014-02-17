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

    @Before
    public void setUp() {
        server = new Participant(EccKeyPairGenerator.generate());
        client = new Participant(EccKeyPairGenerator.generate());

        contact = new Contact(server, client);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        contact = new Contact(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullServer() {
        contact = new Contact(null, client);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullClient() {
        contact = new Contact(server, null);
    }

    @Test
    public void testConstructorOnAssignment() {
        assertSame(server, contact.server);
        assertSame(client, contact.client);
    }

    @Test
    public void testGetServer() {
        assertSame(server, contact.getServer());
    }

    @Test
    public void testGetClient() {
        assertSame(client, contact.getClient());
    }

}
