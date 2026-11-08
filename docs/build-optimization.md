# Build asset profiles and memory optimization

The repository keeps the complete, universal source assets. Build-time preparation writes only to `build/assets`; it never edits files under `assets/`.

## Profiles

- `ASSET_PROFILE=full` (default): keeps all fonts and universal CleanYourTV profiles.
- `ASSET_PROFILE=sber`: subsets the UI Inter fonts to Latin/Cyrillic and common punctuation, and clock fonts to digits/punctuation. This is suitable for the current Russian-only Sber UI. It does **not** remove CleanYourTV models or profiles.
- `ASSET_OPTIMIZE=1`: enables staged asset preparation. Without it, the build copies source assets unchanged.

For the personalized Sber build:

```bash
ASSET_OPTIMIZE=1 ASSET_PROFILE=sber bash build.sh
```

PowerShell:

```powershell
$env:ASSET_OPTIMIZE = "1"
$env:ASSET_PROFILE = "sber"
.\build.ps1
```

CI and release workflows set both variables and install Pillow + fontTools.

## Image and bitmap policy

- Scene JPEGs are resized only when larger than 1920×1080 and recompressed at quality 88.
- The source JPEGs remain unchanged. The current source JPEGs were already efficient; the measured additional reduction was only about 1 KB.
- At runtime, opaque JPEGs decode as `RGB_565` (about half the pixel memory of `ARGB_8888`).
- PNG/WebP and all other formats remain `ARGB_8888` so transparent foreground cutouts and logos keep their alpha channel.

## Measured Sber build

Latest successful CI artifact on PR #12:
- Font assets: 1,945,412 → 1,119,856 bytes (−42.4%).
- APK: 6,567,745 → 6,174,685 bytes (−5.98%) versus the prior CI build before font subsetting.

The APK-size result does not prove lower runtime PSS. Visual clock/wallpaper checks and a before/after PSS test on the target Sber SDX-50U4123B are still required before merging or publishing an OTA release.

## CleanYourTV profiles

Do not trim the universal CleanYourTV index for this device yet. The existing Sber examples target different boards/chipsets; neither matches the user's CV9632-KH / MT9632 television. Keep all 341 source profiles until a correct device profile has been verified.
