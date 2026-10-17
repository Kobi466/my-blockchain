const {
  appendBlocks,
  heading1,
  heading2,
  heading3,
  callout,
  codeBlock,
  bullet,
  paragraph,
  divider,
  MODULE_PAGES
} = require('./notion-sync');

async function syncFeature1() {
  const pageId = MODULE_PAGES.core;
  console.log(`Starting Notion sync for Feature 1 (Core Module: ${pageId})...`);

  const blocks = [
    callout('Feature 1 Hoàn Thành: Core Cryptography, Merkle Tree & Block Data Structure (Java 21). Đạt 23/23 Unit Tests.', '🛡️'),

    heading2('1. Tổng quan Kiến trúc & Triết lý Thiết kế'),
    paragraph('Module Core chịu trách nhiệm thiết lập nền tảng mật mã học và cấu trúc dữ liệu cơ bản cho chuỗi khối, đảm bảo tính bất biến, toàn vẹn dữ liệu và tính xác định (determinism).'),
    bullet('Tính xác định (Determinism): SHA-256 sử dụng java.security.MessageDigest với UTF-8 encoding và bitmask 0xff & b để ngăn sign-extension trong Java.'),
    bullet('Số học tài chính: Toàn bộ Transaction Amount dùng kiểu số nguyên 64-bit (long) thay vì float/double nhằm loại bỏ hoàn toàn sai số làm tròn chuẩn IEEE 754 giữa các kiến trúc máy tính.'),
    bullet('Chống Canonical Delimiter Collision: Định danh Transaction (txId) và Block Hash sử dụng dấu phân cách tường minh ":" giữa các trường (ví dụ: from:to:amount:timestamp), ngăn chặn các trường cạnh nhau va chạm giá trị hash.'),
    bullet('Cây Merkle nhị phân (Binary Merkle Tree): Gom toàn bộ giao dịch thành một mã băm 32-byte duy nhất (merkleRoot) đặt tại Block Header, giúp việc kiểm tra và đào khối có độ phức tạp thời gian O(1). Xử lý nút lẻ bằng cách nhân bản lá cuối cùng.'),
    bullet('Phòng thủ sao chép (Defensive Copying): Sử dụng new ArrayList<>() và Collections.unmodifiableList() để ngăn chặn mọi hành vi can thiệp trái phép vào danh sách giao dịch và chuỗi khối từ bên ngoài.'),

    heading2('2. Bất biến Hệ thống (System Invariants)'),
    bullet('Invariant 1 - Tính liên kết xâu chuỗi (Chain Linkage): Mọi Block n (với n > 0) bắt buộc phải có previousHash khớp chính xác với hash của Block n-1.'),
    bullet('Invariant 2 - Khối nguyên thủy (Genesis Invariant): Genesis Block luôn có index = 0, previousHash = "0", danh sách giao dịch rỗng và hash tính toán lại phải khớp.'),
    bullet('Invariant 3 - Hiệu ứng tuyết lở (Avalanche Effect): Bất kỳ sự thay đổi dù chỉ 1 ký tự hoặc 1 bit trong Transaction nào đều làm thay đổi txId -> thay đổi Merkle Root -> thay đổi Block Hash -> làm đứt gãy chuỗi.'),

    heading2('3. Mã nguồn tiêu biểu (Key Implementations)'),
    heading3('Thuật toán tính Merkle Root (MerkleTree.java)'),
    codeBlock(`public static String calculateMerkleRoot(List<Transaction> transactions) {
    if (transactions == null || transactions.isEmpty()) return null;
    List<String> hashes = new ArrayList<>();
    for (Transaction tx : transactions) hashes.add(tx.getTxId());

    while (hashes.size() > 1) {
        List<String> newHashes = new ArrayList<>();
        for (int i = 0; i < hashes.size(); i += 2) {
            String left = hashes.get(i);
            String right = (i + 1 < hashes.size()) ? hashes.get(i + 1) : left;
            newHashes.add(HashUtil.sha256(left + right));
        }
        hashes = newHashes;
    }
    return hashes.get(0);
}`, 'java'),

    heading3('Kiểm tra tính toàn vẹn chuỗi khối (Blockchain.java)'),
    codeBlock(`public boolean isChainValid() {
    Block genesisBlock = this.chain.get(0);
    if (genesisBlock.getIndex() != 0 || !"0".equals(genesisBlock.getPreviousHash())) return false;
    if (!genesisBlock.getHash().equals(genesisBlock.calculateHash())) return false;
    if (!Objects.equals(genesisBlock.getMerkleRoot(), MerkleTree.calculateMerkleRoot(genesisBlock.getTransactions()))) return false;

    for (int i = 1; i < this.chain.size(); i++) {
        Block current = this.chain.get(i);
        Block previous = this.chain.get(i - 1);

        if (current.getIndex() != previous.getIndex() + 1) return false;
        if (!Objects.equals(current.getMerkleRoot(), MerkleTree.calculateMerkleRoot(current.getTransactions()))) return false;
        if (!current.getHash().equals(current.calculateHash())) return false;
        if (!current.getPreviousHash().equals(previous.getHash())) return false;
    }
    return true;
}`, 'java'),

    heading2('4. Bằng chứng kiểm thử (Verification & Test Evidence)'),
    callout('Kết quả kiểm thử tự động với JUnit 5: 23/23 tests PASSED (100% Success)', '✅'),
    bullet('dev.myblockchain.core.HashUtilTest: 5 tests passed (SHA-256 độ dài 64 hex, tính xác định, UTF-8, kiểm tra null).'),
    bullet('dev.myblockchain.core.MerkleTreeTest: 5 tests passed (Danh sách rỗng/null, 1 giao dịch, 3 giao dịch với lá nhân bản, tính nhạy cảm thứ tự, hiệu ứng tuyết lở).'),
    bullet('dev.myblockchain.core.TransactionTest: 4 tests passed (Tạo transaction hợp lệ, tính xác định, kiểm tra tính toàn vẹn, kiểm tra biên giá trị âm/null/rỗng).'),
    bullet('dev.myblockchain.core.BlockTest: 4 tests passed (Tạo block với Merkle root, phòng thủ sao chép defensive copy, kiểm tra biên, phát hiện giả mạo).'),
    bullet('dev.myblockchain.core.BlockchainTest: 5 tests passed (Khởi tạo Genesis block, thêm block xâu chuỗi, bảo vệ unmodifiable list, phát hiện cấy transaction giả mạo qua reflection, phát hiện đứt gãy previousHash).'),

    divider()
  ];

  await appendBlocks(pageId, blocks);
  console.log('✅ Feature 1 documentation synced to Notion successfully!');
}

syncFeature1().catch(err => {
  console.error('Failed to sync Feature 1 to Notion:', err);
  process.exit(1);
});
