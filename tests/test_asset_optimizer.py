#!/usr/bin/env python3
"""Test that build-time optimization only recompresses scene JPEGs safely."""
from pathlib import Path
from tempfile import TemporaryDirectory
import subprocess
import sys

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / "tools" / "prepare_assets.py"

with TemporaryDirectory() as tmp:
    root = Path(tmp)
    source = root / "assets"
    (source / "backgrounds").mkdir(parents=True)
    (source / "cleanyourtv").mkdir()
    # A large, smooth source image compresses well while exercising the resize path.
    image = Image.new("RGB", (2400, 1350))
    pixels = image.load()
    for y in range(1350):
        for x in range(2400):
            pixels[x, y] = (x % 256, y % 256, (x + y) % 256)
    image.save(source / "backgrounds" / "test.jpg", quality=96)
    image.close()
    (source / "cleanyourtv" / "index.txt").write_text("keep-this-data\n", encoding="utf-8")
    alpha = Image.new("RGBA", (8, 8), (255, 0, 0, 100))
    alpha.save(source / "backgrounds" / "alpha.png")
    alpha.close()

    destination = root / "build-assets"
    subprocess.run([
        sys.executable, str(SCRIPT), "--source", str(source), "--dest", str(destination),
        "--optimize", "--quality", "88", "--max-width", "1920", "--max-height", "1080"
    ], check=True)

    with Image.open(destination / "backgrounds" / "test.jpg") as result:
        assert result.size == (1920, 1080), result.size
    assert (destination / "cleanyourtv" / "index.txt").read_text(encoding="utf-8") == "keep-this-data\n"
    assert (destination / "backgrounds" / "alpha.png").read_bytes() == (source / "backgrounds" / "alpha.png").read_bytes()

print("PASS: scene JPEGs are resized/recompressed; non-JPEG assets are preserved")
