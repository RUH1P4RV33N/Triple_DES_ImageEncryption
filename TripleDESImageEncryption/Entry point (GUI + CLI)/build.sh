#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
#  build.sh  —  Triple DES Image Encryption Project
#  Usage:
#    ./build.sh           # compile only
#    ./build.sh gui       # compile + launch GUI
#    ./build.sh test      # compile + run tests
#    ./build.sh encrypt <input> <output.3des> <key.3deskey>
#    ./build.sh decrypt <input.3des> <key.3deskey> <output_image>
# ─────────────────────────────────────────────────────────────────────────────

set -e

OUT_DIR="out"

CIPHER_SRC="Swing GUI (Encrypt tab + Decrypt tab + About)/TripleDESCipher.java"
SERVICE_SRC="Core 3DES encrypt/decrypt engine/ImageEncryptionService.java"
GUI_SRC="File I/O + key management/ImageEncryptionGUI.java"
MAIN_SRC="Automated test suite (7 tests)/Main.java"
TEST_SRC="src/TripleDESTest.java"

echo "══════════════════════════════════════════════"
echo "  Triple DES Image Encryption — Build Script  "
echo "══════════════════════════════════════════════"

# ── Compile ──────────────────────────────────────────────────────────────────
echo ""
echo "► Compiling sources…"
mkdir -p "$OUT_DIR"
javac -source 17 -target 17 -d "$OUT_DIR" \
  "$CIPHER_SRC"  \
  "$SERVICE_SRC" \
  "$GUI_SRC"     \
  "$TEST_SRC"    \
  "$MAIN_SRC"
echo "  ✔ Compilation successful"

# ── Run ───────────────────────────────────────────────────────────────────────
MODE="${1:-gui}"

case "$MODE" in
  gui)
    echo ""
    echo "► Launching GUI…"
    java -cp "$OUT_DIR" Main gui
    ;;
  test)
    echo ""
    echo "► Running test suite…"
    java -cp "$OUT_DIR" TripleDESTest
    ;;
  encrypt)
    echo ""
    echo "► Encrypting: $2  →  $3"
    java -cp "$OUT_DIR" Main encrypt "$2" "$3" "$4"
    ;;
  decrypt)
    echo ""
    echo "► Decrypting: $2  →  $4"
    java -cp "$OUT_DIR" Main decrypt "$2" "$3" "$4"
    ;;
  *)
    echo "  Unknown command: $MODE"
    echo "  Usage: ./build.sh [gui|test|encrypt|decrypt]"
    exit 1
    ;;
esac