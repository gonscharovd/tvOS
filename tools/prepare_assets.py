#!/usr/bin/env python3
"""Create a build-only asset tree and safely recompress opaque scene JPEGs."""
from __future__ import annotations

import argparse
import io
import shutil
from pathlib import Path

from PIL import Image, ImageOps


def prepare_assets(source: Path, destination: Path, optimize: bool, quality: int = 88,
                   max_width: int = 1920, max_height: int = 1080) -> tuple[int, int]:
    if destination.exists():
        shutil.rmtree(destination)
    shutil.copytree(source, destination)

    before = 0
    after = 0
    if not optimize:
        return 0, 0

    background_dir = destination / "backgrounds"
    if not background_dir.is_dir():
        raise FileNotFoundError(f"Missing backgrounds directory: {background_dir}")

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
    return before, after


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source", type=Path, required=True)
    parser.add_argument("--dest", type=Path, required=True)
    parser.add_argument("--optimize", action="store_true")
    parser.add_argument("--quality", type=int, default=88)
    parser.add_argument("--max-width", type=int, default=1920)
    parser.add_argument("--max-height", type=int, default=1080)
    args = parser.parse_args()
    if not 75 <= args.quality <= 95:
        parser.error("--quality must be between 75 and 95")
    before, after = prepare_assets(args.source, args.dest, args.optimize,
                                   args.quality, args.max_width, args.max_height)
    if args.optimize:
        saved = before - after
        pct = (saved * 100.0 / before) if before else 0.0
        print(f"Optimized scene JPEGs: {before:,} -> {after:,} bytes "
              f"({saved:,} bytes saved, {pct:.1f}%)")
    else:
        print("Asset tree copied unchanged (ASSET_OPTIMIZE is disabled)")


if __name__ == "__main__":
    main()
