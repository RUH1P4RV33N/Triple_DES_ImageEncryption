/**
 * Main.java — Entry point for the Triple DES Image Encryption project.
 *
 * Supports two modes:
 *   1. GUI mode (default — no arguments)
 *   2. CLI mode  (pass arguments for scripting / automation)
 *
 * CLI Usage:
 *   java Main encrypt <input_image> <output_enc> <output_key>
 *   java Main decrypt <input_enc>   <key_file>   <output_image>
 *   java Main gui
 */
public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length == 0 || args[0].equalsIgnoreCase("gui")) {
            // ── GUI mode ────────────────────────────────────────────────────
            System.out.println("Starting Triple DES Image Encryption GUI…");
            javax.swing.SwingUtilities.invokeLater(
                () -> new ImageEncryptionGUI().setVisible(true));

        } else if (args[0].equalsIgnoreCase("encrypt") && args.length == 4) {
            // ── CLI encrypt ─────────────────────────────────────────────────
            String inputImage = args[1];
            String outputEnc  = args[2];
            String outputKey  = args[3];

            System.out.println("═══ Triple DES Image Encryption — CLI ═══");
            System.out.println("Input Image    : " + inputImage);
            System.out.println("Output File    : " + outputEnc);
            System.out.println("Key File       : " + outputKey);
            System.out.println("Encrypting…");

            ImageEncryptionService svc = new ImageEncryptionService();
            ImageEncryptionService.EncryptionResult r =
                    svc.encryptImage(inputImage, outputEnc, outputKey);

            System.out.println("\n✔  ENCRYPTION SUCCESSFUL");
            System.out.printf("   Original Size  : %,d bytes%n", r.originalSize);
            System.out.printf("   Encrypted Size : %,d bytes%n", r.encryptedSize);
            System.out.printf("   Time Elapsed   : %d ms%n",     r.elapsedMs);
            System.out.println("   Key (Base64)   : " + r.keyBase64);
            System.out.println("   IV  (Base64)   : " + r.ivBase64);

        } else if (args[0].equalsIgnoreCase("decrypt") && args.length == 4) {
            // ── CLI decrypt ─────────────────────────────────────────────────
            String inputEnc   = args[1];
            String keyFile    = args[2];
            String outputImg  = args[3];

            System.out.println("═══ Triple DES Image Decryption — CLI ═══");
            System.out.println("Encrypted File : " + inputEnc);
            System.out.println("Key File       : " + keyFile);
            System.out.println("Output Image   : " + outputImg);
            System.out.println("Decrypting…");

            ImageEncryptionService svc = new ImageEncryptionService();
            ImageEncryptionService.DecryptionResult r =
                    svc.decryptImage(inputEnc, keyFile, outputImg);

            System.out.println("\n✔  DECRYPTION SUCCESSFUL");
            System.out.printf("   Encrypted Size  : %,d bytes%n", r.encryptedSize);
            System.out.printf("   Decrypted Size  : %,d bytes%n", r.decryptedSize);
            System.out.printf("   Time Elapsed    : %d ms%n",     r.elapsedMs);

        } else {
            printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("""
                Triple DES Image Encryption — Usage
                ─────────────────────────────────────────────────────────
                GUI Mode (default):
                  java Main
                  java Main gui

                CLI Encrypt:
                  java Main encrypt <input_image> <output.3des> <key.3deskey>

                CLI Decrypt:
                  java Main decrypt <input.3des> <key.3deskey> <output_image>

                Examples:
                  java Main encrypt photo.png photo.3des photo.3deskey
                  java Main decrypt photo.3des photo.3deskey photo_restored.png
                ─────────────────────────────────────────────────────────
                """);
    }
}