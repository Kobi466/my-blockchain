package dev.myblockchain.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlockTest {

    @Test
    @DisplayName("Khởi tạo Block với danh sách Transactions: Hash và MerkleRoot phải được tính toán chính xác")
    void testBlockCreationWithTransactions() {
        System.out.println("\n=== TEST: Block Creation with Transactions ===");
        Transaction tx1 = new Transaction("Alice", "Bob", 50L, 1000L);
        Transaction tx2 = new Transaction("Bob", "Charlie", 25L, 1000L);
        List<Transaction> txs = List.of(tx1, tx2);

        Block block = new Block(1, 1000L, "prev_hash_123", txs);

        System.out.println("Block Index       : " + block.getIndex());
        System.out.println("Block PreviousHash: " + block.getPreviousHash());
        System.out.println("Block MerkleRoot  : " + block.getMerkleRoot());
        System.out.println("Block Hash        : " + block.getHash());
        System.out.println("Number of TXs     : " + block.getTransactions().size());

        assertNotNull(block.getHash());
        assertEquals(64, block.getHash().length());
        assertEquals(block.getHash(), block.calculateHash());
        assertEquals(MerkleTree.calculateMerkleRoot(txs), block.getMerkleRoot());
        assertEquals(2, block.getTransactions().size());
    }

    @Test
    @DisplayName("Phát hiện làm giả giao dịch: Sửa đổi giao dịch làm thay đổi MerkleRoot và Block Hash")
    void testTamperTransactionChangesBlockHash() {
        System.out.println("\n=== TEST: Tamper Transaction Changes Block Hash ===");
        Transaction tx1 = new Transaction("Alice", "Bob", 50L, 1000L);
        Block originalBlock = new Block(1, 1000L, "prev_hash_123", List.of(tx1));

        Transaction tamperedTx = new Transaction("Alice", "Bob", 999L, 1000L);
        Block tamperedBlock = new Block(1, 1000L, "prev_hash_123", List.of(tamperedTx));

        System.out.println("Original Block MerkleRoot: " + originalBlock.getMerkleRoot());
        System.out.println("Tampered Block MerkleRoot: " + tamperedBlock.getMerkleRoot());
        System.out.println("Original Block Hash      : " + originalBlock.getHash());
        System.out.println("Tampered Block Hash      : " + tamperedBlock.getHash());

        assertNotEquals(originalBlock.getMerkleRoot(), tamperedBlock.getMerkleRoot());
        assertNotEquals(originalBlock.getHash(), tamperedBlock.getHash());
    }

    @Test
    @DisplayName("Bảo vệ tính bất biến (Immutability): Thay đổi list ban đầu không làm ảnh hưởng Block")
    void testDefensiveCopy() {
        System.out.println("\n=== TEST: Defensive Copy ===");
        List<Transaction> mutableList = new ArrayList<>();
        mutableList.add(new Transaction("Alice", "Bob", 10L, 1000L));

        Block block = new Block(1, 1000L, "prev_hash_123", mutableList);
        String originalRoot = block.getMerkleRoot();
        System.out.println("Initial TX count in Block: " + block.getTransactions().size());
        System.out.println("Initial Merkle Root      : " + originalRoot);

        // Kẻ tấn công cố tình thêm phần tử vào list gốc bên ngoài
        mutableList.add(new Transaction("Hacker", "Victim", 1000L, 1000L));
        System.out.println("External list size after hack attempt: " + mutableList.size());
        System.out.println("Block TX count after hack attempt    : " + block.getTransactions().size());

        // Block bên trong không được phép thay đổi
        assertEquals(1, block.getTransactions().size());
        assertEquals(originalRoot, block.getMerkleRoot());
    }

    @Test
    @DisplayName("Kiểm tra tham số không hợp lệ")
    void testInvalidInputs() {
        System.out.println("\n=== TEST: Invalid Inputs ===");
        System.out.println("Checking negative index throws IllegalArgumentException...");
        assertThrows(IllegalArgumentException.class, () -> new Block(-1, "0", List.of()));

        System.out.println("Checking null previousHash throws NullPointerException...");
        assertThrows(NullPointerException.class, () -> new Block(0, null, List.of()));
        System.out.println("Guard clauses passed successfully.");
    }
}
