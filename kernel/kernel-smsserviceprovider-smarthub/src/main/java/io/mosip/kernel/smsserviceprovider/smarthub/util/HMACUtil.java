package io.mosip.kernel.smsserviceprovider.smarthub.util;
import org.apache.commons.codec.digest.HmacUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HMACUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    public static String generateHash(String algorithm, String data, String key) {
        return new HmacUtils(algorithm, key).hmacHex(data);
    }

    public static String generateHMACSHA256Hash( String data, String key) {
        return new HmacUtils(HMAC_SHA256, key).hmacHex(data);
    }

    public static String generateHMAC(String algorithm, String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);
        Mac mac = Mac.getInstance(algorithm);
        mac.init(secretKeySpec);
        return bytesToHex(mac.doFinal(data.getBytes()));
    }
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte h : hash) {
            String hex = Integer.toHexString(0xff & h);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}