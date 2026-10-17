package dev.myblockchain.core;

import java.util.Objects;

public class Transaction {
    private final String from;
    private final String to;
    private final long amount;
    private final long timestamp;
    private final String txId;

    public Transaction(String from, String to, long amount) {
        this(from, to, amount, System.currentTimeMillis());
    }

    public Transaction(String from, String to, long amount, long timestamp) {
        this.from = Objects.requireNonNull(from, "Sender (from) cannot be null").trim();
        this.to = Objects.requireNonNull(to, "Receiver (to) cannot be null").trim();

        if (this.from.isEmpty() || this.to.isEmpty()) {
            throw new IllegalArgumentException("Sender and receiver addresses cannot be empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be strictly positive");
        }

        this.amount = amount;
        this.timestamp = timestamp;
        this.txId = calculateTxId();
    }

    private String calculateTxId() {
        String data = from+ ":" + to + ":" + amount + ":" + timestamp;
        return HashUtil.sha256(data);
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public long getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTxId() {
        return txId;
    }

    @Override
    public String toString() {
        return String.format("Transaction{from='%s', to='%s', amount=%d, timestamp=%d, txId='%s'}", from, to, amount, timestamp, txId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(txId, that.txId);
}

    @Override
    public int hashCode() {
        return Objects.hash(txId);
    }
}
