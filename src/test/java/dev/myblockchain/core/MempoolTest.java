package dev.myblockchain.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MempoolTest {

    @Test
    @DisplayName("Thêm giao dịch hợp lệ vào Mempool và duy trì thứ tự FIFO")
    void testAddTransactionAndFifoOrder() {
        Mempool mempool = new Mempool();
        Transaction tx1 = new Transaction("Alice", "Bob", 10L, 1000L);
        Transaction tx2 = new Transaction("Bob", "Charlie", 20L, 2000L);

        assertTrue(mempool.addTransaction(tx1));
        assertTrue(mempool.addTransaction(tx2));
        assertEquals(2, mempool.size());
        assertTrue(mempool.contains(tx1.getTxId()));
        assertTrue(mempool.contains(tx2.getTxId()));

        List<Transaction> all = mempool.getAll();
        assertEquals(tx1.getTxId(), all.get(0).getTxId(), "Giao dịch đến trước phải nằm trước");
        assertEquals(tx2.getTxId(), all.get(1).getTxId());
    }

    @Test
    @DisplayName("Từ chối giao dịch trùng lặp (Idempotent)")
    void testRejectDuplicateTransaction() {
        Mempool mempool = new Mempool();
        Transaction tx1 = new Transaction("Alice", "Bob", 10L, 1000L);

        assertTrue(mempool.addTransaction(tx1));
        assertFalse(mempool.addTransaction(tx1), "Thêm lần 2 phải trả về false");
        assertEquals(1, mempool.size());
    }

    @Test
    @DisplayName("Kiểm tra tham số không hợp lệ")
    void testInvalidInputs() {
        Mempool mempool = new Mempool();
        assertThrows(IllegalArgumentException.class, () -> mempool.addTransaction(null));
        assertThrows(IllegalArgumentException.class, () -> new Mempool(0));
        assertThrows(IllegalArgumentException.class, () -> new Mempool(-5));
    }

    @Test
    @DisplayName("Bảo vệ chống DoS/OOM: Ném ngoại lệ khi Mempool vượt quá dung lượng tối đa")
    void testCapacityLimitThrowsException() {
        Mempool mempool = new Mempool(2); // Giới hạn chỉ chứa 2 tx
        Transaction tx1 = new Transaction("Alice", "Bob", 10L, 1000L);
        Transaction tx2 = new Transaction("Bob", "Charlie", 20L, 2000L);
        Transaction tx3 = new Transaction("Charlie", "David", 30L, 3000L);

        assertTrue(mempool.addTransaction(tx1));
        assertTrue(mempool.addTransaction(tx2));
        assertEquals(2, mempool.size());

        assertThrows(IllegalStateException.class, () -> mempool.addTransaction(tx3));
        assertEquals(2, mempool.size());
    }

    @Test
    @DisplayName("pollBatch lấy đúng số lượng giao dịch và không xóa khỏi Mempool")
    void testPollBatchDoesNotRemove() {
        Mempool mempool = new Mempool();
        Transaction tx1 = new Transaction("Alice", "Bob", 10L, 1000L);
        Transaction tx2 = new Transaction("Bob", "Charlie", 20L, 2000L);
        Transaction tx3 = new Transaction("Charlie", "David", 30L, 3000L);

        mempool.addTransaction(tx1);
        mempool.addTransaction(tx2);
        mempool.addTransaction(tx3);

        List<Transaction> batch = mempool.pollBatch(2);
        assertEquals(2, batch.size());
        assertEquals(tx1.getTxId(), batch.get(0).getTxId());
        assertEquals(tx2.getTxId(), batch.get(1).getTxId());
        assertEquals(3, mempool.size(), "pollBatch không được phép làm giảm size của Mempool");

        assertEquals(0, mempool.pollBatch(0).size());
    }

    @Test
    @DisplayName("removeTransactions dọn dẹp chính xác các giao dịch đã lên chuỗi")
    void testRemoveTransactions() {
        Mempool mempool = new Mempool();
        Transaction tx1 = new Transaction("Alice", "Bob", 10L, 1000L);
        Transaction tx2 = new Transaction("Bob", "Charlie", 20L, 2000L);
        Transaction tx3 = new Transaction("Charlie", "David", 30L, 3000L);

        mempool.addTransaction(tx1);
        mempool.addTransaction(tx2);
        mempool.addTransaction(tx3);

        mempool.removeTransactions(List.of(tx1, tx3));

        assertEquals(1, mempool.size());
        assertFalse(mempool.contains(tx1.getTxId()));
        assertTrue(mempool.contains(tx2.getTxId()));
        assertFalse(mempool.contains(tx3.getTxId()));
    }
}