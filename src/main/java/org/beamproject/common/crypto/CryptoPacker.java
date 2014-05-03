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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.beamproject.common.Message;
import static org.beamproject.common.MessageField.*;
import org.beamproject.common.MessageField.ContentField;
import static org.beamproject.common.MessageField.ContentField.TypeValue.*;
import org.beamproject.common.Participant;
import org.beamproject.common.util.Exceptions;
import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.unpacker.Unpacker;

/**
 * Allows to pack-and-encrypt and decrypt-and-unpack using {@link EccCipher} and
 * {@link MessagePack}.
 *
 * @see Message
 * @see EccCipher
 */
public class CryptoPacker {

    EccCipher eccCipher;
    Participant participant;
    MessagePack messagePack;
    Message plaintext;
    byte[] packedContent;
    byte[] encryptedPacketContent;
    byte[] ciphertext;

    /**
     * Instantiates a new {@link CryptoPacker}.
     */
    public CryptoPacker() {
        eccCipher = new EccCipher();
        messagePack = new MessagePack();
    }

    /**
     * Packs and encrypts the given {@code plaintext} to a {@link MessagePack}
     * byte array.<p>
     * The field ContentField ({@code CNT}) will be encrypted.<p>
     * The fields Version ({@code VRS}) and Participant ({@code PRT}) will
     * <b>not</b> be encrypted.
     *
     * @param plaintext The unencrypted {@link Message}. This may not be null.
     * @return The messagePacked and encrypted message.
     * @throws IllegalArgumentException If at least one argument is null.
     * @throws PackerException If anything goes wrong during
     * packing/serializing.
     */
    public byte[] packAndEncrypt(Message plaintext) {
        validatePlaintext(plaintext);

        packContentField();
        encryptContentField();
        packAllPartsToCiphertext();

        return ciphertext;
    }

    private void validatePlaintext(Message plaintext) {
        Exceptions.verifyArgumentsNotNull(plaintext);

        if (plaintext.getRecipient() == null) {
            throw new IllegalArgumentException("The argument has to have a Participant.");
        }

        this.plaintext = plaintext;
    }

    private void packContentField() {
        packedContent = serializeMap(plaintext.getContent());
    }

    private void encryptContentField() {
        encryptedPacketContent = eccCipher.encrypt(packedContent, plaintext.getRecipient().getPublicKey());
    }

    private void packAllPartsToCiphertext() {
        Map<String, byte[]> map = new HashMap<>();

        map.put(VRS.toString(), plaintext.getVersion().getBytes());
        map.put(PRT.toString(), plaintext.getRecipient().getPublicKeyAsBytes());
        map.put(CNT.toString(), encryptedPacketContent);

        ciphertext = serializeMap(map);
    }

    private byte[] serializeMap(Map<String, byte[]> map) {
        try {
            return messagePack.write(map);
        } catch (IOException ex) {
            throw new PackerException("Could not serialize a map to a MessagePack: " + ex.getMessage());
        }
    }

    /**
     * Decrypts and unpacks the given {@code ciphertext} to a {@link Message}.
     *
     * @param ciphertext The encrypted message, serialized using
     * {@link MessagePack}. This may not be null.
     * @param participant The {@link Participant} with the needed key, in this
     * case the private key of the local side. This may not be null.
     * @return The plaintext message.
     * @throws IllegalArgumentException If at lest one argument is null.
     * @throws PackerException If anything goes wrong during
     * unpacking/deserializing. Also, when the integrity of the message cannot
     * be verified.
     */
    public Message decryptAndUnpack(byte[] ciphertext, Participant participant) {
        validateCiphertext(ciphertext);
        validateParticipant(participant);

        unpackAllPartsFromCiphertext();

        decyptContent();
        unpackContent();

        return plaintext;
    }

    private void validateCiphertext(byte[] ciphertext) {
        Exceptions.verifyArgumentsNotNull(ciphertext);

        this.ciphertext = ciphertext;
    }

    private void validateParticipant(Participant participant) {
        Exceptions.verifyArgumentsNotNull(participant);

        this.participant = participant;
    }

    private void unpackAllPartsFromCiphertext() {
        Map<String, byte[]> map = buildMapFromBytes(ciphertext);

        plaintext = new Message(BLANK, participant);
        plaintext.setVersion(readStringFromMap(map, VRS));
        encryptedPacketContent = readByteArrayFromMap(map, CNT);
    }

    private void decyptContent() {
        packedContent = eccCipher.decrypt(encryptedPacketContent, participant.getPrivateKey());
    }

    private void unpackContent() {
        Map<String, byte[]> map = buildMapFromBytes(packedContent);

        for (ContentField field : ContentField.values()) {
            if (map.containsKey(field.toString())) {
                plaintext.putContent(field, readByteArrayFromMap(map, field));
            }
        }
    }

    private Map<String, byte[]> buildMapFromBytes(byte[] bytes) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        Unpacker unpacker = messagePack.createUnpacker(inputStream);
        Template<Map<String, byte[]>> mapTemplate = Templates.tMap(Templates.TString, Templates.TByteArray);

        try {
            return unpacker.read(mapTemplate);
        } catch (MessageTypeException | IOException ex) {
            throw new PackerException("Could not read from an unpacker: " + ex.getMessage());
        }
    }

    private String readStringFromMap(Map<String, byte[]> map, Enum field) {
        return new String(readByteArrayFromMap(map, field));
    }

    private byte[] readByteArrayFromMap(Map<String, byte[]> map, Enum field) {
        return map.get(field.toString());
    }

}
