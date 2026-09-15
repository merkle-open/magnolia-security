package com.merkle.oss.magnolia.property;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.lang3.Strings;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.machinezoo.noexception.Exceptions;

/**
 * Used to de/encrypt properties in magnolia properties.<br>
 * See also<br>
 *  - {@link EncryptedMagnoliaConfigurationProperties} <br>
 */
public class StringEncryptor {
    private static final String ENCRYPTED_VALUE_PREFIX = "ENC(";
    private static final String ENCRYPTED_VALUE_SUFFIX = ")";
    private final int saltSize;
    private final String encryptorAlgorithm;
    private final Charset messageCharset;
    private final Charset encryptedMessageCharset;
    private final String password;

    public StringEncryptor(final String password) {
        this(password, 16, "PBEWITHSHA256AND256BITAES-CBC-BC", StandardCharsets.UTF_8, StandardCharsets.US_ASCII);
    }
    public StringEncryptor(
            final String password,
            final int saltSize,
            final String encryptorAlgorithm,
            final Charset messageCharset,
            final Charset encryptedMessageCharset
    ) {
        this.password = password;
        this.saltSize = saltSize;
        this.encryptorAlgorithm = encryptorAlgorithm;
        this.messageCharset = messageCharset;
        this.encryptedMessageCharset = encryptedMessageCharset;
    }

    public String encrypt(final String message) {
        final byte[] encryptedMessage = Exceptions.wrap().get(() -> {
            final byte[] salt = generateSalt(saltSize);
            final Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, salt);
            final byte[] encrypted = cipher.doFinal(message.getBytes(messageCharset));
            return prependSalt(encrypted, salt);
        });
        return toString(encryptedMessage);
    }

    public String decrypt(final String encryptedMessage) {
        final byte[] decodedEncryptedMessage = fromString(encryptedMessage);
        final byte[] salt = getSalt(decodedEncryptedMessage, saltSize);
        final byte[] message = getEncryptedMessage(decodedEncryptedMessage, saltSize);
        final byte[] decrypted = Exceptions.wrap().get(() -> getCipher(Cipher.DECRYPT_MODE, salt).doFinal(message));
        return new String(decrypted, messageCharset);
    }

    public static boolean isEncryptedValue(final String message) {
        return getStrippedEncryptedMessage(message).isPresent();
    }

    private Cipher getCipher(final int mode, final byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        final SecretKeyFactory factory = SecretKeyFactory.getInstance(encryptorAlgorithm, new BouncyCastleProvider());
        final SecretKey secretKey = factory.generateSecret(new PBEKeySpec(password.toCharArray()));
        final Cipher cipher = Cipher.getInstance(factory.getAlgorithm(), factory.getProvider());
        cipher.init(mode, secretKey, new PBEParameterSpec(
                salt,
                1000,
                new IvParameterSpec(generateIv(cipher.getBlockSize()))
        ));
        return cipher;
    }

    protected byte[] generateIv(final int size) {
        //same as jasypt NoIvGenerator
        return new byte[0];
    }

    private byte[] generateSalt(final int size) throws NoSuchAlgorithmException {
        //same as jasypt RandomSaltGenerator
        final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        final byte[] salt = new byte[size];
        random.nextBytes(salt);
        return salt;
    }

    private byte[] prependSalt(final byte[] encryptedMessage, final byte[] salt) {
        final byte[] result = new byte[salt.length + encryptedMessage.length];
        System.arraycopy(salt, 0, result, 0, salt.length);
        System.arraycopy(encryptedMessage, 0, result, salt.length, encryptedMessage.length);
        return result;
    }

    private byte[] getSalt(final byte[] encryptedMessageWithPrependedSalt, final int saltSize) {
        final byte[] salt = new byte[saltSize];
        System.arraycopy(encryptedMessageWithPrependedSalt, 0, salt, 0, saltSize);
        return salt;
    }

    private byte[] getEncryptedMessage(final byte[] encryptedMessageWithPrependedSalt, final int saltSize) {
        final byte[] encryptedMessage = new byte[encryptedMessageWithPrependedSalt.length - saltSize];
        System.arraycopy(encryptedMessageWithPrependedSalt, saltSize, encryptedMessage, 0, encryptedMessage.length);
        return encryptedMessage;
    }

    private String toString(final byte[] encrypted) {
        return ENCRYPTED_VALUE_PREFIX + new String(Base64.getEncoder().encode(encrypted), encryptedMessageCharset) + ENCRYPTED_VALUE_SUFFIX;
    }

    private byte[] fromString(final String encryptedMessage) {
        final String strippedMessage = getStrippedEncryptedMessage(encryptedMessage).orElseThrow(() ->
                new IllegalArgumentException("message is not encrypted!" + encryptedMessage)
        );
        return Base64.getDecoder().decode(strippedMessage.getBytes(encryptedMessageCharset));
    }

    private static Optional<String> getStrippedEncryptedMessage(final String encryptedMessage) {
        if(encryptedMessage.startsWith(ENCRYPTED_VALUE_PREFIX) && encryptedMessage.endsWith(ENCRYPTED_VALUE_SUFFIX)) {
            return Optional
                    .of(encryptedMessage)
                    .map(m -> Strings.CI.removeStart(m, ENCRYPTED_VALUE_PREFIX))
                    .map(m -> Strings.CI.removeEnd(m, ENCRYPTED_VALUE_SUFFIX));
        }
        return Optional.empty();
    }
}
