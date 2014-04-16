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
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * A helper class to manage the Bouncy Castle integration into the Java
 * Cryptographic Architecture.
 */
public abstract class BouncyCastleIntegrator {

    public final static String PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;

    /**
     * Makes sure that the {@link BouncyCastleProvider} is integrated into the
     * {@link Security} Provider List properly. If it is already installed,
     * nothing changes.
     */
    public static void initBouncyCastleProvider() {
        if (Security.getProvider(PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
}
