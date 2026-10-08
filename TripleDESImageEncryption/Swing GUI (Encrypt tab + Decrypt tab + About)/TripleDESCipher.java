import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.Base64;

/**
 * TripleDESCipher - Core encryption/decryption engine using Triple DES (3DES / DESede)
 *
 * Triple DES applies the DES cipher algorithm three times to each data block.
 * Key size: 168-bit (24 bytes) effective security.
 * Block size: 64-bit (8 bytes).
 * Mode: CBC (Cipher Block Chaining) with PKCS5 padding.
 */
public class TripleDESCipher {

    private static final String ALGORITHM      = "DESede";
    private static final String TRANSFORMATION = "DESede/CBC/PKCS5Padding";
    private static final int    KEY_SIZE_BYTES  = 24;   // 192-bit key (168-bit effective)
    private static final int    IV_SIZE_BYTES   = 8;    // 3DES block size

    private SecretKey     secretKey;
    private IvParameterSpec iv;

    // -----------------------------------------------------------------------
    // Construction / Key Management
    // -----------------------------------------------------------------------

    /** Generate a brand-new random 3DES key + IV. */
    public TripleDESCipher() throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM);
        kg.init(168); // 168-bit effective key
        this.secretKey = kg.generateKey();
        this.iv        = generateIV();
    }

    /** Restore cipher from an existing Base64-encoded key + IV string. */
    public TripleDESCipher(String base64Key, String base64IV) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        byte[] ivBytes  = Base64.getDecoder().decode(base64IV);
        this.secretKey  = new SecretKeySpec(keyBytes, ALGORITHM);
        this.iv         = new IvParameterSpec(ivBytes);
    }

    /** Restore cipher from raw key + IV bytes (used internally). */
    public TripleDESCipher(byte[] keyBytes, byte[] ivBytes) {
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        this.iv        = new IvParameterSpec(ivBytes);
    }

    // -----------------------------------------------------------------------
    // Encrypt / Decrypt
    // -----------------------------------------------------------------------

    /**
     * Encrypt raw bytes (image pixel data).
     * @param plainData  raw bytes to encrypt
     * @return           ciphertext bytes
     */
    public byte[] encrypt(byte[] plainData)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
                   InvalidAlgorithmParameterException, InvalidKeyException,
                   IllegalBlockSizeException, BadPaddingException {

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        return cipher.doFinal(plainData);
    }

    /**
     * Decrypt ciphertext bytes back to original image bytes.
     * @param cipherData  encrypted bytes
     * @return            original plaintext bytes
     */
    public byte[] decrypt(byte[] cipherData)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
                   InvalidAlgorithmParameterException, InvalidKeyException,
                   IllegalBlockSizeException, BadPaddingException {

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        return cipher.doFinal(cipherData);
    }

    // -----------------------------------------------------------------------
    // Key / IV Accessors  (Base64-encoded for safe file storage)
    // -----------------------------------------------------------------------

    public String getKeyBase64() {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public String getIVBase64() {
        return Base64.getEncoder().encodeToString(iv.getIV());
    }

    public byte[] getKeyBytes() { return secretKey.getEncoded(); }
    public byte[] getIVBytes()  { return iv.getIV(); }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private IvParameterSpec generateIV() {
        byte[] ivBytes = new byte[IV_SIZE_BYTES];
        new SecureRandom().nextBytes(ivBytes);
        return new IvParameterSpec(ivBytes);
    }

    /** Human-readable hex dump (for debug / display). */
    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }

    // -----------------------------------------------------------------------
    // Static / Metadata
    // -----------------------------------------------------------------------

    public static String getAlgorithmInfo() {
        return "Algorithm: DESede (Triple DES)  |  Mode: CBC  |  Padding: PKCS5  |  Key: 168-bit  |  Block: 64-bit";
    }
}