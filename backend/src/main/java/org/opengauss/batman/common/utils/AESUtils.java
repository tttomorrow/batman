package org.opengauss.batman.common.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Random;
import java.util.UUID;


public class AESUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AESUtils.class);

    private static final String ALGORITHM = "AES";

    private static final String AES_CBC_NOPADDING = "AES/CBC/NoPadding";

    private static final String CANDIDATE_CODE = "abcdefghijklmnopqrstuvwxyz0123456789";

    private static final int IV_LENGTH = 16;

    public static String encrypt(String data, String key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_NOPADDING);
            int blockSize = cipher.getBlockSize();

            byte[] dataBytes = data.getBytes();
            int plainTextLen = dataBytes.length;
            if (plainTextLen % blockSize != 0) {
                plainTextLen = plainTextLen + (blockSize - (plainTextLen % blockSize));
            }

            byte[] plaintext = new byte[plainTextLen];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plaintext);	// 加密
            String str = new String(new Base64().encode(encrypted));
            return str.trim();
        } catch (Exception e) {
            LOGGER.error("Encrypt exception: {}", ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public static String decrypt(String data, String key, String iv) {
        try {
            byte[] encryptedBytes = new Base64().decode(data);

            Cipher cipher = Cipher.getInstance(AES_CBC_NOPADDING);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] plainBytes = cipher.doFinal(encryptedBytes);
            String plainText = new String(plainBytes);
            return plainText.trim();
        } catch (Exception e) {
            LOGGER.error("Decrypt exception: {}", ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public static String genEncryptKey() {
        int hashCode = UUID.randomUUID().hashCode();
        if (hashCode < 0){
            hashCode = -hashCode;
        }
        return String.format("%016d", hashCode);
    }

    public static String genEncryptIV(){
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < IV_LENGTH; i++){
            sb.append(CANDIDATE_CODE.charAt(random.nextInt(CANDIDATE_CODE.length())));
        }
        return sb.toString();
    }
}