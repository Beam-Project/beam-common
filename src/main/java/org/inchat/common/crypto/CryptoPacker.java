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
package org.inchat.common.crypto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.inchat.common.Message;
import org.inchat.common.MessageField;
import org.inchat.common.Participant;
import org.inchat.common.util.Exceptions;
import org.msgpack.MessagePack;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.unpacker.Unpacker;

/**
 * Allows to pack-and-encrypt and decrypt-and-unpack using {@link EccCipher} and
 * {@link MessagePack}.
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
     * The field Content ({@code CNT}) will be encrypted.<p>
     * The fields Version ({@code VRS}) and Participant ({@code PRT}) will
     * <b>not</b> be encrypted.
     *
     * @param plaintext The unencrypted {@link Message}. This may not be null.
     * @param participant The {@link Participant} with the needed key, in this
     * case the public key of the remote side. This may not be null.
     * @return The messagePacked and encrypted message.
     * @throws IllegalArgumentException If at least one argument is null.
     * @throws PackerException If anything goes wrong during
     * packing/serializing.
     */
    public byte[] packAndEncrypt(Message plaintext, Participant participant) {
        validatePlaintext(plaintext);
        validateParticipant(participant);

        packContentField();
        encryptContentField();
        packAllPartsToCiphertext();

        return ciphertext;
    }

    private void validatePlaintext(Message plaintext) {
        Exceptions.verifyArgumentNotNull(plaintext);

        if (plaintext.getParticipant() == null) {
            throw new IllegalArgumentException("The argument has to have a Participant.");
        }

        this.plaintext = plaintext;
    }

    private void validateParticipant(Participant participant) {
        Exceptions.verifyArgumentNotNull(participant);

        this.participant = participant;
    }

    private void packContentField() {
        Map<String, byte[]> map = new HashMap<>();
        map.put(MessageField.CNT_MSG.toString(), plaintext.getContent());

        packedContent = serializeMap(map);
    }

    private void encryptContentField() {
        encryptedPacketContent = eccCipher.encrypt(packedContent, participant.getPublicKey());
    }

    private void packAllPartsToCiphertext() {
        Map<String, byte[]> map = new HashMap<>();

        map.put(MessageField.VRS.toString(), plaintext.getVersion().getBytes());
        map.put(MessageField.PRT.toString(), plaintext.getParticipant().getPublicKeyAsBytes());
        map.put(MessageField.CNT.toString(), encryptedPacketContent);

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
        Exceptions.verifyArgumentNotNull(ciphertext);

        this.ciphertext = ciphertext;
    }

    private void unpackAllPartsFromCiphertext() {
        Map<String, byte[]> map = buildMapFromBytes(ciphertext);

        plaintext = new Message();

        plaintext.setVersion(readStringFromMap(map, MessageField.VRS));
        plaintext.setParticipant(participant);
        encryptedPacketContent = readByteArrayFromMap(map, MessageField.CNT);
    }

    private void decyptContent() {
        packedContent = eccCipher.decrypt(encryptedPacketContent, participant.getPrivateKey());
    }

    private void unpackContent() {
        Map<String, byte[]> map = buildMapFromBytes(packedContent);
        plaintext.setContent(readByteArrayFromMap(map, MessageField.CNT_MSG));
    }

    private Map<String, byte[]> buildMapFromBytes(byte[] bytes) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        Unpacker unpacker = messagePack.createUnpacker(inputStream);
        Template<Map<String, byte[]>> mapTemplate = Templates.tMap(Templates.TString, Templates.TByteArray);

        try {
            return unpacker.read(mapTemplate);
        } catch (IOException ex) {
            throw new PackerException("Could not read from an unpacker: " + ex.getMessage());
        }
    }

    private String readStringFromMap(Map<String, byte[]> map, MessageField field) {
        return new String(readByteArrayFromMap(map, field));
    }

    private byte[] readByteArrayFromMap(Map<String, byte[]> map, MessageField field) {
        return map.get(field.toString());
    }

}
