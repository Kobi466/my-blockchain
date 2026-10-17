package dev.myblockchain.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HashUtilTest {

    @Test
    @DisplayName("Băm chuỗi rỗng phải trả về hash rỗng chuẩn của SHA-256")
    void testEmptyString() {
        String expectedEmptyHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        assertEquals(expectedEmptyHash, HashUtil.sha256(""));
    }

    @Test
    @DisplayName("Băm test vector chuẩn NIST 'abc'")
    void testNistVector() {
        String expectedHash = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
        assertEquals(expectedHash, HashUtil.sha256("abc"));
    }

    @Test
    @DisplayName("Băm cùng một chuỗi phải luôn trả về cùng một hash")
    void testDeterminism() {
        String input = "Deterministic Test";
        String hash1 = HashUtil.sha256(input);
        String hash2 = HashUtil.sha256(input);
        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length());
    }

    @Test
    @DisplayName("Hiệu ứng thác đổ: Đổi 1 ký tự tạo ra hash hoàn toàn khác")
    void testAvalancheEffect() {
        String hash1 = HashUtil.sha256("Avalanche Test 1");
        String hash2 = HashUtil.sha256("Avalanche Test 2");
        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Băm null phải ném ra IllegalArgumentException")
    void testNullInputThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> HashUtil.sha256((String) null));
        assertThrows(IllegalArgumentException.class, () -> HashUtil.sha256((byte[]) null));
    }
}
