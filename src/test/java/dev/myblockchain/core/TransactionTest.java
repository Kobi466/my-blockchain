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
}
