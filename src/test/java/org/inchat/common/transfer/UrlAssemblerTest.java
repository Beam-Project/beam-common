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
import org.inchat.common.crypto.EccKeyPairGenerator;
import org.junit.Test;
import static org.junit.Assert.*;

public class UrlAssemblerTest {

    private Participant server;
    private Participant client;
    private String output;

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
        server = new Participant(EccKeyPairGenerator.generate());
        client = new Participant(EccKeyPairGenerator.generate());
        output = UrlAssembler.toUrlByServerAndClient(server, client);

        String serverPart = DatatypeConverter.printHexBinary(server.getId());
        String clientPart = DatatypeConverter.printHexBinary(client.getId());
        String expectedUrl = "inchat://" + serverPart + "/" + clientPart;
        expectedUrl = expectedUrl.toLowerCase();

        assertEquals(expectedUrl, output);
    }

}
