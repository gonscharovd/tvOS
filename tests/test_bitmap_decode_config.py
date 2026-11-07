#!/usr/bin/env python3
"""Guard bitmap decode formats: opaque JPEGs use RGB_565; alpha assets keep ARGB_8888."""
from pathlib import Path
import re
import sys

source = Path("src/com/techgenius/tvoslauncher/Scenes.java").read_text(encoding="utf-8")
pattern = (
    r'String lowerAssetPath = assetPath\\.toLowerCase\\(Locale\\.ROOT\\);\\s*'
    r'opts\\.inPreferredConfig = \\(lowerAssetPath\\.endsWith\\("\\.jpg"\\) \\|\\| '
    r'lowerAssetPath\\.endsWith\\("\\.jpeg"\\)\\)\\s*'
    r'\\? Bitmap\\.Config\\.RGB_565\\s*:\\s*Bitmap\\.Config\\.ARGB_8888;'
)
if not re.search(pattern, source):
    print("FAIL: JPEG decode must use RGB_565 and other formats ARGB_8888")
    sys.exit(1)
print("PASS: opaque JPEG assets use RGB_565; alpha-capable assets retain ARGB_8888")
