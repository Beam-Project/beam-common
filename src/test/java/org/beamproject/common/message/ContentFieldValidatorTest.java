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
package org.beamproject.common.message;

import static org.beamproject.common.message.Field.Cnt.SESSION;
import static org.beamproject.common.message.Field.Cnt.NONCE;
import static org.beamproject.common.message.Field.Cnt.PUBLIC_KEY;
import static org.beamproject.common.message.Field.Cnt.TYP;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class ContentFieldValidatorTest {

    private ContentFieldValidator validator;
    private Message message;

    @Before
    public void setUp() {
        message = new Message();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNull() {
        validator = new ContentFieldValidator((Field.Cnt) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        validator = new ContentFieldValidator((Field.Cnt[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnSingleNull() {
        validator = new ContentFieldValidator(SESSION, null);
    }

    @Test
    public void testIsValidOnMissingField() {
        testValidator(false, TYP);

        message.getContent().put(TYP.toString(), new byte[]{12});
        testValidator(true, TYP);
    }

    @Test
    public void testIsValidOnNullContent() {
        message.getContent().put(TYP.toString(), null);
        testValidator(false, TYP);

        message.getContent().put(TYP.toString(), "FORWARD".getBytes());
        testValidator(true, TYP);
    }

    @Test
    public void testIsValidOnEmptyContent() {
        message.getContent().put(TYP.toString(), new byte[0]);
        testValidator(false, TYP);

        message.getContent().put(TYP.toString(), new byte[]{17});
        testValidator(true, TYP);
    }

    @Test
    public void testIsValidOnTooManyFields() {
        message.getContent().put(TYP.toString(), "type".getBytes());
        testValidator(true, TYP);

        message.getContent().put(SESSION.toString(), "mykey".getBytes());
        testValidator(false, TYP);
    }

    @Test
    public void testIsValidOnSeveralFields() {
        message.getContent().put(TYP.toString(), "type".getBytes());
        message.getContent().put(SESSION.toString(), "mykey".getBytes());
        message.getContent().put(PUBLIC_KEY.toString(), "mypubkey".getBytes());
        message.getContent().put(NONCE.toString(), "213j0-NONCE-ldkjfsd".getBytes());
        testValidator(true, TYP, SESSION, PUBLIC_KEY, NONCE);
    }

    private void testValidator(boolean exptected, Field.Cnt... fields) {
        validator = new ContentFieldValidator(fields);
        assertEquals(exptected, validator.isValid(message));
    }

}
