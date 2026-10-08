import java.nio.file.*;
import java.security.SecureRandom;

/**
 * TripleDESTest — Automated test suite for the Triple DES Image Encryption project.
 *
 * Tests:
 *  1. Key generation
 *  2. Encrypt → Decrypt byte-level round-trip
 *  3. Different plaintexts produce different ciphertexts
 *  4. Same plaintext, different keys → different ciphertext
 *  5. File-level round-trip (simulated image bytes)
 *  6. Wrong key → decryption failure
 *  7. Large data performance test
 */
public class TripleDESTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        printHeader("Triple DES Image Encryption — Test Suite");

        test1_KeyGeneration();
        test2_EncryptDecryptRoundTrip();
        test3_DifferentPlaintexts();
        test4_SameDataDifferentKeys();
        test5_FileLevelRoundTrip();
        test6_WrongKeyFails();
        test7_LargeDataPerformance();

        printSummary();
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    static void test1_KeyGeneration() throws Exception {
        printTest("1. Key Generation");
        TripleDESCipher c1 = new TripleDESCipher();
        TripleDESCipher c2 = new TripleDESCipher();

        assertNotNull(c1.getKeyBase64(), "Key must not be null");
        assertNotNull(c1.getIVBase64(),  "IV must not be null");
        assertEquals(24, c1.getKeyBytes().length, "Key must be 24 bytes (192-bit)");
        assertEquals(8,  c1.getIVBytes().length,  "IV must be 8 bytes");
        assertNotEquals(c1.getKeyBase64(), c2.getKeyBase64(), "Two keys must differ");
        assertNotEquals(c1.getIVBase64(),  c2.getIVBase64(),  "Two IVs must differ");
        printPass("1");
    }

    static void test2_EncryptDecryptRoundTrip() throws Exception {
        printTest("2. Encrypt → Decrypt Round-trip");
        byte[] original = "Hello, Triple DES Image Encryption! 🔐".getBytes();
        TripleDESCipher cipher = new TripleDESCipher();

        byte[] encrypted  = cipher.encrypt(original);
        byte[] decrypted  = cipher.decrypt(encrypted);

        assertNotArrayEquals(original, encrypted, "Ciphertext must differ from plaintext");
        assertArrayEquals(original, decrypted, "Decrypted bytes must match original");
        printPass("2");
    }

    static void test3_DifferentPlaintexts() throws Exception {
        printTest("3. Different Plaintexts → Different Ciphertexts");
        TripleDESCipher cipher = new TripleDESCipher();

        byte[] enc1 = cipher.encrypt("AAAAAAAA".getBytes());
        byte[] enc2 = cipher.encrypt("BBBBBBBB".getBytes());

        assertNotArrayEquals(enc1, enc2, "Different plaintexts must produce different ciphertexts");
        printPass("3");
    }

    static void test4_SameDataDifferentKeys() throws Exception {
        printTest("4. Same Data, Different Keys → Different Ciphertext");
        byte[] data = "ImageData12345".getBytes();

        TripleDESCipher c1 = new TripleDESCipher();
        TripleDESCipher c2 = new TripleDESCipher();

        byte[] enc1 = c1.encrypt(data);
        byte[] enc2 = c2.encrypt(data);

        assertNotArrayEquals(enc1, enc2, "Different keys must produce different ciphertexts");
        printPass("4");
    }

    static void test5_FileLevelRoundTrip() throws Exception {
        printTest("5. File-Level Round-trip (simulated image)");

        // Create a fake "image" file (1 KB of random bytes)
        byte[] fakeImage = new byte[1024];
        new SecureRandom().nextBytes(fakeImage);

        Path tmpDir  = Files.createTempDirectory("3des_test");
        Path imgPath = tmpDir.resolve("test_image.png");
        Path encPath = tmpDir.resolve("test_image.3des");
        Path keyPath = tmpDir.resolve("test_image.3deskey");
        Path outPath = tmpDir.resolve("test_image_restored.png");

        Files.write(imgPath, fakeImage);

        ImageEncryptionService svc = new ImageEncryptionService();
        svc.encryptImage(imgPath.toString(), encPath.toString(), keyPath.toString());
        svc.decryptImage(encPath.toString(), keyPath.toString(), outPath.toString());

        byte[] restored = Files.readAllBytes(outPath);
        assertArrayEquals(fakeImage, restored, "File-level round-trip must be lossless");

        // Cleanup
        Files.deleteIfExists(imgPath); Files.deleteIfExists(encPath);
        Files.deleteIfExists(keyPath); Files.deleteIfExists(outPath);
        Files.deleteIfExists(tmpDir);
        printPass("5");
    }

    static void test6_WrongKeyFails() throws Exception {
        printTest("6. Wrong Key → Decryption Failure");
        byte[] data  = "SecretImageData!".getBytes();
        TripleDESCipher enc = new TripleDESCipher();
        byte[] ciphertext   = enc.encrypt(data);

        // Try decrypting with a different random key
        TripleDESCipher wrong = new TripleDESCipher();
        boolean failed = false;
        try {
            byte[] result = wrong.decrypt(ciphertext);
            // If it decrypts without exception, data must differ
            if (java.util.Arrays.equals(result, data)) {
                System.out.println("  ✘ FAIL: Wrong key produced correct plaintext!");
                TripleDESTest.failed++;
                return;
            }
            failed = true; // Different data — acceptable
        } catch (Exception e) {
            failed = true; // Exception expected
        }
        assertTrue(failed, "Wrong key must not recover the original plaintext");
        printPass("6");
    }

    static void test7_LargeDataPerformance() throws Exception {
        printTest("7. Large Data Performance (5 MB)");
        byte[] largeData = new byte[5 * 1024 * 1024]; // 5 MB
        new SecureRandom().nextBytes(largeData);

        TripleDESCipher cipher = new TripleDESCipher();

        long t0 = System.currentTimeMillis();
        byte[] encrypted = cipher.encrypt(largeData);
        long encMs = System.currentTimeMillis() - t0;

        long t1 = System.currentTimeMillis();
        byte[] decrypted = cipher.decrypt(encrypted);
        long decMs = System.currentTimeMillis() - t1;

        assertArrayEquals(largeData, decrypted, "Large data round-trip must be lossless");

        System.out.printf("  Encrypt 5 MB: %d ms%n", encMs);
        System.out.printf("  Decrypt 5 MB: %d ms%n", decMs);
        printPass("7");
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    static void assertArrayEquals(byte[] a, byte[] b, String msg) {
        if (!java.util.Arrays.equals(a, b)) fail(msg);
    }

    static void assertNotArrayEquals(byte[] a, byte[] b, String msg) {
        if (java.util.Arrays.equals(a, b)) fail(msg);
    }

    static void assertNotNull(Object o, String msg) {
        if (o == null) fail(msg);
    }

    static void assertEquals(int expected, int actual, String msg) {
        if (expected != actual) fail(msg + " (expected=" + expected + ", got=" + actual + ")");
    }

    static void assertNotEquals(String a, String b, String msg) {
        if (a.equals(b)) fail(msg);
    }

    static void assertTrue(boolean cond, String msg) {
        if (!cond) fail(msg);
    }

    static void fail(String msg) {
        System.out.println("  ✘ FAIL: " + msg);
        TripleDESTest.failed++;
        throw new AssertionError(msg);
    }

    // ── Formatting ────────────────────────────────────────────────────────────

    static void printHeader(String title) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.printf( "║  %-48s║%n", title);
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
    }

    static void printTest(String name) {
        System.out.println("► Test " + name);
    }

    static void printPass(String n) {
        System.out.println("  ✔ PASS\n");
        passed++;
    }

    static void printSummary() {
        System.out.println("══════════════════════════════════════════════════");
        System.out.printf("  Results:  %d passed  /  %d failed  /  %d total%n",
                passed, failed, passed + failed);
        System.out.println("══════════════════════════════════════════════════");
        if (failed == 0) System.out.println("  All tests passed! ✔");
        else             System.out.println("  " + failed + " test(s) FAILED ✘");
    }
}