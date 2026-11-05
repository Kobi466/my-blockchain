package dev.myblockchain.core;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;

public class Transaction {
    private final String from;
    private final String to;
    private final long amount;
    private final long timestamp;
    private final String txId;
    private String signature;
    private final String senderPublicKey;
    public static final String COINBASE_SENDER = "COINBASE";

    public Transaction(String from, String to, long amount) {
        this(from, to, amount, System.currentTimeMillis(), null, null);
    }

    public Transaction(String from, String to, long amount, long timestamp) {
        this(from, to, amount, timestamp, null, null);
    }

    public Transaction(String from, String to, long amount, String senderPublicKey) {
        this(from, to, amount, System.currentTimeMillis(), senderPublicKey, null);
    }

    public Transaction(String from, String to, long amount, long timestamp, String senderPublicKey) {
        this(from, to, amount, timestamp, senderPublicKey, null);
    }

    public Transaction(String from, String to, long amount, long timestamp, String senderPublicKey, String signature) {
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
        this.senderPublicKey = senderPublicKey;
        this.signature = signature;
        this.txId = calculateTxId();
    }

    private String calculateTxId() {
        String data = from+ ":" + to + ":" + amount + ":" + timestamp;
        return HashUtil.sha256(data);
    }

    public void sign(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new IllegalArgumentException("Private key cannot be null");
        }
        this.signature = CryptoUtil.sign(privateKey, this.txId);
    }

    public boolean isSignatureValid() {
        // If the transaction is a coinbase transaction, it doesn't require a signature
        if (COINBASE_SENDER.equals(this.from)) {
            return "COINBASE_TX".equals(this.signature);
        }
        if (signature == null || senderPublicKey == null) {
            return false;
        }
        try {
            PublicKey publicKey = CryptoUtil.publicKeyFromHex(this.senderPublicKey);
            String drivedAddress = Wallet.calculateAddress(publicKey);
            if (!drivedAddress.equals(this.from)) {
                return false; // The sender's public key does not match the 'from' address
            }
            return CryptoUtil.verify(publicKey, this.txId, this.signature);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tạo giao dịch Coinbase đúc coin thưởng cho thợ đào (không cần người gửi).
     * @param to Địa chỉ ví của thợ đào nhận thưởng.
     * @param reward Số lượng coin thưởng khối (phải > 0).
     */
    public static Transaction createCoinbase(String to, long reward){
        return new Transaction(
            COINBASE_SENDER,
            to,
            reward,
            System.currentTimeMillis(),
            null,
            "COINBASE_TX"
        );
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

    public String getSignature() {
        return signature;
    }

    public String getSenderPublicKey() {
        return senderPublicKey;
    }

    @Override
    public String toString() {
        return String.format("Transaction{from='%s', to='%s', amount=%d, timestamp=%d, txId='%s', senderPublicKey='%s', signature='%s'}",
                from, to, amount, timestamp, txId, senderPublicKey, signature);
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
