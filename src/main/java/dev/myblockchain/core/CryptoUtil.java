package dev.myblockchain.core;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HexFormat;

public class CryptoUtil {
    public static final String EC_ALGORITHM = "EC";
    public static final String CURVE_NAME = "secp256r1";
    public static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";

    private CryptoUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Sinh cặp khóa Elliptic Curve (ECDSA) ngẫu nhiên an toàn bằng CSPRNG.
     */
    public static KeyPair generateKeyPair(){
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(EC_ALGORITHM);
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(CURVE_NAME);
            keyGen.initialize(ecSpec, new SecureRandom());
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Error generating key pair: " + e.getMessage(), e);
        }
    }

    /**
     * Dùng Private Key để ký số lên chuỗi dữ liệu (tự động băm SHA-256 bên trong ECDSA).
     * @return Chuỗi Hex đại diện cho chữ ký số ASN.1 DER.
     */
    public static String sign(PrivateKey privateKey, String data) {
        if (privateKey == null || data == null) {
            throw new IllegalArgumentException("Private key and data cannot be null");
        }
        try {
            Signature ecdsaSign = Signature.getInstance(SIGNATURE_ALGORITHM);
            ecdsaSign.initSign(privateKey);
            ecdsaSign.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = ecdsaSign.sign();
            return HexFormat.of().formatHex(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error signing data: " + e.getMessage(), e);
        }
    }

    /**
     * Dùng Public Key để kiểm chứng tính xác thực của chữ ký số đối với dữ liệu.
     * @return true nếu chữ ký hợp lệ và dữ liệu nguyên vẹn, false nếu bị mạo danh hoặc giả mạo.
     */
    public static boolean verify(PublicKey publicKey, String data, String signatureHex) {
        if (publicKey == null || data == null || signatureHex == null) {
            return false; // Return false if any input is null
        }
        try {
            byte[] signatureBytes = HexFormat.of().parseHex(signatureHex);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            return false; // Return false if any exception occurs during verification
        }
    }

    /**
     * Tuần tự hóa PrivateKey sang chuỗi Hex chuẩn PKCS#8.
     */
    public static String privateKeyToHex(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new IllegalArgumentException("Private key cannot be null");
        }
        return HexFormat.of().formatHex(privateKey.getEncoded());
    }

    /**
     * Khôi phục PrivateKey từ chuỗi Hex chuẩn PKCS#8.
     */
    public static PrivateKey privateKeyFromHex(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("Hex string cannot be null");
        }
        try {
            byte[] keyBytes = HexFormat.of().parseHex(hex);
            KeyFactory keyFactory = KeyFactory.getInstance(EC_ALGORITHM);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new RuntimeException("Error converting hex to private key: " + e.getMessage(), e);
        }
    }

    /**
     * Tuần tự hóa PublicKey sang chuỗi Hex chuẩn X.509.
     */
    public static String publicKeyToHex(PublicKey publicKey) {
        if (publicKey == null) {
            throw new IllegalArgumentException("Public key cannot be null");
        }
        return HexFormat.of().formatHex(publicKey.getEncoded());
    }

    /**
     * Khôi phục PublicKey từ chuỗi Hex chuẩn X.509.
     */
    public static PublicKey publicKeyFromHex(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("Hex string cannot be null");
        }
        try {
            byte[] keyBytes = HexFormat.of().parseHex(hex);
            KeyFactory keyFactory = KeyFactory.getInstance(EC_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new RuntimeException("Error converting hex to public key: " + e.getMessage(), e);
        }
    }
}
