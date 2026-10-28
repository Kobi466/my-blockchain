package dev.myblockchain.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MerkleTreeTest {

    @Test
    @DisplayName("Danh sách rỗng hoặc null phải trả về null")
    void testEmptyOrNullList() {
        System.out.println("\n=== TEST: Empty or Null ===");
        String rootNull = MerkleTree.calculateMerkleRoot(null);
        String rootEmpty = MerkleTree.calculateMerkleRoot(List.of());

        System.out.println("Root (null input) : " + rootNull);
        System.out.println("Root (empty input): " + rootEmpty);

        assertNull(rootNull);
        assertNull(rootEmpty);
    }

    @Test
    @DisplayName("Chỉ có 1 transaction: Merkle root chính là txId của transaction đó")
    void testSingleTransaction() {
        System.out.println("\n=== TEST: Single Transaction ===");
        Transaction tx = new Transaction("Alice", "Bob", 100L, 1000L);
        System.out.println("Tx ID      : " + tx.getTxId());

        String root = MerkleTree.calculateMerkleRoot(List.of(tx));
        System.out.println("Merkle Root: " + root);

        assertEquals(tx.getTxId(), root);
    }

    @Test
    @DisplayName("Số lượng giao dịch lẻ (3 giao dịch): Giao dịch thứ 3 phải được nhân bản để băm")
    void testOddTransactions() {
        System.out.println("\n=== TEST: Odd Transactions (3 txs) ===");
        Transaction tx1 = new Transaction("Alice", "Bob", 10L, 1000L);
        Transaction tx2 = new Transaction("Bob", "Charlie", 20L, 1000L);
        Transaction tx3 = new Transaction("Charlie", "David", 30L, 1000L);

        System.out.println("Tx1 Hash: " + tx1.getTxId());
        System.out.println("Tx2 Hash: " + tx2.getTxId());
        System.out.println("Tx3 Hash: " + tx3.getTxId());

        // Mô phỏng tính bằng tay để quan sát:
        String h12 = HashUtil.sha256(tx1.getTxId() + tx2.getTxId());
        String h33 = HashUtil.sha256(tx3.getTxId() + tx3.getTxId()); // Tx3 nhân đôi
        String expectedRoot = HashUtil.sha256(h12 + h33);

        System.out.println("Level 1 -> H(1+2): " + h12);
        System.out.println("Level 1 -> H(3+3): " + h33 + " (Tx3 duplicated)");

        String root = MerkleTree.calculateMerkleRoot(List.of(tx1, tx2, tx3));
        System.out.println("Final Merkle Root: " + root);
        System.out.println("Expected Merkle Root: " + expectedRoot);
        assertEquals(expectedRoot, root);
        assertEquals(64, root.length());
    }

    @Test
    @DisplayName("Đảo thứ tự giao dịch phải sinh ra Merkle Root khác nhau")
    void testOrderSensitivity() {
        System.out.println("\n=== TEST: Order Sensitivity ===");
        Transaction tx1 = new Transaction("Alice", "Bob", 10L, 1000L);
        Transaction tx2 = new Transaction("Bob", "Charlie", 20L, 1000L);

        String root1 = MerkleTree.calculateMerkleRoot(List.of(tx1, tx2));
        String root2 = MerkleTree.calculateMerkleRoot(List.of(tx2, tx1));

        System.out.println("Order [Tx1, Tx2] Root: " + root1);
        System.out.println("Order [Tx2, Tx1] Root: " + root2);
        System.out.println("Hai root có khác nhau không? -> " + (!root1.equals(root2)));

        assertNotEquals(root1, root2);
    }

    @Test
    @DisplayName("Thay đổi dữ liệu 1 giao dịch làm thay đổi toàn bộ Merkle Root")
    void testTamperTransactionChangesRoot() {
        System.out.println("\n=== TEST: Tamper Data Avalanche Effect ===");
        Transaction tx1 = new Transaction("Alice", "Bob", 10L, 1000L);
        Transaction tx2 = new Transaction("Bob", "Charlie", 20L, 1000L);
        String originalRoot = MerkleTree.calculateMerkleRoot(List.of(tx1, tx2));

        // Kẻ tấn công sửa amount từ 20L thành 999L:
        Transaction tamperedTx2 = new Transaction("Bob", "Charlie", 999L, 1000L);
        String tamperedRoot = MerkleTree.calculateMerkleRoot(List.of(tx1, tamperedTx2));

        System.out.println("Original Root (20L) : " + originalRoot);
        System.out.println("Tampered Root (999L): " + tamperedRoot);

        assertNotEquals(originalRoot, tamperedRoot);
    }

    @Test
    @DisplayName("Xác thực SPV thành công cho tất cả giao dịch trong cây lẻ và chẵn")
    void testMerkleProofVerificationSuccess() {
        Transaction tx1 = new Transaction("Alice", "Bob", 10L, 1000L);
        Transaction tx2 = new Transaction("Bob", "Charlie", 20L, 1000L);
        Transaction tx3 = new Transaction("Charlie", "David", 30L, 1000L);
        List<Transaction> txs = List.of(tx1, tx2, tx3);

        String root = MerkleTree.calculateMerkleRoot(txs);
        System.out.println("Merkle Root: " + root);

        for (Transaction tx : txs) {
            List<MerkleProofNode> proof = MerkleTree.generateProof(txs, tx.getTxId());
            System.out.println("Proof for Tx ID " + tx.getTxId() + ": " + proof);
            assertFalse(proof.isEmpty());
            boolean isValid = MerkleTree.verifyProof(tx.getTxId(), proof, root);
            System.out.println("Is proof valid for Tx ID " + tx.getTxId() + "? -> " + isValid);
            assertTrue(isValid, "Proof phải hợp lệ cho giao dịch: " + tx.getTxId());
        }
    }

    @Test
    @DisplayName("Giao dịch không tồn tại trong danh sách phải trả về proof rỗng và xác thực thất bại")
    void testMerkleProofNonExistentTransaction() {
        Transaction tx1 = new Transaction("Alice", "Bob", 10L, 1000L);
        Transaction tx2 = new Transaction("Bob", "Charlie", 20L, 1000L);
        List<Transaction> txs = List.of(tx1, tx2);
        String root = MerkleTree.calculateMerkleRoot(txs);

        List<MerkleProofNode> proof = MerkleTree.generateProof(txs, "non_existent_tx_id");
        System.out.println("Proof for non-existent Tx ID: " + proof);
        assertTrue(proof.isEmpty());

        boolean isValid = MerkleTree.verifyProof("non_existent_tx_id", proof, root);
        System.out.println("Is proof valid for non-existent Tx ID? -> " + isValid);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Proof bị làm giả hoặc sai Merkle Root phải bị từ chối")
    void testTamperedMerkleProofFails() {
        Transaction tx1 = new Transaction("Alice", "Bob", 10L, 1000L);
        Transaction tx2 = new Transaction("Bob", "Charlie", 20L, 1000L);
        List<Transaction> txs = List.of(tx1, tx2);
        String root = MerkleTree.calculateMerkleRoot(txs);
        System.out.println("Merkle Root: " + root);

        List<MerkleProofNode> proof = MerkleTree.generateProof(txs, tx1.getTxId());
        System.out.println("Original Proof for Tx ID " + tx1.getTxId() + ": " + proof);

        // Kẻ tấn công sửa hash trong proof
        List<MerkleProofNode> fakeProof = List.of(
            new MerkleProofNode("fake_sibling_hash_000000000000000000000000000000000000000000000", MerkleProofNode.NodePosition.RIGHT)
        );

        System.out.println("Fake Proof for Tx ID " + tx1.getTxId() + ": " + fakeProof);
        assertFalse(MerkleTree.verifyProof(tx1.getTxId(), fakeProof, root));
        assertFalse(MerkleTree.verifyProof(tx1.getTxId(), proof, "fake_merkle_root"));
    }
}
