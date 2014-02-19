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
package org.inchat.common.transfer;

import java.security.Security;
import org.inchat.common.Contact;
import org.inchat.common.Participant;
import org.inchat.common.crypto.BouncyCastleIntegrator;
import org.inchat.common.crypto.EccKeyPairGenerator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class UrlAssemblerTest {

    private Participant server;
    private Participant client;
    private String name;
    private String url;
    private Contact contact;

    @Before
    public void setUp() {
        server = new Participant(EccKeyPairGenerator.generate());
        client = new Participant(EccKeyPairGenerator.generate());
        name = "myName";
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndClientOnNulls() {
        url = UrlAssembler.toUrlByServerAndClient(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndClientOnNullServer() {
        url = UrlAssembler.toUrlByServerAndClient(null, client, name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndClientOnNullClient() {
        url = UrlAssembler.toUrlByServerAndClient(server, null, name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndClientOnNullName() {
        url = UrlAssembler.toUrlByServerAndClient(server, client, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndClientOnEmptyName() {
        url = UrlAssembler.toUrlByServerAndClient(server, client, "");
    }

    @Test
    public void testToUrlByServerAndClient() {
        server = new Participant(EccKeyPairGenerator.generate());
        client = new Participant(EccKeyPairGenerator.generate());
        url = UrlAssembler.toUrlByServerAndClient(server, client, name);

        String serverPart = server.getPublicKeyAsHex().toLowerCase();
        String clientPart = client.getPublicKeyAsHex().toLowerCase();
        String expectedUrl = "inchat:"
                + serverPart
                + "." + clientPart
                + "?name=" + name;

        assertEquals(expectedUrl, url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerOnNull() {
        url = UrlAssembler.toUrlByServer(null);
    }

    @Test
    public void testToUrlByServer() {
        server = new Participant(EccKeyPairGenerator.generate());
        url = UrlAssembler.toUrlByServer(server);

        String serverPart = server.getPublicKeyAsHex();
        String expectedUrl = "inchat:" + serverPart;
        expectedUrl = expectedUrl.toLowerCase();

        assertEquals(expectedUrl, url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndClientUrlOnNull() {
        contact = UrlAssembler.toContactByServerAndClientUrl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndClientUrlOnEmptyString() {
        contact = UrlAssembler.toContactByServerAndClientUrl("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndClientUrlOnShortUrl() {
        contact = UrlAssembler.toContactByServerAndClientUrl("lasjfalsfjasd;");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndClientUrlOnMissingProtocol() {
        url = UrlAssembler.toUrlByServerAndClient(server, client, name);
        url = url.replace("inchat", "");
        contact = UrlAssembler.toContactByServerAndClientUrl(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndClientUrlOnWrongProtocol() {
        url = UrlAssembler.toUrlByServerAndClient(server, client, name);
        url = url.replace("inchat", "inhcat");
        contact = UrlAssembler.toContactByServerAndClientUrl(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndClientUrlOnWrongCharacters() {
        url = UrlAssembler.toUrlByServerAndClient(server, client, name);
        url = url.replaceFirst("1", "z");
        contact = UrlAssembler.toContactByServerAndClientUrl(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndClientUrlOnMissingSchemaSpeperator() {
        url = UrlAssembler.toUrlByServerAndClient(server, client, name);
        url = url.replaceFirst(":", "");
        contact = UrlAssembler.toContactByServerAndClientUrl(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndClientUrlOnMissingSpeperator() {
        url = UrlAssembler.toUrlByServerAndClient(server, client, name);
        url = url.replaceFirst(".", "");
        contact = UrlAssembler.toContactByServerAndClientUrl(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndClientUrlOnTooShortClient() {
        url = UrlAssembler.toUrlByServerAndClient(server, client, name);
        url = url.substring(0, url.length() - 30);
        contact = UrlAssembler.toContactByServerAndClientUrl(url);
    }

    @Test
    public void testToContactByServerAndClientUrlOnBouncyCastleProvider() {
        server = new Participant(EccKeyPairGenerator.generate());
        client = new Participant(EccKeyPairGenerator.generate());

        Security.removeProvider(BouncyCastleIntegrator.PROVIDER_NAME);

        url = UrlAssembler.toUrlByServerAndClient(server, client, name);
        System.out.println("" + url);
        contact = UrlAssembler.toContactByServerAndClientUrl(url);

        assertArrayEquals(server.getPublicKeyAsBytes(), contact.getServer().getPublicKeyAsBytes());
        assertArrayEquals(client.getPublicKeyAsBytes(), contact.getClient().getPublicKeyAsBytes());
    }

    @Test
    public void testToContactByServerAndClientUrlOnCorrectUrl() {
        server = new Participant(EccKeyPairGenerator.generate());
        client = new Participant(EccKeyPairGenerator.generate());

        url = UrlAssembler.toUrlByServerAndClient(server, client, name);
        contact = UrlAssembler.toContactByServerAndClientUrl(url);

        assertArrayEquals(server.getPublicKeyAsBytes(), contact.getServer().getPublicKeyAsBytes());
        assertArrayEquals(client.getPublicKeyAsBytes(), contact.getClient().getPublicKeyAsBytes());
        assertEquals(name, contact.getName());
    }
}
