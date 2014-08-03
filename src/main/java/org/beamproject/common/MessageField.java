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
package org.beamproject.common;

import java.security.PublicKey;
import org.beamproject.common.crypto.Handshake;
import org.beamproject.common.crypto.HandshakeChallenger;
import org.beamproject.common.crypto.HandshakeResponder;

/**
 * Defines all typically used field identifier used in {@link Message}s.
 * <p>
 * The structure is nested, like a {@link Message}. On the first <i>layer</i>
 * are the field identifiers that are also on the first layer in a message.
 * <p>
 * For example, the field {@code MessageField.VRS} is <i>directly</i> in a
 * message. On the other hand, e.g. the field {@code TYP} is nested:
 * {@code MessageField.ContentField.TYP}
 * <p>
 * The nested {@code enum}s represent therefore the <i>set of keys</i> for
 * nested message fields.
 *
 * @see Message
 * @see MessageField.ContentField
 * @see MessageField.ContentField.TypeValue
 */
public enum MessageField {

    /**
     * Stands for "Version". This field contains the message format version.
     */
    VRS,
    /**
     * Stands for "Recipient" and is the public key (X.509 encoded) of the
     * target {@link Participant} of this message.
     */
    RCP,
    /**
     * Stands for "ContentField" and contains the message as several sub fields.
     */
    CNT;

    /**
     * These fields are for the identifiers used for sub-fields in the content
     * block, {@code CNT}, of a {@link Message}.
     */
    public enum ContentField {

        /**
         * Defines the type of this message, e.g. HANDSHAKE, HEARTBEAT, etc..
         */
        TYP,
        /**
         * This field is a part of the handshake protocol. This protocol allows
         * to establish a long-time session.
         * <p>
         * {@code HSPHASE} stands for "Handshake Phase" and informs the
         * recipient of the message about the current phase in the protocol.
         */
        HSPHASE,
        /**
         * This field is a part of the handshake protocol. This protocol allows
         * to establish a long-time session.
         * <p>
         * {@code HSPUBKEY} stands for "Handshake Public Key" and contains the
         * senders {@link PublicKey} as byte array.
         */
        HSPUBKEY,
        /**
         * This field is a part of the handshake protocol. This protocol allows
         * to establish a long-time session.
         * <p>
         * {@code HSNONCE} stands for "Handshake Nonce" and a nonce, generated
         * by the sender.
         */
        HSNONCE,
        /**
         * This field is a part of the handshake protocol. This protocol allows
         * to establish a long-time session.
         * <p>
         * {@code HSSIG} stands for "Handshake Signature" and contains the
         * signature, created with the other sides private key.
         */
        HSSIG,
        /**
         * This field is a part of the handshake protocol. This protocol allows
         * to establish a long-time session.
         * <p>
         * {@code HSKEY} stands for "Handshake Key" and contains the session key
         * that was established earlier.
         */
        HSKEY,
        /**
         * This field is a part of the heartbeat protocol. This protocol allows
         * to keep long-time sessions alive.
         * <p>
         * {@code HBKEY} stands for "Heartbeat Key" and contains the already
         * established session key.
         */
        HBKEY,
        /**
         * This field is a part of the heartbeat protocol. This protocol allows
         * to keep long-time sessions alive.
         * <p>
         * {@code HBTS} stands for "Heartbeat Timestamp" and contains the
         * current timestamp in ISO8601 format.
         */
        HBTS,
        /**
         * Stands for "Message" and contains the message text itself.
         */
        MSG;

        /**
         * @return The bytes of the string representation of this value.
         */
        public byte[] getBytes() {
            return toString().getBytes();
        }

        /**
         * These fields are for the identifiers used for sub-fields in the
         * {@link ContentField} {@code TYP}, of a {@link Message}.
         */
        public enum TypeValue {

            /**
             * This type can be used when the type of a {@link Message} is not
             * clear yet, e.g. before it's been decrypted.
             * <p>
             * This is not a valid value when sending messages around.
             */
            BLANK,
            /**
             * Tells the recipient that the message with this type should be
             * interpreted as part of the handshake, which is used to establish
             * authenticity.
             *
             * @see Handshake
             * @see HandshakeChallenger
             * @see HandshakeResponder
             */
            HANDSHAKE,
            /**
             * Tells the recipient, typically a {@link Server} that the message
             * with this type contains another message, which should be
             * forwarded to its recipient.
             */
            FORWARD;

            /**
             * @return The bytes of the string representation of this value.
             */
            public byte[] getBytes() {
                return toString().getBytes();
            }
        }
    }
}
