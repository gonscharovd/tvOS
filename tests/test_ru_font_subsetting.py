#!/usr/bin/env python3
"""Verify the Russian UI font subset retains Russian UI glyphs and digits while reducing size."""
from pathlib import Path
from tempfile import TemporaryDirectory
import shutil
import sys

from fontTools.ttLib import TTFont

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from prepare_assets import subset_ru_fonts

with TemporaryDirectory() as tmp:
    font_dir = Path(tmp) / "fonts"
    shutil.copytree(ROOT / "assets" / "fonts", font_dir)
    before, after = subset_ru_fonts(font_dir)
    assert after < before, (before, after)

    for name in ("Inter-Regular.ttf", "Inter-Medium.ttf", "Inter-SemiBold.ttf", "InterDisplay-Bold.ttf"):
        font = TTFont(str(font_dir / name))
        cmap = font.getBestCmap()
        required = [ord("A"), ord("Я"), ord("ё"), ord("№"), ord("—"), ord("é")]
        missing = [f"U+{cp:04X}" for cp in required if cp not in cmap]
        assert not missing, f"{name} is missing Russian/Latin UI glyphs: {missing}"

    for name in ("Antonio-Light.ttf", "Bahnschrift.ttf", "VarelaRound-Regular.ttf"):
        font = TTFont(str(font_dir / name))
        cmap = font.getBestCmap()
        missing = [ch for ch in "0123456789:" if ord(ch) not in cmap]
        assert not missing, f"{name} is missing clock glyphs: {missing}"

print(f"PASS: Russian UI font subset retains required glyphs and reduces {before:,} -> {after:,} bytes")
