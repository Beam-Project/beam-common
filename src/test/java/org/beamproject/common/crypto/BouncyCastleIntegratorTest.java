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
package org.beamproject.common.crypto;

import java.security.Security;
import static org.beamproject.common.crypto.BouncyCastleIntegrator.PROVIDER_NAME;
import static org.beamproject.common.crypto.BouncyCastleIntegrator.initBouncyCastleProvider;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class BouncyCastleIntegratorTest {

    @Test
    public void testProviderName() {
        assertEquals(BouncyCastleProvider.PROVIDER_NAME, PROVIDER_NAME);
    }

    @Test
    public void testInitBouncyCasteProvider() {
        if (Security.getProvider(PROVIDER_NAME) != null) {
            Security.removeProvider(PROVIDER_NAME);
        }

        assertNull(Security.getProvider(PROVIDER_NAME));
        initBouncyCastleProvider();
        assertNotNull(Security.getProvider(PROVIDER_NAME));
    }
}
