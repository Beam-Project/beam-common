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

import java.security.PublicKey;
import org.beamproject.common.crypto.Handshake;

/**
 * Defines all typically used field identifier used in {@link Message}s.
 * <p>
 * The structure is nested, like a {@link Message}. On the first <i>layer</i>
 * are the field identifiers that are also on the first layer in a message.
 * <p>
 * For example, the field {@link Field#VRS} is <i>directly</i> in a message. On
 * the other hand, e.g. the field {@code TYP} is nested: {@link Field#Cnt#TYP}
 * <p>
 * The nested {@code enum}s represent therefore the <i>set of keys</i> for
 * nested message fields.
 *
 * @see Message
 * @see Field.Cnt
 * @see Field.Cnt.Typ
 */
public enum Field {

    /**
     * Stands for "version". This field contains the message format version.
     * <p>
     * <b>Usage:</b> as key of a key/value pair, directly in message.
     */
    VRS,
    /**
     * Stands for "content" and contains the message as several sub fields.
     * <p>
     * <b>Usage:</b> as key of a key/value pair, directly in message.
     */
    CNT;

    /**
     * This enumeration provides all <b>keys</b> for the key/value pairs, used
     * in the content block {@link Field#CNT}.
     */
    public enum Cnt {

        /**
         * This content field defines the type of this message, e.g.
         * HS_CHALLENGE, FORWARD, etc..
         * <p>
         * <b>Usage:</b> as key of a key/value pair, nested in
         * {@link Field#CNT}.
         *
         * @see Typ
         */
        TYP,
        /**
         * This content field is a part of the handshake protocol. This protocol
         * allows to establish a long-time session.
         * <p>
         * {@code HS_PUBKEY} stands for "Handshake Public Key" and contains the
         * senders {@link PublicKey} as byte array.
         * <p>
         * <b>Usage:</b> as key of a key/value pair, nested in
         * {@link Field#CNT}.
         */
        HS_PUBKEY,
        /**
         * This content field is a part of the handshake protocol. This protocol
         * allows to establish a long-time session.
         * <p>
         * {@code HS_NONCE} stands for "Handshake Nonce" and a nonce, generated
         * by the sender.
         * <p>
         * <b>Usage:</b> as key of a key/value pair, nested in
         * {@link Field#CNT}.
         */
        HS_NONCE,
        /**
         * This content field is a part of the handshake protocol. This protocol
         * allows to establish a long-time session.
         * <p>
         * {@code HS_SIG} stands for "Handshake Signature" and contains the
         * signature, created with the other sides private key.
         * <p>
         * <b>Usage:</b> as key of a key/value pair, nested in
         * {@link Field#CNT}.
         */
        HS_SIG,
        /**
         * This content field is a part of the handshake protocol. This protocol
         * allows to establish a long-time session.
         * <p>
         * {@code HS_KEY} stands for "Handshake Key" and contains the session
         * key that was established earlier.
         * <p>
         * <b>Usage:</b> as key of a key/value pair, nested in
         * {@link Field#CNT}.
         */
        HS_KEY,
        /**
         * Stands for "Message" and contains the message text itself.
         * <p>
         * <b>Usage:</b> as key of a key/value pair, nested in
         * {@link Field#CNT}.
         */
        MSG;

        /**
         * @return The bytes of the string representation of this value.
         */
        public byte[] getBytes() {
            return toString().getBytes();
        }

        /**
         * This enumeration provides all allowed <b>values</b> for the key/value
         * pair {@link Field.Cnt#TYP}, what represents the type of a message.
         * <p>
         * The type of a message denotes what the next
         * {@link Participant} ({@link Server} or {@link User} respectively its
         * client) should do with the message.
         */
        public enum Typ {

            /**
             * Handshake_Challenge. A message of this type is part of a
             * {@link Handshake}.
             * <p>
             * Phase 1 of 3: The following is described from the point of view
             * of the participant who wants to establish an authenticated
             * session.
             * <p>
             * {@code HS_CHALLENG} tells the other side that an authenticated
             * session should be established.
             * <p>
             * At this time, this side knows the other sides {@link PublicKey}
             * and therefore already can encrypt the first message.
             * <p>
             * This side has to send: The own {@link PublicKey} as bytes (for
             * identification and encryption when the the other side responds)
             * and as challenge a nonce as bytes of the length of
             * {@link Handshake#NONCE_LENGTH_IN_BYTES}.
             * <p>
             * <b>Usage:</b> as value of the key/value pair
             * {@link Field.Cnt#TYP}.
             */
            HS_CHALLENGE,
            /**
             * Handshake_Response. A message of this type is part of a
             * {@link Handshake}.
             * <p>
             * PHASE 2 OF 3: The following is described from the point of view
             * of the participant who has been contacted by an unidentified
             * participant who wants to establish an authenticated session.
             * <p>
             * {@code HS_RESPONSE} tells the other side that the challenge was
             * accepted and this side therefore is able to send a challenge to
             * which the other side has to response to.
             * <p>
             * At this time, this side knows the other sides {@link PublicKey}
             * since it was sent as part of phase 1, {@code HS_CHALLENGE}. Also,
             * this side knows the challenge sent by the other side as a part of
             * phase 1.
             * <p>
             * This side has to send: An own challenge (nonce as bytes) of the
             * length of {@link Handshake#NONCE_LENGTH_IN_BYTES}. Further, this
             * side has to calculate a digest of [the own public key + the own
             * nonce + the other sides nonce]. The own public key followed by
             * the own nonce followed by the other sides nonce have to be
             * concatenated to one large array of bytes. This large array is
             * then used to calculate the digest. This digest has to be signed
             * with this sides private key. The resulting signature has to be
             * sent with the own nonce.
             * <p>
             * <b>Usage:</b> as value of the key/value pair
             * {@link Field.Cnt#TYP}.
             */
            HS_RESPONSE,
            /**
             * Handshake_Success. A message of this type is part of a
             * {@link Handshake}.
             * <p>
             * PHASE 3 OF 3: The following is described from the point of view
             * of the participant who wants to establish an authenticated
             * session.
             * <p>
             * {@code HS_SUCCESS} tells the other side that its during phase 1
             * sent challenge is verified and the challenge, sent by the other
             * side as part of phase 2, is accepted and this side therefore is
             * able to respond.
             * <p>
             * At this time, this side knows the other sides {@link PublicKey},
             * the own nonce (generated in phase 1) and the other sides nonce
             * since it was sent in phase 2. This side also knows the other
             * sides signature, created and sent in phase 2.
             * <p>
             * This side has to send: The response to the challenge from phase
             * 2. Therefore, this side has to calculate a digest of [the own
             * public key + the own nonce + the other sides nonce]. The own
             * public key followed by the own nonce followed by the other sides
             * nonce have to be concatenated to one large array of bytes. This
             * large array is then used to calculate the digest. (This sides
             * nonce was generated in phase 1.) This digest has to be signed
             * with this sides private key. The resulting signature has to be
             * sent.
             * <p>
             * <b>Usage:</b> as value of the key/value pair
             * {@link Field.Cnt#TYP}.
             */
            HS_SUCCESS,
            /**
             * Handshake_Invalidate. A message of this type is part of a
             * {@link Handshake}.
             * <p>
             * {@code HS_INVALIDATE} tells the other side that an already
             * established session has to be invalidated and that it will no
             * longer be valid. Further, this may be used anytime to abort the
             * handshake procedure.
             * <p>
             * This side has to send the session key to the other side.
             * <p>
             * <b>Usage:</b> as value of the key/value pair
             * {@link Field.Cnt#TYP}.
             */
            HS_INVALIDATE,
            /**
             * Tells the recipient, typically a {@link Server} that the message
             * with this type contains another message, which should be
             * forwarded to its recipient.
             * <p>
             * <b>Usage:</b> as value of the key/value pair
             * {@link Field.Cnt#TYP}.
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
