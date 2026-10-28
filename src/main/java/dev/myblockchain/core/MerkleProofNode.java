package dev.myblockchain.core;

import java.util.Objects;

public record MerkleProofNode(String hash, NodePosition position) {
    public enum NodePosition {
        LEFT,
        RIGHT
    }

    public MerkleProofNode {
        Objects.requireNonNull(hash, "Hash cannot be null");
        Objects.requireNonNull(position, "Position cannot be null");
    }
}

