package dev.myblockchain.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    @DisplayName("Tạo Transaction hợp lệ: txId phải có độ dài 64 ký tự hex và không null")
    void testValidTransactionCreation() {
        Transaction tx = new Transaction("Alice", "Bob", 100L, 1000L);

        assertNotNull(tx.getTxId());
        assertEquals(64, tx.getTxId().length());
        assertEquals("Alice", tx.getFrom());
        assertEquals("Bob", tx.getTo());
        assertEquals(100L, tx.getAmount());
    }

    @Test
    @DisplayName("Tính xác định (Determinism): Hai transaction cùng tham số phải có cùng txId")
    void testTransactionDeterminism() {
        Transaction tx1 = new Transaction("Alice", "Bob", 50L, 12345L);
        Transaction tx2 = new Transaction("Alice", "Bob", 50L, 12345L);

        assertEquals(tx1.getTxId(), tx2.getTxId());
    }

    @Test
    @DisplayName("Bảo vệ tính toàn vẹn: Thay đổi bất kỳ trường nào thì txId phải thay đổi")
    void testTamperTransaction() {
        Transaction txOriginal = new Transaction("Alice", "Bob", 50L, 1000L);
        Transaction txModified = new Transaction("Alice", "Bob", 999L, 1000L);

        assertNotEquals(txOriginal.getTxId(), txModified.getTxId());
    }

    @Test
    @DisplayName("Kiểm tra biên: Số tiền <= 0 hoặc địa chỉ rỗng/null phải ném ngoại lệ")
    void testInvalidTransactionInputs() {
        assertThrows(IllegalArgumentException.class, () -> new Transaction("Alice", "Bob", 0L));
        assertThrows(IllegalArgumentException.class, () -> new Transaction("Alice", "Bob", -10L));
        assertThrows(NullPointerException.class, () -> new Transaction(null, "Bob", 10L));
        assertThrows(NullPointerException.class, () -> new Transaction("Alice", null, 10L));
        assertThrows(IllegalArgumentException.class, () -> new Transaction("   ", "Bob", 10L));
        assertThrows(IllegalArgumentException.class, () -> new Transaction("Alice", "", 10L));
    }

    @Test
    @DisplayName("Chữ ký giao dịch hợp lệ: Ký bằng ví chính chủ và thẩm định 2 bước thành công")
    void testValidTransactionSignature() {
        System.out.println("\n=== TEST: Chữ ký giao dịch hợp lệ ===");
        Wallet sender = new Wallet();
        Wallet receiver = new Wallet();

        System.out.println("Sender Address  : " + sender.getAddress());
        System.out.println("Receiver Address: " + receiver.getAddress());
        System.out.println("Sender PubKey   : " + sender.getPublicKeyHex());

        Transaction tx = new Transaction(sender.getAddress(), receiver.getAddress(), 50L, sender.getPublicKeyHex());
        System.out.println("TxID (Hash)     : " + tx.getTxId());
        System.out.println("Chữ ký ban đầu  : " + tx.getSignature());
        
        assertNull(tx.getSignature());
        assertFalse(tx.isSignatureValid(), "Chưa ký thì không hợp lệ");
        System.out.println("-> Kiểm tra trước khi ký: isSignatureValid() = false");

        // Tiến hành ký bằng Private Key của ví gửi
        tx.sign(sender.getPrivateKey());
        System.out.println("Chữ ký sau ký   : " + tx.getSignature());

        assertNotNull(tx.getSignature());
        boolean isValid = tx.isSignatureValid();
        System.out.println("-> Kiểm tra sau khi ký  : isSignatureValid() = " + isValid);
        assertTrue(isValid, "Chữ ký hợp lệ sau khi ký với Private Key của người gửi");
    }

    @Test
    @DisplayName("Phát hiện mạo danh: Dùng Public Key của người khác không khớp địa chỉ from phải thất bại")
    void testSignatureFailsWithMismatchedPublicKey() {
        System.out.println("\n=== TEST: Phát hiện tấn công mạo danh (Impersonation Attack) ===");
        Wallet realOwner = new Wallet();
        Wallet hacker = new Wallet();
        Wallet receiver = new Wallet();

        System.out.println("Địa chỉ nạn nhân (from)  : " + realOwner.getAddress());
        System.out.println("Địa chỉ hacker           : " + hacker.getAddress());
        System.out.println("PubKey hacker đính kèm   : " + hacker.getPublicKeyHex());

        // Hacker cố tình gửi từ địa chỉ của realOwner nhưng đính kèm Public Key của hacker
        Transaction tx = new Transaction(realOwner.getAddress(), receiver.getAddress(), 50L, hacker.getPublicKeyHex());
        tx.sign(hacker.getPrivateKey());
        System.out.println("Chữ ký của hacker        : " + tx.getSignature());

        boolean isValid = tx.isSignatureValid();
        System.out.println("Địa chỉ dẫn xuất từ PubKey đính kèm: " + Wallet.calculateAddress(hacker.getPublicKey()));
        System.out.println("Khớp với 'from' (" + realOwner.getAddress() + ") không? -> FALSE");
        System.out.println("-> Kết quả thẩm định: isSignatureValid() = " + isValid);

        assertFalse(isValid, "Không khớp địa chỉ from với Public Key đính kèm phải bị từ chối");
    }

    @Test
    @DisplayName("Phát hiện làm giả chữ ký: Chữ ký giả hoặc bị sửa đổi phải bị từ chối")
    void testTamperedSignatureFails() {
        System.out.println("\n=== TEST: Phát hiện chữ ký bị sửa đổi/làm giả ===");
        Wallet sender = new Wallet();
        Wallet receiver = new Wallet();

        Transaction tx = new Transaction(sender.getAddress(), receiver.getAddress(), 50L, sender.getPublicKeyHex());
        tx.sign(sender.getPrivateKey());
        System.out.println("Chữ ký thật : " + tx.getSignature());

        String fakeSignature = "fake_signature_hex_000000000000000000000000000000000000000000000000000000";
        System.out.println("Chữ ký giả  : " + fakeSignature);

        Transaction tamperedTx = new Transaction(
                tx.getFrom(),
                tx.getTo(),
                tx.getAmount(),
                tx.getTimestamp(),
                tx.getSenderPublicKey(),
                fakeSignature
        );

        boolean isValid = tamperedTx.isSignatureValid();
        System.out.println("-> Kết quả thẩm định chữ ký giả: isSignatureValid() = " + isValid);

        assertFalse(isValid, "Chữ ký giả mạo phải bị từ chối");
    }
}
