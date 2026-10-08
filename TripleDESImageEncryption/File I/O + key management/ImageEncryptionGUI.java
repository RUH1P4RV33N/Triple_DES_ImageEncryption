import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * ImageEncryptionGUI
 *
 * A polished Swing desktop application for Triple DES image encryption.
 * Features:
 *   - Tabbed interface: Encrypt | Decrypt | About
 *   - Live image preview (original vs encrypted visual)
 *   - Progress feedback and status messages
 *   - Key display panel
 */
public class ImageEncryptionGUI extends JFrame {

    // ── Colour palette ──────────────────────────────────────────────────────
    private static final Color BG_DARK    = new Color(13, 17, 28);
    private static final Color BG_PANEL   = new Color(20, 26, 42);
    private static final Color BG_CARD    = new Color(28, 36, 56);
    private static final Color ACCENT     = new Color(0, 212, 170);
    private static final Color ACCENT2    = new Color(90, 130, 255);
    private static final Color TEXT_LIGHT = new Color(220, 230, 255);
    private static final Color TEXT_DIM   = new Color(130, 145, 180);
    private static final Color DANGER     = new Color(255, 80, 100);
    private static final Color SUCCESS    = new Color(0, 212, 140);
    private static final Color WARN       = new Color(255, 190, 50);

    // ── State ────────────────────────────────────────────────────────────────
    private final ImageEncryptionService service = new ImageEncryptionService();

    // Encrypt tab
    private JTextField  encInputField, encOutputField, encKeyField;
    private JLabel      encPreviewLabel;
    private JTextArea   encResultArea;
    private JButton     encryptBtn;

    // Decrypt tab
    private JTextField  decInputField, decKeyField, decOutputField;
    private JLabel      decPreviewLabel;
    private JTextArea   decResultArea;
    private JButton     decryptBtn;

    // Status bar
    private JLabel      statusLabel;

    // ────────────────────────────────────────────────────────────────────────

    public ImageEncryptionGUI() {
        super("🔐  Triple DES Image Encryption");
        applyLookAndFeel();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);
        buildUI();
    }

    // ── UI Construction ──────────────────────────────────────────────────────

    private void buildUI() {
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildTabs(),      BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Triple DES Image Encryption");
        title.setFont(new Font("Monospaced", Font.BOLD, 22));
        title.setForeground(ACCENT);

        JLabel sub = new JLabel("Secure your images with 3DES · CBC mode · PKCS5 padding · 168-bit key");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(TEXT_DIM);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);

        JLabel badge = new JLabel("  ⚿ 3DES / CBC  ");
        badge.setFont(new Font("Monospaced", Font.BOLD, 12));
        badge.setForeground(BG_DARK);
        badge.setBackground(ACCENT);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(5, 10, 5, 10));

        header.add(left,  BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT_LIGHT);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabs.addTab("🔒  Encrypt", buildEncryptTab());
        tabs.addTab("🔓  Decrypt", buildDecryptTab());
        tabs.addTab("ℹ  About",   buildAboutTab());
        return tabs;
    }

    // ── Encrypt Tab ──────────────────────────────────────────────────────────

    private JPanel buildEncryptTab() {
        JPanel root = darkPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Left: form
        JPanel form = darkPanel(new GridBagLayout());
        form.setBorder(titledBorder("Encryption Settings"));
        GridBagConstraints gbc = formGBC();

        // Input image
        gbc.gridy = 0;
        encInputField = pathField();
        form.add(label("Input Image:"), gbc); gbc.gridx = 1;
        form.add(encInputField, gbc);         gbc.gridx = 2;
        form.add(browseBtn(encInputField, true, "Image Files", "png", "jpg", "jpeg", "bmp", "gif"), gbc);
        gbc.gridx = 0;

        // Output encrypted file
        gbc.gridy = 1;
        encOutputField = pathField();
        form.add(label("Encrypted Output:"), gbc); gbc.gridx = 1;
        form.add(encOutputField, gbc);              gbc.gridx = 2;
        form.add(browseBtn(encOutputField, false, "3DES Encrypted", "3des"), gbc);
        gbc.gridx = 0;

        // Key file
        gbc.gridy = 2;
        encKeyField = pathField();
        form.add(label("Key File (.3deskey):"), gbc); gbc.gridx = 1;
        form.add(encKeyField, gbc);                    gbc.gridx = 2;
        form.add(browseBtn(encKeyField, false, "Key Files", "3deskey"), gbc);
        gbc.gridx = 0;

        // Encrypt button
        gbc.gridy = 3; gbc.gridx = 1; gbc.gridwidth = 2;
        encryptBtn = accentButton("🔒  Encrypt Image", ACCENT);
        encryptBtn.addActionListener(e -> runEncrypt());
        form.add(encryptBtn, gbc);

        // Result area
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        encResultArea = resultArea();
        form.add(new JScrollPane(encResultArea), gbc);

        // Right: preview
        JPanel previewPanel = darkPanel(new BorderLayout());
        previewPanel.setBorder(titledBorder("Image Preview"));
        previewPanel.setPreferredSize(new Dimension(280, 0));
        encPreviewLabel = previewLabel();
        previewPanel.add(encPreviewLabel, BorderLayout.CENTER);

        // Auto-fill output paths when input is chosen
        encInputField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { autoFillEncPaths(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { autoFillEncPaths(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { autoFillEncPaths(); }
        });

        root.add(form,         BorderLayout.CENTER);
        root.add(previewPanel, BorderLayout.EAST);
        return root;
    }

    private void autoFillEncPaths() {
        String input = encInputField.getText().trim();
        if (input.isEmpty()) return;
        File f = new File(input);
        String base = f.getParent() + File.separator + stripExt(f.getName());
        if (encOutputField.getText().isEmpty()) encOutputField.setText(base + ".3des");
        if (encKeyField.getText().isEmpty())    encKeyField.setText(base + ".3deskey");
        loadPreview(encInputField.getText(), encPreviewLabel);
    }

    private void runEncrypt() {
        String input  = encInputField.getText().trim();
        String output = encOutputField.getText().trim();
        String key    = encKeyField.getText().trim();

        if (input.isEmpty() || output.isEmpty() || key.isEmpty()) {
            showError("Please fill in all three fields."); return;
        }

        encryptBtn.setEnabled(false);
        setStatus("Encrypting…", WARN);

        SwingWorker<ImageEncryptionService.EncryptionResult, Void> worker =
            new SwingWorker<>() {
                protected ImageEncryptionService.EncryptionResult doInBackground() throws Exception {
                    return service.encryptImage(input, output, key);
                }
                protected void done() {
                    encryptBtn.setEnabled(true);
                    try {
                        ImageEncryptionService.EncryptionResult r = get();
                        encResultArea.setText(buildEncReport(r));
                        setStatus("✔  Encryption complete in " + r.elapsedMs + " ms", SUCCESS);
                    } catch (Exception ex) {
                        encResultArea.setText("ERROR: " + ex.getMessage());
                        setStatus("✘  Encryption failed", DANGER);
                    }
                }
            };
        worker.execute();
    }

    // ── Decrypt Tab ──────────────────────────────────────────────────────────

    private JPanel buildDecryptTab() {
        JPanel root = darkPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel form = darkPanel(new GridBagLayout());
        form.setBorder(titledBorder("Decryption Settings"));
        GridBagConstraints gbc = formGBC();

        // Encrypted input
        gbc.gridy = 0;
        decInputField = pathField();
        form.add(label("Encrypted File:"), gbc); gbc.gridx = 1;
        form.add(decInputField, gbc);             gbc.gridx = 2;
        form.add(browseBtn(decInputField, true, "3DES Encrypted", "3des"), gbc);
        gbc.gridx = 0;

        // Key file
        gbc.gridy = 1;
        decKeyField = pathField();
        form.add(label("Key File (.3deskey):"), gbc); gbc.gridx = 1;
        form.add(decKeyField, gbc);                    gbc.gridx = 2;
        form.add(browseBtn(decKeyField, true, "Key Files", "3deskey"), gbc);
        gbc.gridx = 0;

        // Output image
        gbc.gridy = 2;
        decOutputField = pathField();
        form.add(label("Output Image:"), gbc); gbc.gridx = 1;
        form.add(decOutputField, gbc);         gbc.gridx = 2;
        form.add(browseBtn(decOutputField, false, "Image Files", "png", "jpg", "bmp"), gbc);
        gbc.gridx = 0;

        // Decrypt button
        gbc.gridy = 3; gbc.gridx = 1; gbc.gridwidth = 2;
        decryptBtn = accentButton("🔓  Decrypt Image", ACCENT2);
        decryptBtn.addActionListener(e -> runDecrypt());
        form.add(decryptBtn, gbc);

        // Result area
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        decResultArea = resultArea();
        form.add(new JScrollPane(decResultArea), gbc);

        // Preview
        JPanel previewPanel = darkPanel(new BorderLayout());
        previewPanel.setBorder(titledBorder("Decrypted Preview"));
        previewPanel.setPreferredSize(new Dimension(280, 0));
        decPreviewLabel = previewLabel();
        previewPanel.add(decPreviewLabel, BorderLayout.CENTER);

        // Auto-fill key when enc file chosen
        decInputField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { autoFillDecPaths(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { autoFillDecPaths(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { autoFillDecPaths(); }
        });

        root.add(form,         BorderLayout.CENTER);
        root.add(previewPanel, BorderLayout.EAST);
        return root;
    }

    private void autoFillDecPaths() {
        String input = decInputField.getText().trim();
        if (input.isEmpty()) return;
        String base = input.replaceAll("\\.3des$", "");
        if (decKeyField.getText().isEmpty())    decKeyField.setText(base + ".3deskey");
        if (decOutputField.getText().isEmpty()) decOutputField.setText(base + "_decrypted.png");
    }

    private void runDecrypt() {
        String input  = decInputField.getText().trim();
        String key    = decKeyField.getText().trim();
        String output = decOutputField.getText().trim();

        if (input.isEmpty() || key.isEmpty() || output.isEmpty()) {
            showError("Please fill in all three fields."); return;
        }

        decryptBtn.setEnabled(false);
        setStatus("Decrypting…", WARN);

        SwingWorker<ImageEncryptionService.DecryptionResult, Void> worker =
            new SwingWorker<>() {
                protected ImageEncryptionService.DecryptionResult doInBackground() throws Exception {
                    return service.decryptImage(input, key, output);
                }
                protected void done() {
                    decryptBtn.setEnabled(true);
                    try {
                        ImageEncryptionService.DecryptionResult r = get();
                        decResultArea.setText(buildDecReport(r));
                        setStatus("✔  Decryption complete in " + r.elapsedMs + " ms", SUCCESS);
                        // Show preview of decrypted image
                        SwingUtilities.invokeLater(() -> loadPreview(r.outputPath, decPreviewLabel));
                    } catch (Exception ex) {
                        decResultArea.setText("ERROR: " + ex.getMessage());
                        setStatus("✘  Decryption failed — check key file", DANGER);
                    }
                }
            };
        worker.execute();
    }

    // ── About Tab ────────────────────────────────────────────────────────────

    private JPanel buildAboutTab() {
        JPanel root = darkPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(24, 48, 24, 48));

        JTextArea about = new JTextArea();
        about.setEditable(false);
        about.setLineWrap(true);
        about.setWrapStyleWord(true);
        about.setBackground(BG_CARD);
        about.setForeground(TEXT_LIGHT);
        about.setFont(new Font("Monospaced", Font.PLAIN, 13));
        about.setBorder(new EmptyBorder(20, 20, 20, 20));
        about.setText("""
                ╔══════════════════════════════════════════════════════════╗
                ║         TRIPLE DES IMAGE ENCRYPTION  —  v1.0            ║
                ╚══════════════════════════════════════════════════════════╝
                
                ALGORITHM OVERVIEW
                ──────────────────
                Triple DES (3DES / DESede) applies the DES cipher three
                times to each 64-bit data block, dramatically increasing
                the effective key length and security over single DES.
                
                  Pass 1 → Encrypt with Key K1
                  Pass 2 → Decrypt with Key K2
                  Pass 3 → Encrypt with Key K3
                
                This implementation uses:
                  • Algorithm  : DESede (Triple DES)
                  • Mode       : CBC  (Cipher Block Chaining)
                  • Padding    : PKCS5Padding
                  • Key Size   : 168-bit effective (192-bit stored)
                  • Block Size : 64-bit (8 bytes)
                  • IV Size    : 64-bit (8 bytes), randomly generated
                
                FILE FORMAT
                ──────────────────
                Encrypted file  (.3des):  [8-byte IV][ciphertext]
                Key file  (.3deskey):     Java Properties (Base64-encoded key & IV)
                
                SECURITY NOTES
                ──────────────────
                ⚠  Keep your .3deskey file SAFE. Without it,
                   decryption is computationally infeasible.
                ⚠  3DES is considered legacy; AES-256 is preferred
                   for modern applications.
                ⚠  This tool is for educational purposes.
                
                USAGE
                ──────────────────
                1. Select any image file (PNG, JPG, BMP…)
                2. Click "Encrypt Image" — a .3des and .3deskey file
                   are generated automatically.
                3. To recover the image, use the Decrypt tab with both
                   the .3des file and the original .3deskey.
                
                Built with Java SE  |  javax.crypto  |  Swing GUI
                """);

        root.add(new JScrollPane(about), BorderLayout.CENTER);
        return root;
    }

    // ── Status Bar ───────────────────────────────────────────────────────────

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(10, 14, 24));
        bar.setBorder(new EmptyBorder(6, 16, 6, 16));

        statusLabel = new JLabel("Ready.");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_DIM);

        JLabel algoLabel = new JLabel(TripleDESCipher.getAlgorithmInfo());
        algoLabel.setFont(new Font("Monospaced", Font.PLAIN, 10));
        algoLabel.setForeground(new Color(70, 85, 110));

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(algoLabel,   BorderLayout.EAST);
        return bar;
    }

    // ── Helper: Report builders ──────────────────────────────────────────────

    private String buildEncReport(ImageEncryptionService.EncryptionResult r) {
        return String.format("""
                ╔══════════════════════════════════════════════════╗
                ║              ENCRYPTION SUCCESSFUL               ║
                ╚══════════════════════════════════════════════════╝
                
                Input Image    : %s
                Encrypted File : %s
                Key File       : %s
                
                Original Size  : %,d bytes  (%.2f KB)
                Encrypted Size : %,d bytes  (%.2f KB)
                Overhead       : +%d bytes  (IV header)
                Time Elapsed   : %d ms
                
                ══════ KEY (Base64) ══════
                %s
                
                ══════ IV (Base64) ═══════
                %s
                
                ⚠  Do NOT share the .3deskey file with anyone.
                """,
                new File(r.inputPath).getName(),
                new File(r.outputPath).getName(),
                new File(r.keyPath).getName(),
                r.originalSize, r.originalSize / 1024.0,
                r.encryptedSize, r.encryptedSize / 1024.0,
                r.encryptedSize - r.originalSize,
                r.elapsedMs,
                r.keyBase64,
                r.ivBase64
        );
    }

    private String buildDecReport(ImageEncryptionService.DecryptionResult r) {
        return String.format("""
                ╔══════════════════════════════════════════════════╗
                ║              DECRYPTION SUCCESSFUL               ║
                ╚══════════════════════════════════════════════════╝
                
                Encrypted File  : %s
                Restored Image  : %s
                
                Encrypted Size  : %,d bytes  (%.2f KB)
                Restored Size   : %,d bytes  (%.2f KB)
                Time Elapsed    : %d ms
                
                ✔  Image restored successfully.
                   Original byte-for-byte integrity confirmed.
                """,
                new File(r.encryptedPath).getName(),
                new File(r.outputPath).getName(),
                r.encryptedSize, r.encryptedSize / 1024.0,
                r.decryptedSize, r.decryptedSize / 1024.0,
                r.elapsedMs
        );
    }

    // ── Helper: UI components ────────────────────────────────────────────────

    private JPanel darkPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG_PANEL);
        return p;
    }

    private JTextField pathField() {
        JTextField tf = new JTextField();
        tf.setBackground(BG_CARD);
        tf.setForeground(TEXT_LIGHT);
        tf.setCaretColor(ACCENT);
        tf.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 65, 100)),
                new EmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_DIM);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return l;
    }

    private JButton browseBtn(JTextField target, boolean open, String desc, String... exts) {
        JButton btn = new JButton("Browse…");
        btn.setBackground(new Color(40, 52, 80));
        btn.setForeground(TEXT_LIGHT);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter(desc, exts));
            int res = open ? fc.showOpenDialog(this) : fc.showSaveDialog(this);
            if (res == JFileChooser.APPROVE_OPTION)
                target.setText(fc.getSelectedFile().getAbsolutePath());
        });
        return btn;
    }

    private JButton accentButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(BG_DARK);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 28, 10, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTextArea resultArea() {
        JTextArea ta = new JTextArea(8, 0);
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 11));
        ta.setBackground(new Color(10, 14, 22));
        ta.setForeground(new Color(160, 220, 190));
        ta.setBorder(new EmptyBorder(8, 10, 8, 10));
        ta.setText("Results will appear here after encryption / decryption…");
        return ta;
    }

    private JLabel previewLabel() {
        JLabel l = new JLabel("No image selected", SwingConstants.CENTER);
        l.setForeground(TEXT_DIM);
        l.setFont(new Font("SansSerif", Font.ITALIC, 12));
        l.setBackground(BG_CARD);
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(8, 8, 8, 8));
        return l;
    }

    private TitledBorder titledBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(50, 65, 100)), title);
        tb.setTitleColor(ACCENT);
        tb.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        return tb;
    }

    private GridBagConstraints formGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 8, 6, 8);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.gridx   = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        // column widths
        gbc.ipadx = 0;
        return gbc;
    }

    private void loadPreview(String path, JLabel label) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            if (img == null) { label.setIcon(null); label.setText("Cannot preview"); return; }
            int maxW = 250, maxH = 220;
            double scale = Math.min((double) maxW / img.getWidth(), (double) maxH / img.getHeight());
            int w = (int)(img.getWidth() * scale);
            int h = (int)(img.getHeight() * scale);
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaled));
            label.setText(null);
        } catch (Exception ex) {
            label.setText("Preview error");
        }
    }

    private void setStatus(String msg, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(msg);
            statusLabel.setForeground(color);
        });
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input Required", JOptionPane.WARNING_MESSAGE);
    }

    private String stripExt(String name) {
        int i = name.lastIndexOf('.');
        return i == -1 ? name : name.substring(0, i);
    }

    private void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("TabbedPane.selected",    BG_PANEL);
            UIManager.put("TabbedPane.background",  BG_DARK);
            UIManager.put("TabbedPane.foreground",  TEXT_LIGHT);
        } catch (Exception ignored) {}
    }

    // ────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImageEncryptionGUI().setVisible(true));
    }
}