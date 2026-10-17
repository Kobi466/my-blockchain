package dev.myblockchain.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Block {

    private final int index;
    private final long timestamp;
    private final String previousHash;
    private final List<Transaction> transactions;
    private final String merkleRoot;
    private final String hash;

    public Block(int index, String previousHash, List<Transaction> transactions) {
        this(index, System.currentTimeMillis(), previousHash, transactions);
    }

    public Block(int index, long timestamp, String previousHash, List<Transaction> transactions) {
        if (index < 0) {
            throw new IllegalArgumentException("Block index cannot be negative");
        }
        this.index = index;
        this.timestamp = timestamp;
        this.previousHash = Objects.requireNonNull(previousHash, "previousHash cannot be null");
        List<Transaction> txList = (transactions == null) ? Collections.emptyList() : new ArrayList<>(transactions);
        this.transactions = Collections.unmodifiableList(txList);
        this.merkleRoot = MerkleTree.calculateMerkleRoot(txList);
        this.hash = calculateHash();
    }

    
    public String calculateHash() {
        String rawData = index + ":" + previousHash + ":" + timestamp + ":" + (merkleRoot == null ? "" : merkleRoot);
        return HashUtil.sha256(rawData);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public int getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return Objects.equals(hash, block.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}
