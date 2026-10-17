package dev.myblockchain.core;

import java.util.ArrayList;
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
}
