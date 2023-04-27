package com.webseleniumdriver.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptUtils {
    private static final String ENCRYPT_KEY = "26091996VN201501";// 128 bit key

    private static final String ENCRYPT_INIT_VECTOR = "26091996VN201501";// 16 bytes IV

    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(ENCRYPT_INIT_VECTOR.getBytes("UTF-8"));
            SecretKeySpec sKeySpec = new SecretKeySpec(ENCRYPT_KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String encrypted) {
        if (encrypted != null) {
            try {
                IvParameterSpec iv = new IvParameterSpec(ENCRYPT_INIT_VECTOR.getBytes("UTF-8"));
                SecretKeySpec sKeySpec = new SecretKeySpec(ENCRYPT_KEY.getBytes("UTF-8"), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                cipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv);

                byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
                return new String(original);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }
}
