package dev.myblockchain.core;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;

public class Wallet {
    private final KeyPair keyPair;
    private final String address;

    public Wallet() {
        this(CryptoUtil.generateKeyPair());
    }

    public Wallet(KeyPair keyPair) {
        this.keyPair = Objects.requireNonNull(keyPair, "KeyPair cannot be null");
        this.address = calculateAddress(this.keyPair.getPublic());
    }

    /**
     * Dẫn xuất địa chỉ ví công khai từ Public Key bằng cách băm SHA-256 chzuỗi Hex X.509.
     */
    public static String calculateAddress(PublicKey publicKey) {
        Objects.requireNonNull(publicKey, "Public key cannot be null");
        String publicKeyHex = CryptoUtil.publicKeyToHex(publicKey);
        return HashUtil.sha256(publicKeyHex);
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public String getAddress() {
        return address;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public String getPublicKeyHex() {
        return CryptoUtil.publicKeyToHex(keyPair.getPublic());
    }

    public String sign(String data) {
        return CryptoUtil.sign(keyPair.getPrivate(), data);
    }

    @Override 
    public String toString() {
        return "Wallet{" +
                "address='" + address + '\'' +
                ", publicKey=" + getPublicKeyHex() +
                '}';
    }
}
