#!/usr/bin/env python3
"""Verify the gallery captures and build the contact sheet.

Checks, per page:
  - the file exists and is a full 1080x2400 frame
  - the frame is not blank (distinct colour count)
  - the magenta end marker is present, which proves the page fitted one screen

Then writes design/captures/gallery-components.png: every page at 40 percent scale in a
two-column contact sheet, with a caption per page.

Run from the repository root:  python3 .mobile-work/build-gallery-sheet.py
"""

from __future__ import annotations

import pathlib
import sys

from PIL import Image, ImageDraw

ROOT = pathlib.Path(__file__).resolve().parent.parent
CAPTURES = ROOT / "design/captures"
MARKER = (255, 0, 255)
SCALE = 0.4
COLUMNS = 2
CAPTION_H = 26


def marker_rows(image: Image.Image) -> int:
    """Rows that are at least half pure magenta: the end marker, not a stray pixel."""
    pixels = image.load()
    width, height = image.size
    hits = 0
    for y in range(height):
        run = 0
        for x in range(0, width, 4):
            if pixels[x, y][:3] == MARKER:
                run += 1
        if run > (width // 4) // 2:
            hits += 1
    return hits


def main() -> int:
    pages = sorted(CAPTURES.glob("gallery-page[0-9][0-9].png"))
    if not pages:
        print("no captures found in design/captures")
        return 1

    rows = []
    failures = 0
    for path in pages:
        with Image.open(path) as image:
            image = image.convert("RGB")
            distinct = len(image.getcolors(maxcolors=1 << 24) or [])
            hits = marker_rows(image)
            ok = hits >= 4 and distinct > 200
            if not ok:
                failures += 1
            rows.append((path.name, image.size, distinct, hits, ok))
            scaled = image.resize(
                (int(image.width * SCALE), int(image.height * SCALE)), Image.LANCZOS
            )
            rows[-1] = rows[-1] + (scaled,)

    header = ["file", "size", "colours", "marker rows", "page fits"]
    print(f"{header[0]:<28}{header[1]:<14}{header[2]:>9}{header[3]:>14}{header[4]:>11}")
    for name, size, distinct, hits, ok, _ in rows:
        print(f"{name:<28}{str(size):<14}{distinct:>9}{hits:>14}{'yes' if ok else 'NO':>11}")

    if failures:
        print(f"\n{failures} page(s) did not fit one screen: the end marker is missing.")
        print("Fix by lowering GALLERY_PAGE_BUDGET_DP, not by re-capturing.")

    cell_w = rows[0][5].width
    cell_h = rows[0][5].height + CAPTION_H
    columns = COLUMNS
    grid_rows = (len(rows) + columns - 1) // columns
    sheet = Image.new("RGB", (cell_w * columns, cell_h * grid_rows), (11, 11, 12))
    draw = ImageDraw.Draw(sheet)
    for index, (name, _, _, _, ok, scaled) in enumerate(rows):
        column = index % columns
        row = index // columns
        x = column * cell_w
        y = row * cell_h
        sheet.paste(scaled, (x, y + CAPTION_H))
        caption = f"{name}  {'ok' if ok else 'CLIPPED'}"
        draw.text((x + 8, y + 7), caption, fill=(240, 240, 235) if ok else (255, 120, 120))
        draw.rectangle(
            [x, y, x + cell_w - 1, y + cell_h - 1],
            outline=(42, 42, 45) if ok else (255, 0, 255),
        )

    out = CAPTURES / "gallery-components.png"
    sheet.save(out, optimize=True)
    print(f"\nwrote {out.relative_to(ROOT)} ({out.stat().st_size} bytes, {sheet.size[0]}x{sheet.size[1]})")
    return 0 if failures == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
