#!/usr/bin/env python3
"""Regression guards for Control Panel focus, Russian UI coverage and clock fonts."""
from pathlib import Path
import re

ROOT = Path("src/com/techgenius/tvoslauncher")
panel = (ROOT / "ControlPanelOverlay.java").read_text(encoding="utf-8")
main = (ROOT / "MainActivity.java").read_text(encoding="utf-8")
scenes = (ROOT / "Scenes.java").read_text(encoding="utf-8")
ui = (ROOT / "Ui.java").read_text(encoding="utf-8")
i18n = (ROOT / "I18n.java").read_text(encoding="utf-8")

# The full-width VPN row must lead to RAM, not skip diagonally to Sleep Timer.
assert 'layer.setTag("control_panel")' in panel
assert 'layer.setTag("control_panel")' in main
assert "moveControlPanelFocus(pickerCells, keyCode)" in main
assert "moveControlPanelFocus(activeCells, keyCode)" in panel
assert "case 2: return up ? 1 : down ? 3 : -1;" in panel
assert "case 3: return up ? 2 : right ? 4 : down ? 5 : -1;" in panel
assert "case 4: return up ? 2 : left ? 3 : down ? 5 : -1;" in panel
assert "case 5: return up ? 3 : down ? 6 : -1;" in panel

# Existing three styles plus six Android system families.
for style in ("DUO", "ROUNDED", "TALL", "CLASSIC", "SERIF", "DIGITAL", "CONDENSED", "THIN", "BOLD"):
    assert f"CLOCK_STYLE_{style}" in scenes, f"Missing clock style: {style}"
for family in ("sans-serif", "serif", "monospace", "sans-serif-condensed", "sans-serif-thin", "sans-serif-black"):
    assert family in ui, f"Missing clock typeface family: {family}"

# Every literal I18n key used by launcher Java screens must exist in the Russian dictionary.
def unescape_java(value: str) -> str:
    value = value.replace(r"\\n", "\n").replace(r"\\t", "\t").replace(r'\\"', '"').replace(r"\\\\", "\\")
    return re.sub(r"\\u([0-9a-fA-F]{4})", lambda m: chr(int(m.group(1), 16)), value)

dictionary = {
    unescape_java(m.group(1))
    for m in re.finditer(r'(?:add|ru\\.put)\\("((?:\\\\.|[^"\\\\])*)"', i18n)
}
untranslated = []
call_re = re.compile(r'I18n\\.(?:s|t)\\s*\\(\\s*[^,]+,\\s*"((?:\\\\.|[^"\\\\])*)"', re.S)
dynamic_prefixes = ("Move ", "Switch to ", "Done (", "Not done (")
for source in ROOT.glob("*.java"):
    code = source.read_text(encoding="utf-8")
    for match in call_re.finditer(code):
        key = unescape_java(match.group(1))
        tail = code[match.end():].lstrip()
        if tail.startswith("+") or key.startswith(dynamic_prefixes):
            continue
        if key.endswith("% used  ·  Clean"):
            continue  # translated by I18n.translateOther's numeric suffix handler
        if key not in dictionary:
            untranslated.append(f"{source.name}: {key!r}")

assert not untranslated, "Untranslated I18n keys: " + "; ".join(untranslated)

print("Control Panel navigation, clock font choices and Russian localization checks passed.")
