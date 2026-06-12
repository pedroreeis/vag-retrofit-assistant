package com.vagretrofit.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    public static String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexUtils.bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Falha ao inicializar SHA-256", e);
        }
    }
}
