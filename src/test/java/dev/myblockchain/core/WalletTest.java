package dev.myblockchain.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @Test
    @DisplayName("Khởi tạo Wallet mới: Địa chỉ ví phải có độ dài 64 hex và không trùng lặp")
    void testWalletCreationAndAddressGeneration() {
        Wallet wallet1 = new Wallet();
        Wallet wallet2 = new Wallet();

        System.out.println("Wallet 1: " + wallet1);
        System.out.println("Wallet 2: " + wallet2);

        assertNotNull(wallet1.getAddress());
        assertEquals(64, wallet1.getAddress().length(), "Địa chỉ ví SHA-256 phải có độ dài đúng 64 ký tự hex");
        assertNotEquals(wallet1.getAddress(), wallet2.getAddress(), "Hai ví khác nhau phải có địa chỉ khác nhau");
        assertNotEquals(wallet1.getPublicKeyHex(), wallet2.getPublicKeyHex());
    }

    @Test
    @DisplayName("Tính xác định của địa chỉ ví: Cùng một PublicKey luôn sinh ra cùng một địa chỉ")
    void testAddressDerivationDeterminism() {
        KeyPair keyPair = CryptoUtil.generateKeyPair();
        Wallet wallet1 = new Wallet(keyPair);
        Wallet wallet2 = new Wallet(keyPair);

        System.out.println("Wallet 1: " + wallet1);
        System.out.println("Wallet 2: " + wallet2);


        assertEquals(wallet1.getAddress(), wallet2.getAddress());
        assertEquals(Wallet.calculateAddress(keyPair.getPublic()), wallet1.getAddress());
    }

    @Test
    @DisplayName("Ví ký dữ liệu và xác thực thành công bằng Public Key của ví")
    void testWalletSigningAndVerification() {
        Wallet wallet = new Wallet();
        String transactionPayload = "Alice:Bob:50:123456";

        String signature = wallet.sign(transactionPayload);

        System.out.println("Transaction Payload: " + transactionPayload);
        System.out.println("Wallet Address: " + wallet.getAddress());
        System.out.println("Signature: " + signature);
        assertNotNull(signature);
        assertFalse(signature.isEmpty());

        boolean isValid = CryptoUtil.verify(wallet.getPublicKey(), transactionPayload, signature);
        System.out.println("Is signature valid? " + isValid);
        assertTrue(isValid, "Chữ ký sinh ra từ ví phải được xác thực thành công bằng Public Key của ví");
    }

    @Test
    @DisplayName("Kiểm tra tham số không hợp lệ trong Wallet")
    void testInvalidInputs() {
        assertThrows(NullPointerException.class, () -> new Wallet(null));
        assertThrows(NullPointerException.class, () -> Wallet.calculateAddress(null));
    }
}