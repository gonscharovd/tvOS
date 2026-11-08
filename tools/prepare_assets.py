#!/usr/bin/env python3
"""Create a build-only asset tree with optional Sber-specific font and JPEG optimization."""
from __future__ import annotations

import argparse
import io
import shutil
from pathlib import Path

from PIL import Image, ImageOps


UI_FONT_NAMES = ("Inter-Regular.ttf", "Inter-Medium.ttf", "Inter-SemiBold.ttf", "InterDisplay-Bold.ttf")
CLOCK_FONT_NAMES = ("Antonio-Light.ttf", "Bahnschrift.ttf", "VarelaRound-Regular.ttf")


def _subset_font(path: Path, unicodes: set[int]) -> tuple[int, int]:
    from fontTools import subset
    from fontTools.ttLib import TTFont

    original_size = path.stat().st_size
    font = TTFont(str(path))
    options = subset.Options()
    options.name_IDs = ["*"]
    options.name_legacy = True
    options.name_languages = ["*"]
    options.layout_features = ["*"]
    options.glyph_names = True
    options.legacy_cmap = True
    options.symbol_cmap = True
    options.notdef_glyph = True
    options.notdef_outline = True
    options.recommended_glyphs = True
    subsetter = subset.Subsetter(options=options)
    subsetter.populate(unicodes=unicodes)
    subsetter.subset(font)

    output = io.BytesIO()
    font.save(output)
    data = output.getvalue()
    if len(data) < original_size:
        path.write_bytes(data)
    return original_size, min(original_size, len(data))


def subset_sber_fonts(font_dir: Path) -> tuple[int, int]:
    # The Sber edition exposes Russian UI. Include Latin (including Turkish/Western accents),
    # Cyrillic, combining marks, punctuation, currency, common symbols and the numero sign.
    ui_ranges = (
        (0x0020, 0x024F), (0x0300, 0x036F), (0x0400, 0x052F),
        (0x2000, 0x206F), (0x20A0, 0x20CF), (0x2100, 0x214F),
        (0x2190, 0x21FF), (0x2DE0, 0x2DFF), (0xA640, 0xA69F),
    )
    ui_unicodes = {cp for start, end in ui_ranges for cp in range(start, end + 1)}
    clock_unicodes = set(range(0x0020, 0x003B)) | {0x2013, 0x2014, 0x2022}

    before = after = 0
    for name in UI_FONT_NAMES:
        old, new = _subset_font(font_dir / name, ui_unicodes)
        before += old
        after += new
    for name in CLOCK_FONT_NAMES:
        old, new = _subset_font(font_dir / name, clock_unicodes)
        before += old
        after += new
    return before, after


def prepare_assets(source: Path, destination: Path, optimize: bool, quality: int = 88,
                   max_width: int = 1920, max_height: int = 1080, profile: str = "full") -> dict[str, tuple[int, int]]:
    if destination.exists():
        shutil.rmtree(destination)
    shutil.copytree(source, destination)

    results: dict[str, tuple[int, int]] = {}
    if not optimize:
        return results

    background_dir = destination / "backgrounds"
    if not background_dir.is_dir():
        raise FileNotFoundError(f"Missing backgrounds directory: {background_dir}")

    before = after = 0
    for path in sorted(background_dir.iterdir()):
        if not path.is_file() or path.suffix.lower() not in (".jpg", ".jpeg"):
            continue
        original = path.read_bytes()
        before += len(original)
        with Image.open(io.BytesIO(original)) as opened:
            image = ImageOps.exif_transpose(opened).convert("RGB")
            old_size = image.size
            image.thumbnail((max_width, max_height), Image.Resampling.LANCZOS)
            resized = image.size != old_size
            output = io.BytesIO()
            image.save(output, format="JPEG", quality=quality, optimize=True,
                       progressive=False, subsampling=2)
        candidate = output.getvalue()
        if resized or len(candidate) < len(original):
            path.write_bytes(candidate)
            after += len(candidate)
        else:
            after += len(original)
    results["scene JPEGs"] = (before, after)

    if profile == "sber":
        results["fonts"] = subset_sber_fonts(destination / "fonts")
    return results


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source", type=Path, required=True)
    parser.add_argument("--dest", type=Path, required=True)
    parser.add_argument("--optimize", action="store_true")
    parser.add_argument("--profile", choices=("full", "sber"), default="full")
    parser.add_argument("--quality", type=int, default=88)
    parser.add_argument("--max-width", type=int, default=1920)
    parser.add_argument("--max-height", type=int, default=1080)
    args = parser.parse_args()
    if not 75 <= args.quality <= 95:
        parser.error("--quality must be between 75 and 95")
    results = prepare_assets(args.source, args.dest, args.optimize,
                             args.quality, args.max_width, args.max_height, args.profile)
    if not args.optimize:
        print("Asset tree copied unchanged (ASSET_OPTIMIZE is disabled)")
    else:
        for label, (before, after) in results.items():
            saved = before - after
            pct = (saved * 100.0 / before) if before else 0.0
            print(f"Optimized {label}: {before:,} -> {after:,} bytes "
                  f"({saved:,} bytes saved, {pct:.1f}%)")


if __name__ == "__main__":
    main()
