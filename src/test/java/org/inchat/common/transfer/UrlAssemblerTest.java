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

import javax.xml.bind.DatatypeConverter;
import org.inchat.common.Participant;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class UrlAssemblerTest {

    private final static String SERVER_ID = "b3eacd33433b31b5252351032c9b3e7a2e7aa7738d5decdf0dd6c62680853c06";
    private final static String CLIENT_ID = "948fe603f61dc036b5c596dc09fe3ce3f3d30dc90f024c85f3c82db2ccab679d";
    private final static String EXPECTED_URL = "inchat://" + SERVER_ID + "/" + CLIENT_ID;
    private Participant server;
    private Participant client;
    private String output;

    @Before
    public void setUp() {
        server = new Participant(DatatypeConverter.parseHexBinary(SERVER_ID));
        client = new Participant(DatatypeConverter.parseHexBinary(CLIENT_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndClientOnNulls() {
        output = UrlAssembler.toUrlByServerAndClient(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndClientOnNullServer() {
        output = UrlAssembler.toUrlByServerAndClient(null, client);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToUrlByServerAndClientOnNullParticipant() {
        output = UrlAssembler.toUrlByServerAndClient(server, null);
    }

    @Test
    public void testToUrlByServerAndClient() {
        output = UrlAssembler.toUrlByServerAndClient(server, client);
        assertEquals(EXPECTED_URL, output);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToServerByUrlOnNull() {
        server = UrlAssembler.toServerByUrl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToServerByUrlOnEmptyArgument() {
        server = UrlAssembler.toServerByUrl("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToServerByUrlOnNonUrl() {
        server = UrlAssembler.toServerByUrl("blablup anything else");
    }

    @Test
    public void testToServerByUrl() {
        server = UrlAssembler.toServerByUrl(EXPECTED_URL);
        byte[] serverId = DatatypeConverter.parseHexBinary(SERVER_ID);
        assertArrayEquals(serverId, server.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToClientByUrlOnNull() {
        server = UrlAssembler.toClientByUrl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToClientByUrlOnEmptyArgument() {
        server = UrlAssembler.toClientByUrl("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToClientByUrlOnNonUrl() {
        server = UrlAssembler.toClientByUrl("blablup anything else");
    }

    @Test
    public void testToCleintByUrl() {
        client = UrlAssembler.toClientByUrl(EXPECTED_URL);
        byte[] clientId = DatatypeConverter.parseHexBinary(CLIENT_ID);
        assertArrayEquals(clientId, client.getId());
    }
}
