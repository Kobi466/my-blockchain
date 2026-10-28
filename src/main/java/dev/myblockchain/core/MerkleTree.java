package dev.myblockchain.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MerkleTree {
    public static String calculateMerkleRoot(
        List<Transaction> transactions
    ) {
        if (transactions == null || transactions.isEmpty()) {
            return null;
        }

        List<String> hashes = new ArrayList<>();

        //1. Hash từng transaction 1
        for (Transaction tx: transactions){
            hashes.add(tx.getTxId());
        }

        //2. Build tree từ bottom lên
        while (hashes.size() > 1) {
            List<String> newHashes = new ArrayList<>();

            for (int i = 0; i < hashes.size(); i += 2) {
                String left = hashes.get(i);
                // String right;
                // if (i + 1 < hashes.size()) {
                //     right = hashes.get(i+1);
                // } else {
                //     right = left; // Duplicate the last hash if odd number of hashes
                // }
                String right = (i+1 <  hashes.size()) ? hashes.get(i+1) : left; // Duplicate the last hash if odd number of hashes
                String combinedHash = HashUtil.sha256(left + right);
                newHashes.add(combinedHash);
            }

            hashes = newHashes;
        }
        return hashes.get(0);
    }

    public static List<MerkleProofNode> generateProof(List<Transaction> transactions, String targetTxId) {
        if(transactions == null || transactions.isEmpty() || targetTxId == null){
            return Collections.emptyList();
        }
        int index = -1;
        List<String> currentLevel = new ArrayList<>();
        for (int i = 0; i < transactions.size(); i++) {
            String txId = transactions.get(i).getTxId();
            currentLevel.add(txId);
            if(txId.equals(targetTxId)){
                index = i;
            }
        }

        if(index == -1){
            return Collections.emptyList();
        }

        List<MerkleProofNode> proof = new ArrayList<>();
        while(currentLevel.size()>1){
            int pairIndex;
            MerkleProofNode.NodePosition position;  
            if(index % 2 == 0){
                pairIndex = (index + 1) < currentLevel.size() ? index + 1 : index;
                position = MerkleProofNode.NodePosition.RIGHT;
            } else {
                pairIndex = index - 1;
                position = MerkleProofNode.NodePosition.LEFT;
            }
            proof.add(new MerkleProofNode(currentLevel.get(pairIndex), position));
            List<String> nextLevel = new ArrayList<>();
            for(int i = 0; i < currentLevel.size(); i += 2){
                String left = currentLevel.get(i);
                String right = (i + 1 < currentLevel.size()) ? currentLevel.get(i + 1) : left;
                String combinedHash = HashUtil.sha256(left + right);
                nextLevel.add(combinedHash);
            }
            currentLevel = nextLevel;
            index /= 2;
        }
        return proof;
    }

    public static boolean verifyProof(String txId
        , List<MerkleProofNode> proof
        , String merkleRoot){
        
        if(txId == null || proof == null || merkleRoot == null || proof == null){
            return false;
        }

        String computedHash = txId;
        for(MerkleProofNode node: proof){
            if(node.position() == MerkleProofNode.NodePosition.LEFT){
                computedHash = HashUtil.sha256(node.hash() + computedHash);
            } else {
                computedHash = HashUtil.sha256(computedHash + node.hash());
            }
        }
        return computedHash.equals(merkleRoot);
    }
}
