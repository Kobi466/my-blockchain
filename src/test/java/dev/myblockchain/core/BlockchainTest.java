package dev.myblockchain.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlockchainTest {

    @Test
    @DisplayName("Khởi tạo Blockchain: Phải có sẵn Genesis Block và chuỗi hợp lệ")
    void testGenesisBlockCreation() {
        System.out.println("\n=== TEST: Genesis Block Creation ===");
        Blockchain blockchain = new Blockchain();

        System.out.println("Chain size      : " + blockchain.getSize());
        Block genesis = blockchain.getLatestBlock();
        System.out.println("Genesis Index   : " + genesis.getIndex());
        System.out.println("Genesis PrevHash: " + genesis.getPreviousHash());
        System.out.println("Genesis Hash    : " + genesis.getHash());
        System.out.println("Is chain valid? : " + blockchain.isChainValid());

        assertEquals(1, blockchain.getSize());
        assertEquals(0, genesis.getIndex());
        assertEquals("0", genesis.getPreviousHash());
        assertTrue(genesis.getTransactions().isEmpty());
        assertTrue(blockchain.isChainValid());
    }

    @Test
    @DisplayName("Thêm Block mới chứa Transactions: Chỉ số và mối nối previousHash phải hợp lệ")
    void testAddBlocks() {
        System.out.println("\n=== TEST: Add Blocks & Chain Link Validation ===");
        Blockchain blockchain = new Blockchain();

        Transaction tx1 = new Transaction("Alice", "Bob", 10L, 1000L);
        Transaction tx2 = new Transaction("Bob", "Charlie", 5L, 1000L);
        blockchain.addBlock(List.of(tx1, tx2));
        System.out.println("Added Block #1 with 2 txs -> Hash: " + blockchain.getLatestBlock().getHash());

        Transaction tx3 = new Transaction("Charlie", "David", 2L, 2000L);
        blockchain.addBlock(List.of(tx3));
        System.out.println("Added Block #2 with 1 tx  -> Hash: " + blockchain.getLatestBlock().getHash());

        System.out.println("Current Chain Size: " + blockchain.getSize());
        Block block2 = blockchain.getLatestBlock();
        Block block1 = blockchain.getChain().get(1);

        System.out.println("Block #2 PrevHash : " + block2.getPreviousHash());
        System.out.println("Block #1 Hash     : " + block1.getHash());
        System.out.println("Chain link match? : " + block2.getPreviousHash().equals(block1.getHash()));
        System.out.println("Is whole chain valid? " + blockchain.isChainValid());

        assertEquals(3, blockchain.getSize());
        assertEquals(2, block2.getIndex());
        assertEquals(block1.getHash(), block2.getPreviousHash());
        assertEquals(1, block2.getTransactions().size());
        assertTrue(blockchain.isChainValid());
    }

        @Test
    @DisplayName("Phát hiện giả mạo: Thay đổi giao dịch trong Block làm đứt gãy chuỗi (isChainValid == false)")
    void testTamperBlockTransactionsFailsValidation() throws Exception {
        System.out.println("\n=== TEST: Tamper Block Transactions Detection ===");
        Blockchain blockchain = new Blockchain();
        blockchain.addBlock(List.of(new Transaction("Alice", "Bob", 10L, 1000L)));
        blockchain.addBlock(List.of(new Transaction("Charlie", "David", 20L, 2000L)));

        System.out.println("Chuỗi ban đầu có 3 blocks. isChainValid: " + blockchain.isChainValid());
        assertTrue(blockchain.isChainValid(), "Chuỗi ban đầu phải hoàn toàn hợp lệ");

        // Giả lập kẻ tấn công làm giả Block #1 bằng cách thay đổi số tiền từ 10L thành 9999L
        Block originalBlock1 = blockchain.getChain().get(1);
        System.out.println("Block #1 gốc    - MerkleRoot: " + originalBlock1.getMerkleRoot());
        System.out.println("Block #1 gốc    - Block Hash: " + originalBlock1.getHash());

        Transaction hackedTx = new Transaction("Alice", "Bob", 9999L, 1000L);
        Block tamperedBlock1 = new Block(
                originalBlock1.getIndex(),
                originalBlock1.getTimestamp(),
                originalBlock1.getPreviousHash(),
                List.of(hackedTx)
        );
        System.out.println("Block #1 làm giả- MerkleRoot: " + tamperedBlock1.getMerkleRoot());
        System.out.println("Block #1 làm giả- Block Hash: " + tamperedBlock1.getHash());

        // Dùng reflection can thiệp vào danh sách chain nội bộ để giả lập dữ liệu bị hacker chỉnh sửa trên đĩa/RAM
        java.lang.reflect.Field chainField = Blockchain.class.getDeclaredField("chain");
        chainField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Block> internalChain = (List<Block>) chainField.get(blockchain);
        internalChain.set(1, tamperedBlock1);

        System.out.println("-> Đã cấy Block #1 bị làm giả vào chuỗi.");
        boolean validAfterTamper = blockchain.isChainValid();
        System.out.println("-> Kết quả kiểm tra isChainValid(): " + validAfterTamper);

        Block block2 = blockchain.getChain().get(2);
        System.out.println("-> Lý do đứt gãy: Block #2 previousHash (" + block2.getPreviousHash() 
                + ") không còn khớp với Hash của Block #1 bị sửa (" + tamperedBlock1.getHash() + ")");

        assertFalse(validAfterTamper, "Chuỗi có block bị thay đổi dữ liệu bắt buộc phải trả về false");
    }

    @Test
    @DisplayName("Phát hiện đứt gãy liên kết previousHash: Khi mối nối giữa 2 block bị sai lệch")
    void testBrokenPreviousHashLinkFailsValidation() throws Exception {
        System.out.println("\n=== TEST: Broken PreviousHash Link Detection ===");
        Blockchain blockchain = new Blockchain();
        blockchain.addBlock(List.of(new Transaction("Alice", "Bob", 10L, 1000L)));
        blockchain.addBlock(List.of(new Transaction("Bob", "Charlie", 5L, 2000L)));

        Block genesis = blockchain.getChain().get(0);
        System.out.println("Genesis Hash thực tế: " + genesis.getHash());

        // Tạo một Block #1 giả có previousHash sai ("fake_previous_hash")
        Block fakeBlock1 = new Block(1, System.currentTimeMillis(), "fake_previous_hash", List.of(new Transaction("Alice", "Bob", 10L, 1000L)));
        System.out.println("Block #1 giả trỏ previousHash về: " + fakeBlock1.getPreviousHash());

        java.lang.reflect.Field chainField = Blockchain.class.getDeclaredField("chain");
        chainField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Block> internalChain = (List<Block>) chainField.get(blockchain);
        internalChain.set(1, fakeBlock1);

        System.out.println("-> Đã cấy Block #1 có previousHash sai vào chuỗi.");
        boolean validAfterBreak = blockchain.isChainValid();
        System.out.println("-> Kết quả kiểm tra isChainValid(): " + validAfterBreak);
        System.out.println("-> Lý do đứt gãy: fakeBlock1.previousHash không khớp genesis.getHash()");

        assertFalse(validAfterBreak, "Chuỗi có previousHash không khớp bắt buộc phải trả về false");
    }



    @Test
    @DisplayName("Bảo vệ tính đóng gói: getChain() không cho phép thêm sửa xóa từ bên ngoài")
    void testUnmodifiableChain() {
        System.out.println("\n=== TEST: Unmodifiable Chain Protection ===");
        Blockchain blockchain = new Blockchain();
        Block fakeBlock = new Block(1, "fake_prev", Collections.emptyList());

        System.out.println("Attempting to inject a fake block directly into getChain()...");
        assertThrows(UnsupportedOperationException.class, () -> {
            blockchain.getChain().add(fakeBlock);
        });
        System.out.println("Blocked successfully! UnsupportedOperationException was thrown.");
    }

    @Test
    @DisplayName("Proof of Work: Block chưa được đào hoặc không đạt target độ khó phải bị từ chối")
    void testUnminedBlockFailsChainValidation() throws Exception {
        System.out.println("\n=== TEST: Unmined Block Fails Validation ===");
        Blockchain blockchain = new Blockchain(2);
        blockchain.addBlock(List.of(new Transaction("Alice", "Bob", 10L, 1000L)));

        // Tạo một block hợp lệ về hash nhưng CHƯA QUA ĐÀO (nonce = 0, hash không bắt đầu bằng "00")
        Block block1 = blockchain.getLatestBlock();
        Block unminedBlock = new Block(
                block1.getIndex() + 1,
                System.currentTimeMillis(),
                block1.getHash(),
                List.of(new Transaction("Bob", "Charlie", 5L, 2000L)),
                0L
        );

        // Giả sử hash tình cờ không bắt đầu bằng "00" (xác suất 255/256)
        if (!unminedBlock.getHash().startsWith("00")) {
            java.lang.reflect.Field chainField = Blockchain.class.getDeclaredField("chain");
            chainField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Block> internalChain = (List<Block>) chainField.get(blockchain);
            internalChain.add(unminedBlock);

            System.out.println("Cấy block chưa đào vào chuỗi -> isChainValid(): " + blockchain.isChainValid());
            assertFalse(blockchain.isChainValid(), "Chuỗi chứa block chưa thỏa mãn PoW bắt buộc phải trả về false");
        }
    }

    @Test
    @DisplayName("Khởi tạo Blockchain với độ khó tùy chỉnh")
    void testCustomDifficultyBlockchain() {
        Blockchain blockchain = new Blockchain(1);
        assertEquals(1, blockchain.getDifficulty());
        assertTrue(blockchain.getLatestBlock().getHash().startsWith("0"));
        assertTrue(blockchain.isChainValid());

        assertThrows(IllegalArgumentException.class, () -> new Blockchain(-1));
    }

    @Test
    @DisplayName("Tích hợp Mempool: minePendingTransactions đào block và dọn dẹp hàng đợi tự động")
    void testMinePendingTransactionsIntegration() {
        System.out.println("\n=== TEST: Mine Pending Transactions Integration ===");
        Blockchain blockchain = new Blockchain(4);
        Mempool mempool = new Mempool();

        Transaction tx1 = new Transaction("Alice", "Bob", 10L, 1000L);
        Transaction tx2 = new Transaction("Bob", "Charlie", 20L, 2000L);
        Transaction tx3 = new Transaction("Charlie", "David", 30L, 3000L);

        mempool.addTransaction(tx1);
        mempool.addTransaction(tx2);
        mempool.addTransaction(tx3);
        assertEquals(3, mempool.size());

        // Đào block đầu tiên với tối đa 2 giao dịch
        Block block1 = blockchain.minePendingTransactions(mempool, 2);

        System.out.println("Mined Block #1 Hash: " + block1.getHash());
        System.out.println("Mempool size after block #1: " + mempool.size());

        assertEquals(2, blockchain.getSize());
        assertEquals(2, block1.getTransactions().size());
        assertEquals(1, mempool.size(), "Mempool chỉ còn lại 1 giao dịch (tx3)");
        assertFalse(mempool.contains(tx1.getTxId()));
        assertFalse(mempool.contains(tx2.getTxId()));
        assertTrue(mempool.contains(tx3.getTxId()));
        assertTrue(blockchain.isChainValid());

        // Đào tiếp block thứ 2 lấy nốt giao dịch còn lại
        Block block2 = blockchain.minePendingTransactions(mempool, 2);
        assertEquals(3, blockchain.getSize());
        assertEquals(1, block2.getTransactions().size());
        assertEquals(0, mempool.size(), "Mempool phải hoàn toàn trống rỗng");
        assertTrue(blockchain.isChainValid());

        System.out.println("Mined Block #2 Hash: " + block2.getHash());
        System.out.println("Mempool size after block #2: " + mempool.size());
        System.out.println("Is whole chain valid? " + blockchain.isChainValid());
        System.out.println("Final Chain Size: " + blockchain.getSize());
        System.out.println("Get Chain: " + blockchain.getChain());
    }

    @Test
    @DisplayName("Kiểm tra tham số không hợp lệ trong minePendingTransactions")
    void testMinePendingTransactionsInvalidInputs() {
        Blockchain blockchain = new Blockchain(2);
        Mempool mempool = new Mempool();

        assertThrows(NullPointerException.class, () -> blockchain.minePendingTransactions(null, 10));
        assertThrows(IllegalArgumentException.class, () -> blockchain.minePendingTransactions(mempool, 0));
        assertThrows(IllegalArgumentException.class, () -> blockchain.minePendingTransactions(mempool, -1));
    }
}
