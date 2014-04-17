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
import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.beamproject.common.util.Base58;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class UrlAssemblerTest {

    private final String PROTOCOL_NAME = "beam:";
    private final String NAME = "Mr. Beam";
    private Participant server;
    private Participant client;
    private String url;
    private Contact contact;

    @Before
    public void setUp() {
        server = new Participant(EccKeyPairGenerator.generate());
        client = new Participant(EccKeyPairGenerator.generate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndClientOnNulls() {
        url = UrlAssembler.toUrlByServerAndClient(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndClientOnNullServer() {
        url = UrlAssembler.toUrlByServerAndClient(null, client, NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndClientOnNullClient() {
        url = UrlAssembler.toUrlByServerAndClient(server, null, NAME);
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
        url = UrlAssembler.toUrlByServerAndClient(server, client, NAME);

        String serverPart = server.getPublicKeyAsBase58();
        String clientPart = client.getPublicKeyAsBase58();
        String expectedUrl = PROTOCOL_NAME
                + serverPart
                + "." + clientPart
                + "?name=" + Base58.encode(NAME.getBytes());

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

        String serverPart = server.getPublicKeyAsBase58();
        String expectedUrl = PROTOCOL_NAME + serverPart;

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
        url = UrlAssembler.toUrlByServerAndClient(server, client, NAME);
        url = url.replace("beam", "");
        contact = UrlAssembler.toContactByServerAndClientUrl(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndClientUrlOnWrongProtocol() {
        url = UrlAssembler.toUrlByServerAndClient(server, client, NAME);
        url = url.replace("beam", "baem");
        contact = UrlAssembler.toContactByServerAndClientUrl(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndClientUrlOnMissingSchemaSpeperator() {
        url = UrlAssembler.toUrlByServerAndClient(server, client, NAME);
        url = url.replaceFirst(":", "");
        contact = UrlAssembler.toContactByServerAndClientUrl(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndClientUrlOnMissingSpeperator() {
        url = UrlAssembler.toUrlByServerAndClient(server, client, NAME);
        url = url.replaceFirst(".", "");
        contact = UrlAssembler.toContactByServerAndClientUrl(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToContactByServerAndClientUrlOnTooShortClient() {
        url = UrlAssembler.toUrlByServerAndClient(server, client, NAME);
        url = url.substring(0, url.length() - 30);
        contact = UrlAssembler.toContactByServerAndClientUrl(url);
    }

    @Test
    public void testToContactByServerAndClientUrlOnBouncyCastleProvider() {
        server = new Participant(EccKeyPairGenerator.generate());
        client = new Participant(EccKeyPairGenerator.generate());

        Security.removeProvider(BouncyCastleIntegrator.PROVIDER_NAME);

        url = UrlAssembler.toUrlByServerAndClient(server, client, NAME);
        contact = UrlAssembler.toContactByServerAndClientUrl(url);

        assertArrayEquals(server.getPublicKeyAsBytes(), contact.getServer().getPublicKeyAsBytes());
        assertArrayEquals(client.getPublicKeyAsBytes(), contact.getUser().getPublicKeyAsBytes());
    }

    @Test
    public void testToContactByServerAndClientUrlOnCorrectUrl() {
        server = new Participant(EccKeyPairGenerator.generate());
        client = new Participant(EccKeyPairGenerator.generate());

        url = UrlAssembler.toUrlByServerAndClient(server, client, NAME);
        contact = UrlAssembler.toContactByServerAndClientUrl(url);

        assertArrayEquals(server.getPublicKeyAsBytes(), contact.getServer().getPublicKeyAsBytes());
        assertArrayEquals(client.getPublicKeyAsBytes(), contact.getUser().getPublicKeyAsBytes());
        assertEquals(NAME, contact.getName());
    }
}
