import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.file.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * ImageEncryptionService
 *
 * Handles the file-level workflow:
 *  - Reading image bytes from disk
 *  - Encrypting/decrypting via TripleDESCipher
 *  - Writing output files
 *  - Saving/loading key files (.3deskey)
 *
 * Encrypted file format:
 *  [8 bytes IV][cipher bytes...]
 *  The key is stored separately in a .3deskey file.
 */
public class ImageEncryptionService {

    /** Extension appended to encrypted files. */
    public static final String ENC_EXTENSION  = ".3des";
    /** Extension for key files. */
    public static final String KEY_EXTENSION  = ".3deskey";

    // -----------------------------------------------------------------------
    // Encrypt
    // -----------------------------------------------------------------------

    /**
     * Encrypts an image file.
     *
     * @param inputImagePath   path to the original image (PNG / JPG / BMP …)
     * @param outputEncPath    path where the encrypted file will be written
     * @param keyOutputPath    path where the key file will be saved
     * @return                 EncryptionResult with stats
     */
    public EncryptionResult encryptImage(String inputImagePath,
                                         String outputEncPath,
                                         String keyOutputPath)
            throws IOException, NoSuchAlgorithmException,
                   NoSuchPaddingException, InvalidKeyException,
                   InvalidAlgorithmParameterException,
                   IllegalBlockSizeException, BadPaddingException {

        long startTime = System.currentTimeMillis();

        // 1. Read original image bytes
        byte[] imageBytes = Files.readAllBytes(Paths.get(inputImagePath));

        // 2. Generate a fresh 3DES key
        TripleDESCipher cipher = new TripleDESCipher();

        // 3. Encrypt
        byte[] encryptedBytes = cipher.encrypt(imageBytes);

        // 4. Write encrypted file: [IV (8 bytes)][ciphertext]
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(outputEncPath)))) {
            byte[] iv = cipher.getIVBytes();
            dos.write(iv);               // prepend IV
            dos.write(encryptedBytes);
        }

        // 5. Save key file
        saveKeyFile(keyOutputPath, cipher.getKeyBase64(), cipher.getIVBase64(),
                    Paths.get(inputImagePath).getFileName().toString());

        long elapsed = System.currentTimeMillis() - startTime;

        return new EncryptionResult(
                inputImagePath, outputEncPath, keyOutputPath,
                imageBytes.length, encryptedBytes.length + 8,
                cipher.getKeyBase64(), cipher.getIVBase64(), elapsed
        );
    }

    // -----------------------------------------------------------------------
    // Decrypt
    // -----------------------------------------------------------------------

    /**
     * Decrypts an encrypted image file.
     *
     * @param encryptedFilePath  path to the .3des encrypted file
     * @param keyFilePath        path to the .3deskey file
     * @param outputImagePath    path where the restored image will be written
     * @return                   DecryptionResult with stats
     */
    public DecryptionResult decryptImage(String encryptedFilePath,
                                          String keyFilePath,
                                          String outputImagePath)
            throws IOException, NoSuchAlgorithmException,
                   NoSuchPaddingException, InvalidKeyException,
                   InvalidAlgorithmParameterException,
                   IllegalBlockSizeException, BadPaddingException {

        long startTime = System.currentTimeMillis();

        // 1. Load key from key file
        Properties props = loadKeyFile(keyFilePath);
        String base64Key = props.getProperty("key");
        String base64IV  = props.getProperty("iv");

        // 2. Read encrypted file: [IV (8 bytes)][ciphertext]
        byte[] rawFile = Files.readAllBytes(Paths.get(encryptedFilePath));
        byte[] iv          = new byte[8];
        byte[] cipherBytes = new byte[rawFile.length - 8];
        System.arraycopy(rawFile, 0, iv, 0, 8);
        System.arraycopy(rawFile, 8, cipherBytes, 0, cipherBytes.length);

        // 3. Rebuild cipher with stored key + IV
        TripleDESCipher cipher = new TripleDESCipher(base64Key, base64IV);

        // 4. Decrypt
        byte[] imageBytes = cipher.decrypt(cipherBytes);

        // 5. Write restored image
        Files.write(Paths.get(outputImagePath), imageBytes);

        long elapsed = System.currentTimeMillis() - startTime;

        return new DecryptionResult(
                encryptedFilePath, outputImagePath,
                cipherBytes.length, imageBytes.length, elapsed
        );
    }

    // -----------------------------------------------------------------------
    // Key file I/O
    // -----------------------------------------------------------------------

    private void saveKeyFile(String path, String key, String iv,
                              String originalFileName) throws IOException {
        Properties props = new Properties();
        props.setProperty("key",               key);
        props.setProperty("iv",                iv);
        props.setProperty("original_filename", originalFileName);
        props.setProperty("algorithm",         "DESede/CBC/PKCS5Padding");
        props.setProperty("generated_at",      java.time.Instant.now().toString());
        try (FileOutputStream fos = new FileOutputStream(path)) {
            props.store(fos, "Triple DES Image Encryption Key File — Keep this file SECRET");
        }
    }

    private Properties loadKeyFile(String path) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
        }
        return props;
    }

    // -----------------------------------------------------------------------
    // Result POJOs
    // -----------------------------------------------------------------------

    public static class EncryptionResult {
        public final String inputPath, outputPath, keyPath;
        public final long   originalSize, encryptedSize, elapsedMs;
        public final String keyBase64, ivBase64;

        public EncryptionResult(String inputPath, String outputPath, String keyPath,
                                long originalSize, long encryptedSize,
                                String keyBase64, String ivBase64, long elapsedMs) {
            this.inputPath     = inputPath;
            this.outputPath    = outputPath;
            this.keyPath       = keyPath;
            this.originalSize  = originalSize;
            this.encryptedSize = encryptedSize;
            this.keyBase64     = keyBase64;
            this.ivBase64      = ivBase64;
            this.elapsedMs     = elapsedMs;
        }
    }

    public static class DecryptionResult {
        public final String encryptedPath, outputPath;
        public final long   encryptedSize, decryptedSize, elapsedMs;

        public DecryptionResult(String encryptedPath, String outputPath,
                                long encryptedSize, long decryptedSize, long elapsedMs) {
            this.encryptedPath = encryptedPath;
            this.outputPath    = outputPath;
            this.encryptedSize = encryptedSize;
            this.decryptedSize = decryptedSize;
            this.elapsedMs     = elapsedMs;
        }
    }
}