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
         * Defines the type of this message, e.g. HANDSHAKE, FORWARD, etc..
         */
        TYP,
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
         * <p>
         * The type of a message denotes what the next
         * {@link Participant} ({@link Server} or {@link User} respectively its
         * client) should do with the message.
         */
        public enum TypeValue {

            /**
             * Handshake_Challenge. A message of this type is part of a
             * {@link Handshake}.
             * <p>
             * Phase 1 of 3: The following is described from the point of view
             * of the participant who wants to establish an authenticated
             * session.
             * <p>
             * {@code CHALLENGE} tells the other side that an authenticated
             * session should be established.
             * <p>
             * At this time, this side knows the other sides {@link PublicKey}
             * and therefore already can encrypt the first message.
             * <p>
             * This side has to send: The own {@link PublicKey} as bytes (for
             * identification and encryption when the the other side responds)
             * and as challenge a nonce as bytes of the length of
             * {@link Handshake.NONCE_LENGTH_IN_BYTES}.
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
             * {@code RESPONSE} tells the other side that the challenge was
             * accepted and this side therefore is able to send a challenge to
             * which the other side has to response to.
             * <p>
             * At this time, this side knows the other sides {@link PublicKey}
             * since it was sent as part of phase 1, {@code CHALLENGE}. Also,
             * this side knows the challenge sent by the other side as a part of
             * phase 1.
             * <p>
             * This side has to send: An own challenge (nonce as bytes) of the
             * length of {@link Handshake.NONCE_LENGTH_IN_BYTES}. Further, this
             * side has to calculate a digest of [the own public key + the own
             * nonce + the other sides nonce], so the own public key followed by
             * the own nonce followed by the other sides nonce have to be
             * concatenated to one large array of bytes. This large array is
             * then used to calculate the digest. This digest has to be signed
             * with this sides private key. The resulting signature has to be
             * sent with the own nonce.
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
             * {@code SUCCESS} tells the other side that its during phase 1 sent
             * challenge is verified and the challenge, sent by the other side
             * as part of phase 2, is accepted and this side therefore is able
             * to respond.
             * <p>
             * At this time, this side knows the other sides {@link PublicKey},
             * the own nonce (generated in phase 1) and the other sides nonce
             * since it was sent in phase 2. This side also knows the other
             * sides signature, created and sent in phase 2.
             * <p>
             * This side has to send: The response to the challenge from phase
             * 2. Therefore, this side has to calculate a digest of [the own
             * public key + the own nonce + the other sides nonce], so the own
             * public key followed by the own nonce followed by the other sides
             * nonce have to be concatenated to one large array of bytes. This
             * large array is then used to calculate the digest. (This sides
             * nonce was generated in phase 1.) This digest has to be signed
             * with this sides private key. The resulting signature has to be
             * sent.
             */
            HS_SUCCESS,
            /**
             * Handshake_Invalidate. A message of this type is part of a
             * {@link Handshake}.
             * <p>
             * {@code INVALIDATE} tells the other side that an already
             * established session has to be invalidated and that it will no
             * longer be valid. Further, this may be used anytime to abort the
             * handshake procedure.
             * <p>
             * This side has to send the session key to the other side.
             */
            HS_INVALIDATE,
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
