package dev.myblockchain.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Blockchain {
    public static final int DEFAULT_DIFFICULTY = 4;
    public static final int DIFFICULTY_ADJUSTMENT_INTERVAL = 5;
    public static final long TARGET_BLOCK_TIME_MS = 1000L;
    
    private final int difficulty;
    private final List<Block> chain;

    public Blockchain() {
        this(DEFAULT_DIFFICULTY);
    }

    public Blockchain(int difficulty) {
        if (difficulty <= 0) {
            throw new IllegalArgumentException("Difficulty must be strictly positive");
        }
        this.difficulty = difficulty;
        this.chain = new ArrayList<>();
        var genesisBlock = createGenesisBlock();
        genesisBlock.mineBlock(this.difficulty);
        this.chain.add(genesisBlock);
    }

    private Block createGenesisBlock() {
        return new Block(0, System.currentTimeMillis(), "0", Collections.emptyList(), 0L, this.difficulty);
    }

    public Block getLatestBlock() {
        return this.chain.get(this.chain.size() - 1);
    }

    /**
     * Thuật toán tự điều chỉnh độ khó (DAA):
     * Sau mỗi chu kỳ DIFFICULTY_ADJUSTMENT_INTERVAL blocks, tính toán lại độ khó dựa trên thời gian thực tế.
     */
    public int getNextDifficulty(){
        Block latestBlock = getLatestBlock();
        int nextIndex = latestBlock.getIndex() + 1;
        if(nextIndex % DIFFICULTY_ADJUSTMENT_INTERVAL != 0){
            return latestBlock.getDifficulty();
        }
        Block previousAdjustmentBlock = this.chain.get(nextIndex - DIFFICULTY_ADJUSTMENT_INTERVAL);
        long actualTimeTaken = latestBlock.getTimestamp() - previousAdjustmentBlock.getTimestamp();
        // System.out.println("Actual time taken for last " + DIFFICULTY_ADJUSTMENT_INTERVAL + " blocks: " + actualTimeTaken + " ms");
        long expectedTime = TARGET_BLOCK_TIME_MS * (DIFFICULTY_ADJUSTMENT_INTERVAL - 1);
        // System.out.println("Expected time for last " + DIFFICULTY_ADJUSTMENT_INTERVAL + " blocks: " + expectedTime + " ms");
        int currentDifficulty = latestBlock.getDifficulty();
        if (actualTimeTaken < expectedTime / 2) {
            return currentDifficulty + 1;
        } else if (actualTimeTaken > expectedTime * 2) {
            return Math.max(1, currentDifficulty - 1);
        } else {
            return currentDifficulty;
        }

        // this.difficulty = nextDifficulty;
    }

    public Block minePendingTransactions(Mempool mempool, int maxBatchSize){
        Objects.requireNonNull(mempool, "Mempool cannot be null");
        if (maxBatchSize <= 0) {
            throw new IllegalArgumentException("Max batch size must be greater than zero");
        }
        List<Transaction> batch = mempool.pollBatch(maxBatchSize);
        Block latestBlock = this.getLatestBlock();
        int nextDifficulty = this.getNextDifficulty();
        Block newBlock = new Block(
            latestBlock.getIndex() + 1, 
            System.currentTimeMillis(), 
            latestBlock.getHash(), 
            batch, 
            0L, 
            nextDifficulty
        );
        newBlock.mineBlock(nextDifficulty);
        this.chain.add(newBlock);
        mempool.removeTransactions(batch);
        return newBlock;
    }

    public void addBlock(List<Transaction> transactions) {
        Block latestBlock = this.getLatestBlock();
        int nextDifficulty = this.getNextDifficulty();
        Block newBlock = new Block(
            latestBlock.getIndex() + 1, 
            System.currentTimeMillis(), 
            latestBlock.getHash(), 
            transactions,
            0L,
            nextDifficulty
        );
        newBlock.mineBlock(nextDifficulty);
        this.chain.add(newBlock);
    }

    public boolean isChainValid() {
        
        Block genesisBlock = this.chain.get(0);

        String target = "0".repeat(genesisBlock.getDifficulty());
        if (genesisBlock.getIndex() != 0 || !"0".equals(genesisBlock.getPreviousHash())) {
            return false;
        }

        if (!genesisBlock.getHash().equals(genesisBlock.calculateHash())) {
            return false;
        }

        if(!genesisBlock.getHash().startsWith(target) || genesisBlock.getDifficulty() <= 0) {
            return false;
        }


        String genesisMerkleRoot = genesisBlock.getMerkleRoot();
        if (!Objects.equals(genesisMerkleRoot, MerkleTree.calculateMerkleRoot(genesisBlock.getTransactions()))) {
            return false;
        }
        for (int i = 1; i < this.chain.size(); i++) {
            Block currentBlock = this.chain.get(i);
            Block previousBlock = this.chain.get(i - 1);

            if (currentBlock.getIndex() != previousBlock.getIndex() + 1) {
                return false;
            }

            String expectedMerkleRoot = MerkleTree.calculateMerkleRoot(currentBlock.getTransactions());
            if (!Objects.equals(currentBlock.getMerkleRoot(), expectedMerkleRoot)) {
                return false;
            }

            // Thẩm định tính toàn vẹn chữ ký mọi giao dịch trong block
            for (Transaction tx : currentBlock.getTransactions()) {
                if (!tx.isSignatureValid()) {
                    return false;
                }
            }

            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                return false;
            }

            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                return false;
            }

            // Độ khó trong cùng 1 epoch không được phép tự ý thay đổi
            if (i % DIFFICULTY_ADJUSTMENT_INTERVAL != 0 && currentBlock.getDifficulty() != previousBlock.getDifficulty()) {
                return false;
            }

            String expectedTarget = "0".repeat(currentBlock.getDifficulty());
            if (!currentBlock.getHash().startsWith(expectedTarget) || currentBlock.getDifficulty() <= 0) {
                return false;
            }
        }
        return true;
    }

    public List<Block> getChain() {
        return Collections.unmodifiableList(chain);
    }

    public int getSize() {
        return this.chain.size();
    }

    public int getDifficulty() {
        return this.difficulty;
    }
}
