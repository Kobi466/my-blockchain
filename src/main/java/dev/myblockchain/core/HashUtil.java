package dev.myblockchain.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class HashUtil {
    private HashUtil() {

    }

    // compute the SHA-256 hash of the input data and return it as a hexadecimal string
    public static String sha256(byte[] data){
        if (data == null) {
            throw new IllegalArgumentException("Input data cannot be null");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            // StringBuilder hexString = new StringBuilder();
            // for (byte b : hash) {
            //     // Convert each byte to a two-digit hexadecimal string 
            //     // People and BlockChain enthusiasts often use this format to represent hashes in a more readable way
            //     String hex = Integer.toHexString(0xff & b);
            //     if (hex.length() == 1) hexString.append('0');
            //     hexString.append(hex);
            // }
            // return hexString.toString();
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while hashing data", e);
        }
    }

    //hepler method to compute the SHA-256 hash of a string input
    public static String sha256(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Input data cannot be null");
        }
        return sha256(data.getBytes(StandardCharsets.UTF_8));
    }
}
