#!/usr/bin/env python3
"""Require Russian dictionary entries for every literal UI translation call."""
from pathlib import Path
import re

SOURCE_ROOT = Path("src/com/techgenius/tvoslauncher")
I18N_PATH = SOURCE_ROOT / "I18n.java"


def java_unescape(value: str) -> str:
    value = re.sub(
        r'\\u([0-9a-fA-F]{4})',
        lambda match: chr(int(match.group(1), 16)),
        value,
    )
    escapes = {
        "n": "\n",
        "r": "\r",
        "t": "\t",
        "b": "\b",
        "f": "\f",
        '"': '"',
        "'": "'",
        "\\": "\\",
    }
    return re.sub(r"\\([nrtbf\"'\\])", lambda match: escapes[match.group(1)], value)


i18n = I18N_PATH.read_text(encoding="utf-8")
dictionary = {
    java_unescape(match.group(1))
    for match in re.finditer(r'add\("((?:\\.|[^"\\])*)"\s*,', i18n)
}

literal_keys = set()
for source_path in SOURCE_ROOT.rglob("*.java"):
    source = source_path.read_text(encoding="utf-8")
    # I18n.s/t take Context first and the English key second.
    literal_keys.update(
        java_unescape(match.group(1))
        for match in re.finditer(
            r'I18n\.(?:s|t)\(\s*[^,]+,\s*"((?:\\.|[^"\\])*)"\s*,',
            source,
        )
    )
    # Local tr helpers use either tr("English", ...) or tr(context, "English", ...).
    literal_keys.update(
        java_unescape(match.group(1))
        for match in re.finditer(
            r'(?<![\w.])tr\(\s*(?:[A-Za-z_]\w*\s*,\s*)?"((?:\\.|[^"\\])*)"\s*,',
            source,
        )
    )

missing = sorted(literal_keys - dictionary)
assert not missing, (
    f"{len(missing)} literal UI strings have no Russian dictionary entry:\n"
    + "\n".join(f"  - {key!r}" for key in missing)
)

print(
    f"Russian localization coverage passed: {len(literal_keys)} unique literal UI strings "
    f"across {len(list(SOURCE_ROOT.rglob('*.java')))} Java source files."
)
