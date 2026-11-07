#!/usr/bin/env python3
"""Guard bitmap decode formats: opaque JPEGs use RGB_565; alpha assets keep ARGB_8888."""
from pathlib import Path
import sys

source = Path("src/com/techgenius/tvoslauncher/Scenes.java").read_text(encoding="utf-8")
required = [
    "String lowerAssetPath = assetPath.toLowerCase(Locale.ROOT);",
    'lowerAssetPath.endsWith(".jpg") || lowerAssetPath.endsWith(".jpeg")',
    "Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888;",
]
missing = [item for item in required if item not in source]
if missing:
    print("FAIL: missing expected JPEG/alpha bitmap decode policy:", missing)
    sys.exit(1)
print("PASS: opaque JPEG assets use RGB_565; alpha-capable assets retain ARGB_8888")
