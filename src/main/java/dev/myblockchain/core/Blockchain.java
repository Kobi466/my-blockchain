package dev.myblockchain.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Blockchain {
    private final List<Block> chain;

    public Blockchain() {
        this.chain = new ArrayList<>();
        this.chain.add(this.createGenesisBlock());
    }

    private Block createGenesisBlock() {
        return new Block(0, System.currentTimeMillis(), "0", Collections.emptyList());
    }

    public Block getLatestBlock() {
        return this.chain.get(this.chain.size() - 1);
    }

    public void addBlock(List<Transaction> transactions) {
        Block latestBlock = this.getLatestBlock();
        Block newBlock = new Block(latestBlock.getIndex() + 1, System.currentTimeMillis(), latestBlock.getHash(), transactions);
        this.chain.add(newBlock);
    }

    public boolean isChainValid() {
        Block genesisBlock = this.chain.get(0);
        if (genesisBlock.getIndex() != 0 || !"0".equals(genesisBlock.getPreviousHash())) {
            return false;
        }
        if (!genesisBlock.getHash().equals(genesisBlock.calculateHash())) {
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

            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                return false;
            }

            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
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
}
