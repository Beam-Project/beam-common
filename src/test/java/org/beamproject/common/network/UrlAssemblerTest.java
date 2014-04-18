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

import java.security.Security;
import org.beamproject.common.Contact;
import org.beamproject.common.Participant;
import org.beamproject.common.crypto.BouncyCastleIntegrator;
import org.beamproject.common.util.Base58;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class UrlAssemblerTest {

    private final String PROTOCOL_NAME = "beam:";
    private final String NAME = "Mr. Beam";
    private Participant server;
    private Participant user;
    private String url;
    private Contact contact;

    @Before
    public void setUp() {
        server = Participant.generate();
        user = Participant.generate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndUserOnNulls() {
        url = UrlAssembler.toUrlByServerAndUser(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndUserOnNullServer() {
        url = UrlAssembler.toUrlByServerAndUser(null, user, NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndUserOnNullUser() {
        url = UrlAssembler.toUrlByServerAndUser(server, null, NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndUserOnNullName() {
        url = UrlAssembler.toUrlByServerAndUser(server, user, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndUserOnEmptyName() {
        url = UrlAssembler.toUrlByServerAndUser(server, user, "");
    }

    @Test
    public void testToUrlByServerAndUser() {
        server = Participant.generate();
        user = Participant.generate();
        url = UrlAssembler.toUrlByServerAndUser(server, user, NAME);

        String serverPart = server.getPublicKeyAsBase58();
        String userPart = user.getPublicKeyAsBase58();
        String expectedUrl = PROTOCOL_NAME
                + serverPart
                + "." + userPart
                + "?name=" + Base58.encode(NAME.getBytes());

        assertEquals(expectedUrl, url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerOnNull() {
        url = UrlAssembler.toUrlByServer(null);
    }

    @Test
    public void testToUrlByServer() {
        server = Participant.generate();
        url = UrlAssembler.toUrlByServer(server);

        String serverPart = server.getPublicKeyAsBase58();
        String expectedUrl = PROTOCOL_NAME + serverPart;

        assertEquals(expectedUrl, url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndUserUrlOnNull() {
        contact = UrlAssembler.toContactByServerAndUserUrl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndUserUrlOnEmptyString() {
        contact = UrlAssembler.toContactByServerAndUserUrl("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndUserUrlOnShortUrl() {
        contact = UrlAssembler.toContactByServerAndUserUrl("lasjfalsfjasd;");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndUserUrlOnMissingProtocol() {
        url = UrlAssembler.toUrlByServerAndUser(server, user, NAME);
        url = url.replace("beam", "");
        contact = UrlAssembler.toContactByServerAndUserUrl(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndUserUrlOnWrongProtocol() {
        url = UrlAssembler.toUrlByServerAndUser(server, user, NAME);
        url = url.replace("beam", "baem");
        contact = UrlAssembler.toContactByServerAndUserUrl(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndUserUrlOnMissingSchemaSpeperator() {
        url = UrlAssembler.toUrlByServerAndUser(server, user, NAME);
        url = url.replaceFirst(":", "");
        contact = UrlAssembler.toContactByServerAndUserUrl(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndUserUrlOnMissingSpeperator() {
        url = UrlAssembler.toUrlByServerAndUser(server, user, NAME);
        url = url.replaceFirst(".", "");
        contact = UrlAssembler.toContactByServerAndUserUrl(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndUserUrlOnTooShortUser() {
        url = UrlAssembler.toUrlByServerAndUser(server, user, NAME);
        url = url.substring(0, url.length() - 30);
        contact = UrlAssembler.toContactByServerAndUserUrl(url);
    }

    @Test
    public void testToContactByServerAndUserUrlOnBouncyCastleProvider() {
        server = Participant.generate();
        user = Participant.generate();

        Security.removeProvider(BouncyCastleIntegrator.PROVIDER_NAME);

        url = UrlAssembler.toUrlByServerAndUser(server, user, NAME);
        contact = UrlAssembler.toContactByServerAndUserUrl(url);

        assertArrayEquals(server.getPublicKeyAsBytes(), contact.getServer().getPublicKeyAsBytes());
        assertArrayEquals(user.getPublicKeyAsBytes(), contact.getUser().getPublicKeyAsBytes());
    }

    @Test
    public void testToContactByServerAndUserUrlOnCorrectUrl() {
        server = Participant.generate();
        user = Participant.generate();

        url = UrlAssembler.toUrlByServerAndUser(server, user, NAME);
        contact = UrlAssembler.toContactByServerAndUserUrl(url);

        assertArrayEquals(server.getPublicKeyAsBytes(), contact.getServer().getPublicKeyAsBytes());
        assertArrayEquals(user.getPublicKeyAsBytes(), contact.getUser().getPublicKeyAsBytes());
        assertEquals(NAME, contact.getName());
    }
}
