"""
Generate Android adaptive + legacy launcher icons from a single source PNG.

Usage:
    python scripts/generate_launcher_icons.py

Reads the logo from the hard-coded SOURCE path and writes:
  - mipmap-<density>/ic_launcher_foreground.png  (adaptive foreground, transparent bg)
  - mipmap-<density>/ic_launcher.webp            (legacy square)
  - mipmap-<density>/ic_launcher_round.webp      (legacy round/circle)
for all standard densities (mdpi..xxxhdpi).
"""
from __future__ import annotations

import os
from PIL import Image, ImageDraw

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
SOURCE = r"C:\Users\Aru\.cursor\projects\c-Users-Aru-AndroidStudioProjects-Diploma\assets\c__Users_Aru_AppData_Roaming_Cursor_User_workspaceStorage_abf1d8887af726e16b99cf7f002d6e38_images_logo_caresteps-15cf8cc8-5c7e-4c3e-a400-742c4cae4d47.png"
RES_DIR = os.path.join(ROOT, "app", "src", "main", "res")

BACKGROUND_COLOR = (0x2B, 0xB3, 0xB1, 0xFF)  # #2BB3B1

# Adaptive icon: canvas is 108x108dp. Safe zone for content is ~66dp diameter.
# We scale content to ~72% of canvas so the logo stays inside every mask.
ADAPTIVE_CONTENT_RATIO = 0.72

# Density multipliers relative to mdpi (1dp = 1px at mdpi).
DENSITIES = {
    "mdpi": 1.0,
    "hdpi": 1.5,
    "xhdpi": 2.0,
    "xxhdpi": 3.0,
    "xxxhdpi": 4.0,
}

# Legacy launcher icon size at mdpi (48dp). Adaptive foreground canvas is 108dp.
LEGACY_SIZE_DP = 48
ADAPTIVE_CANVAS_DP = 108


def load_source() -> Image.Image:
    img = Image.open(SOURCE).convert("RGBA")
    return img


def make_adaptive_foreground(src: Image.Image, canvas_px: int) -> Image.Image:
    """Transparent 108dp canvas with the logo centered inside the safe zone."""
    canvas = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
    content_px = int(round(canvas_px * ADAPTIVE_CONTENT_RATIO))
    logo = src.resize((content_px, content_px), Image.LANCZOS)
    offset = ((canvas_px - content_px) // 2, (canvas_px - content_px) // 2)
    canvas.alpha_composite(logo, dest=offset)
    return canvas


def make_legacy_square(src: Image.Image, size_px: int) -> Image.Image:
    """Solid-background square icon for legacy (pre-O) launchers."""
    bg = Image.new("RGBA", (size_px, size_px), BACKGROUND_COLOR)
    content_px = int(round(size_px * 0.82))  # a bit tighter than adaptive
    logo = src.resize((content_px, content_px), Image.LANCZOS)
    offset = ((size_px - content_px) // 2, (size_px - content_px) // 2)
    bg.alpha_composite(logo, dest=offset)
    return bg


def make_legacy_round(src: Image.Image, size_px: int) -> Image.Image:
    """Circular legacy icon. Fills the circle with BACKGROUND_COLOR and composites logo on top."""
    img = Image.new("RGBA", (size_px, size_px), (0, 0, 0, 0))
    # Draw filled circle as background.
    mask = Image.new("L", (size_px, size_px), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, size_px - 1, size_px - 1), fill=255)
    bg_layer = Image.new("RGBA", (size_px, size_px), BACKGROUND_COLOR)
    img.paste(bg_layer, mask=mask)
    # Place logo.
    content_px = int(round(size_px * 0.78))
    logo = src.resize((content_px, content_px), Image.LANCZOS)
    offset = ((size_px - content_px) // 2, (size_px - content_px) // 2)
    img.alpha_composite(logo, dest=offset)
    # Clip whole thing to circle so corners are fully transparent.
    out = Image.new("RGBA", (size_px, size_px), (0, 0, 0, 0))
    out.paste(img, mask=mask)
    return out


def save_png(img: Image.Image, path: str) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, format="PNG", optimize=True)


def save_webp(img: Image.Image, path: str) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, format="WEBP", lossless=True, quality=100)


def main() -> None:
    src = load_source()

    for density, scale in DENSITIES.items():
        out_dir = os.path.join(RES_DIR, f"mipmap-{density}")
        adaptive_px = int(round(ADAPTIVE_CANVAS_DP * scale))
        legacy_px = int(round(LEGACY_SIZE_DP * scale))

        fg = make_adaptive_foreground(src, adaptive_px)
        save_png(fg, os.path.join(out_dir, "ic_launcher_foreground.png"))

        square = make_legacy_square(src, legacy_px)
        save_webp(square, os.path.join(out_dir, "ic_launcher.webp"))

        round_ = make_legacy_round(src, legacy_px)
        save_webp(round_, os.path.join(out_dir, "ic_launcher_round.webp"))

        print(f"[{density}] adaptive={adaptive_px}px legacy={legacy_px}px  ->  {out_dir}")

    print("Done.")


if __name__ == "__main__":
    main()
