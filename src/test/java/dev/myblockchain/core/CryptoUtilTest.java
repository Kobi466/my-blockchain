package dev.myblockchain.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilTest {

    @Test
    @DisplayName("Sinh cặp khóa ECDSA thành công và không trùng lặp")
    void testGenerateKeyPair() {
        KeyPair kp1 = CryptoUtil.generateKeyPair();
        KeyPair kp2 = CryptoUtil.generateKeyPair();

        assertNotNull(kp1.getPublic());
        assertNotNull(kp1.getPrivate());
        assertNotEquals(kp1.getPrivate(), kp2.getPrivate(), "Mỗi cặp khóa sinh ra phải là duy nhất");
        assertEquals("EC", kp1.getPublic().getAlgorithm());
    }

    @Test
    @DisplayName("Ký số và xác thực thành công khi dữ liệu và khóa hợp lệ")
    void testSignAndVerifySuccess() {
        KeyPair keyPair = CryptoUtil.generateKeyPair();
        String transactionData = "tx_id_12345:Alice:Bob:100L";

        String signature = CryptoUtil.sign(keyPair.getPrivate(), transactionData);

        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        System.out.println("Generated Signature Hex: " + signature);

        boolean isValid = CryptoUtil.verify(keyPair.getPublic(), transactionData, signature);
        System.out.println("Is signature valid? " + isValid);
        assertTrue(isValid, "Chữ ký số hợp lệ với đúng khóa công khai");
    }

    @Test
    @DisplayName("Phát hiện mạo danh: Dùng Public Key của người khác xác thực phải thất bại")
    void testVerifyFailsWithWrongPublicKey() {
        KeyPair alice = CryptoUtil.generateKeyPair();
        KeyPair bob = CryptoUtil.generateKeyPair();
        String transactionData = "tx_id_12345:Alice:Charlie:100L";

        // Alice ký giao dịch
        String aliceSignature = CryptoUtil.sign(alice.getPrivate(), transactionData);

        // Kẻ gian cố dùng Public Key của Bob để xác thực
        boolean isValid = CryptoUtil.verify(bob.getPublic(), transactionData, aliceSignature);
        assertFalse(isValid, "Không thể xác thực chữ ký của Alice bằng Public Key của Bob");
    }

    @Test
    @DisplayName("Phát hiện gian lận: Dữ liệu bị cạo sửa sau khi ký phải bị từ chối")
    void testVerifyFailsWithTamperedData() {
        KeyPair alice = CryptoUtil.generateKeyPair();
        String originalData = "tx_id_12345:Alice:Bob:100L";
        String signature = CryptoUtil.sign(alice.getPrivate(), originalData);

        // Kẻ tấn công sửa số tiền từ 100L thành 9999L
        String tamperedData = "tx_id_12345:Alice:Bob:9999L";

        boolean isValid = CryptoUtil.verify(alice.getPublic(), tamperedData, signature);
        assertFalse(isValid, "Dữ liệu bị sửa đổi sau khi ký bắt buộc phải thất bại");
    }

    @Test
    @DisplayName("Chữ ký giả mạo hoặc sai định dạng hex phải trả về false")
    void testInvalidSignatureFormatReturnsFalse() {
        KeyPair keyPair = CryptoUtil.generateKeyPair();
        String data = "some_data";

        assertFalse(CryptoUtil.verify(keyPair.getPublic(), data, "fake_signature_hex"));
        assertFalse(CryptoUtil.verify(keyPair.getPublic(), data, "1234567890abcdef"));
        assertFalse(CryptoUtil.verify(null, data, "1234"));
        assertFalse(CryptoUtil.verify(keyPair.getPublic(), null, "1234"));
        assertFalse(CryptoUtil.verify(keyPair.getPublic(), data, null));
    }

    @Test
    @DisplayName("Tuần tự hóa và khôi phục PublicKey / PrivateKey từ Hex")
    void testKeySerializationAndRestoration() {
        KeyPair keyPair = CryptoUtil.generateKeyPair();
        String data = "verification_data";
        String signature = CryptoUtil.sign(keyPair.getPrivate(), data);

        // 1. Kiểm tra PublicKey
        String pubHex = CryptoUtil.publicKeyToHex(keyPair.getPublic());
        PublicKey restoredPub = CryptoUtil.publicKeyFromHex(pubHex);
        assertEquals(keyPair.getPublic(), restoredPub);
        assertTrue(CryptoUtil.verify(restoredPub, data, signature), "PublicKey khôi phục từ Hex phải xác thực được chữ ký");

        // 2. Kiểm tra PrivateKey
        String privHex = CryptoUtil.privateKeyToHex(keyPair.getPrivate());
        PrivateKey restoredPriv = CryptoUtil.privateKeyFromHex(privHex);
        assertEquals(keyPair.getPrivate(), restoredPriv);

        String newSigWithRestoredKey = CryptoUtil.sign(restoredPriv, "new_data");
        assertTrue(CryptoUtil.verify(keyPair.getPublic(), "new_data", newSigWithRestoredKey));
    }
}