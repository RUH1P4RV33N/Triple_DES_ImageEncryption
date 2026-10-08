# 🔐 Triple DES Image Encryption — Java Project

A complete Java desktop application for **encrypting and decrypting images** using the **Triple DES (3DES / DESede)** algorithm with a polished Swing GUI.

---

## 📁 Project Structure

```
TripleDESImageEncryption/
├── src/
│   ├── TripleDESCipher.java          # Core 3DES encrypt/decrypt engine
│   ├── ImageEncryptionService.java   # File I/O + key management
│   ├── ImageEncryptionGUI.java       # Swing GUI (Encrypt tab + Decrypt tab + About)
│   ├── TripleDESTest.java            # Automated test suite (7 tests)
│   └── Main.java                     # Entry point (GUI + CLI)
├── build.sh                          # One-command build & run script
└── README.md
```

---

## 🔧 Requirements

| Tool | Version |
|------|---------|
| Java | 17+     |
| javac | 17+   |

No external dependencies — uses only **Java SE standard library** (`javax.crypto`, `javax.swing`, `java.nio`).

---

## 🚀 Quick Start

### 1. Compile
```bash
chmod +x build.sh
./build.sh
```
Or manually:
```bash
mkdir -p out
javac -d out src/*.java
```

### 2. Launch GUI
```bash
./build.sh gui
# or
java -cp out Main
```

### 3. Run Tests
```bash
./build.sh test
# or
java -cp out TripleDESTest
```

### 4. CLI Mode (no GUI)
```bash
# Encrypt
java -cp out Main encrypt photo.png photo.3des photo.3deskey

# Decrypt
java -cp out Main decrypt photo.3des photo.3deskey photo_restored.png
```

---

## 🔒 Algorithm Details

| Property   | Value                      |
|------------|----------------------------|
| Algorithm  | DESede (Triple DES)        |
| Mode       | CBC (Cipher Block Chaining) |
| Padding    | PKCS5Padding               |
| Key Size   | 168-bit effective (192-bit stored) |
| Block Size | 64-bit (8 bytes)           |
| IV Size    | 64-bit (8 bytes), randomly generated per encryption |

### How Triple DES Works

```
Plaintext
    │
    ▼
[DES Encrypt — Key K1]
    │
    ▼
[DES Decrypt — Key K2]   ← "EDE" mode
    │
    ▼
[DES Encrypt — Key K3]
    │
    ▼
Ciphertext
```

### File Format

```
Encrypted file (.3des):   [8 bytes IV][ciphertext bytes...]
Key file      (.3deskey): Java Properties (Base64-encoded key & IV)
```

---

## 🖥️ GUI Guide

### Encrypt Tab
1. Click **Browse…** next to *Input Image* — select any PNG / JPG / BMP
2. Output paths auto-fill (same directory, `.3des` and `.3deskey` extensions)
3. Click **🔒 Encrypt Image**
4. Results panel shows key, IV, sizes, and elapsed time

### Decrypt Tab
1. Select the `.3des` encrypted file
2. Select the matching `.3deskey` key file (auto-filled if same folder)
3. Choose output image path
4. Click **🔓 Decrypt Image**
5. Preview pane shows the restored image

---

## 🧪 Test Suite

`TripleDESTest` runs 7 automated checks:

| # | Test |
|---|------|
| 1 | Key generation (size, randomness) |
| 2 | Encrypt → Decrypt byte round-trip |
| 3 | Different plaintexts → different ciphertexts |
| 4 | Same data + different keys → different ciphertext |
| 5 | File-level lossless round-trip |
| 6 | Wrong key cannot recover plaintext |
| 7 | 5 MB large-data performance |

---

## ⚠️ Security Notes

- **Keep your `.3deskey` file secret.** Without it, decryption is computationally infeasible.
- 3DES is considered **legacy**; **AES-256** is preferred for modern production use.
- This project is intended for **educational purposes**.

---

## 📄 License

MIT — free to use and modify.