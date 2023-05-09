package org.coodex.openai.api.server.util;

import org.coodex.openai.api.server.model.EncryptResult;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class EncryptHelper {
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final SecureRandom random = new SecureRandom();
    public static byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        random.nextBytes(iv);
        return iv;
    }
    public static byte[] generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE);
        return keyGenerator.generateKey().getEncoded();
    }
    public static EncryptResult encrypt(byte[] plainText) throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException {
        EncryptResult result = new EncryptResult();
        result.setKey(generateKey());
        result.setIv(generateIV());
        result.setCipherText(encrypt(plainText, result.getKey(), result.getIv()));
        return result;
    }

    public static byte[] encrypt(byte[] plainText, byte[] key, byte[] iv) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
        return cipher.doFinal(plainText);
    }

    public static byte[] decrypt(byte[] cipherText, byte[] key, byte[] iv) throws IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException,
            NoSuchAlgorithmException {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);
        return cipher.doFinal(cipherText);
    }
}
