package dev.myblockchain.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Mempool {
    private final static int DEFAULT_MAX_CAPACITY = 1000;

    private final Map<String, Transaction> pool;
    private final int maxCapacity;

    public Mempool() {
        this(DEFAULT_MAX_CAPACITY);
    }

    public Mempool(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Max capacity must be greater than zero");
        }
        this.maxCapacity = maxCapacity;
        this.pool = new LinkedHashMap<>();    
    }

    /**
     * Thêm giao dịch vào hàng đợi chờ xử lý.
     * @return true nếu thêm mới thành công, false nếu giao dịch đã tồn tại.
     * @throws IllegalStateException nếu Mempool đã đầy (bảo vệ chống DoS/OOM).
     */
    public synchronized boolean addTransaction(Transaction tx){
        if (tx == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        if(!tx.isSignatureValid()){
            return false;
        }
        if (pool.size() >= maxCapacity) {
            throw new IllegalStateException("Mempool is full."+" Current size: " + pool.size() + ", Max capacity: " + maxCapacity);
        }
        if (pool.containsKey(tx.getTxId())) {
            return false; // Transaction already exists
        }
        pool.put(tx.getTxId(), tx);
        return true;
    }

    public synchronized List<Transaction> peekBatch(int maxBatchSize) {
        if (maxBatchSize <= 0) {
            return Collections.emptyList();
        }
        return pool.values().stream()
            .limit(maxBatchSize)
            .collect(Collectors.toUnmodifiableList());
    }

    public synchronized List<Transaction> pollBatch(int maxBatchSize) {
        List<Transaction> batch = peekBatch(maxBatchSize);
        removeTransactions(batch);
        return batch;
    }

    public synchronized void removeTransactions(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        for (Transaction tx : transactions) {
            pool.remove(tx.getTxId());
        }
    }

    public synchronized boolean contains(String txId) {
        return pool.containsKey(txId);
    }

    public synchronized int size() {
        return pool.size();
    }

    public synchronized void clear() {
        pool.clear();
    }

    public synchronized List<Transaction> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(pool.values()));
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

}
