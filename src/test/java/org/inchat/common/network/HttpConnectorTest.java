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
package org.inchat.common.network;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class HttpConnectorTest {

    private URL url;
    private HttpConnector http;

    @Before
    public void setUp() throws MalformedURLException {
        url = new URL("http://127.0.0.1:8080/inchat-server/deliver");
        http = new HttpConnector(url.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNull() {
        http = new HttpConnector(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnEmptyString() {
        http = new HttpConnector("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnInvalidUrl() {
        http = new HttpConnector("");
    }

    @Test
    public void testConstructorOnAssignment() {
        assertEquals(url, http.target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecutePostOnNull() {
        http.excutePost(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecutePostOnEmptyArray() {
        http.excutePost(new byte[0]);
    }

}
