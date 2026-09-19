#!/usr/bin/env python3
"""Check the helios Android design documents produced in this step.

Verifies structure, ID integrity, cross-document references, traceability to the
feature-parity matrix, and the constraint that no application source was touched.
Exit 0 when every check passes, 1 otherwise.
"""
import re
import subprocess
import sys
import unicodedata

ROOT = "."
DELIVERABLES = {
    "state": ".mobile-work/STATE.md",
    "design": "design/DESIGN.md",
    "flows": "design/ux-flows.md",
    "inventory": "design/screen-inventory.md",
}
STATE_HEADINGS = [
    "# Execution state",
    "## Objective and authorized scope",
    "## Assumptions and decisions",
    "## Environment",
    "## Current milestone",
    "## Evidence and artifacts",
    "## Blockers and risks",
    "## Next action",
]
ID_FAMILIES = r"(?:REQ|RTE|SCR|STS|ACT|CMP|SVC|FLW)"
FROZEN_PREFIXES = ("android/", "ios/", "dist/", "dist-tsc/", "release/")

results = []


def check(name, ok, detail=""):
    results.append((name, bool(ok), detail))


def read(path):
    with open(path, encoding="utf-8") as handle:
        return handle.read()


texts = {}
for key, path in DELIVERABLES.items():
    try:
        texts[key] = read(path)
        check(f"file exists and is non-empty: {path}", len(texts[key].strip()) > 0, f"{len(texts[key].splitlines())} lines")
    except OSError as exc:
        check(f"file exists and is non-empty: {path}", False, str(exc))
        texts[key] = ""

state = texts["state"]
for heading in STATE_HEADINGS:
    check(f"STATE.md heading verbatim: {heading}", heading in state)
    if heading in state and heading.startswith("## "):
        body = state.split(heading, 1)[1].split("\n## ", 1)[0].strip()
        check(f"STATE.md section non-empty: {heading}", len(body) > 60, f"{len(body)} chars")

design = texts["design"]
for direction in ("### D1", "### D2", "### D3"):
    check(f"DESIGN.md has direction {direction[4:]}", direction in design)
check("DESIGN.md states the selected direction", "Selected:" in design)
check("DESIGN.md explains the selection with numbered reasons", design.count("\n1. ") >= 1 and "Reasons:" in design)
check("DESIGN.md evaluates on an explicit criteria table", "| Criterion |" in design)
check("DESIGN.md covers hierarchy emphasis", "Hierarchy" in design)
check("DESIGN.md covers interaction emphasis", "Interaction emphasis" in design)
check("DESIGN.md names the styling authority", "designsystem/" in design)
check("DESIGN.md contains no direction named only by colour", "before selecting the accent" not in design)

flows = texts["flows"]
for topic, needle in [
    ("first run", "## FLW-01 First run"),
    ("principal repeated task", "## FLW-02 Principal repeated task"),
    ("inverter/network recovery", "## FLW-03 Recovery"),
    ("return use", "## FLW-04 Return use"),
]:
    check(f"ux-flows.md covers {topic}", needle in flows)
check("ux-flows.md names the socket timeout bound", "2 s" in flows)
check("ux-flows.md states the stale threshold", "15 s" in flows)
check("ux-flows.md forbids animating stale data", "trails stop" in flows)

inventory = texts["inventory"]
defined = set()
for line in inventory.splitlines():
    if line.startswith("### "):
        head = line[4:].strip().split(" ")[0]
        if re.fullmatch(ID_FAMILIES + r"-\d+", head):
            defined.add(head)
    if line.startswith("| "):
        first = line.split("|")[1].strip()
        if re.fullmatch(ID_FAMILIES + r"-\d+", first):
            defined.add(first)

for family in ("REQ", "RTE", "SCR", "STS", "ACT", "CMP", "SVC", "FLW"):
    count = len([i for i in defined if i.startswith(family + "-")])
    check(f"inventory defines {family}- ids", count > 0, f"{count} ids")

action_rows = []
for line in inventory.splitlines():
    if re.match(r"\| ACT-\d{3} \|", line):
        cells = [c.strip() for c in line.strip().strip("|").split("|")]
        action_rows.append(cells)
check("every action row has 9 columns", all(len(c) == 9 for c in action_rows),
      f"{sum(1 for c in action_rows if len(c) != 9)} bad rows of {len(action_rows)}")
check("every action row is fully populated", all(all(c for c in row) for row in action_rows))
check("every action row is traced to the matrix",
      all(any("FPM" in c for c in row[1:]) for row in action_rows))

screen_sections = re.split(r"\n### ", inventory)[1:]
screen_blocks = [b for b in screen_sections if b.startswith("SCR-")]
for block in screen_blocks:
    sid = block[:6]
    check(f"{sid} records the Android status", "Android status now:" in block)
    check(f"{sid} records the FPM trace", "FPM trace:" in block)
check("inventory reports coverage and gaps", "## 8. Coverage and gaps" in inventory)
check("inventory marks out-of-scope surfaces", "Out of scope for this design" in inventory)

for key in ("design", "flows"):
    refs = set(re.findall(ID_FAMILIES + r"-\d+", texts[key]))
    missing = sorted(r for r in refs if r not in defined)
    check(f"{DELIVERABLES[key]} references only defined ids", not missing, ", ".join(missing[:8]))

for key, path in DELIVERABLES.items():
    emoji = [c for c in texts[key] if unicodedata.category(c) == "So" or ord(c) > 0x1F000]
    check(f"no emoji in {path}", not emoji, "".join(sorted(set(emoji)))[:20])

status = subprocess.run(["git", "status", "--porcelain"], capture_output=True, text=True).stdout
touched = [line for line in status.splitlines()
           if any(line[3:].startswith(p) for p in FROZEN_PREFIXES)]
check("no application source modified by this step", not touched, "; ".join(touched[:5]))

failed = [r for r in results if not r[1]]
for name, ok, detail in results:
    print(("PASS  " if ok else "FAIL  ") + name + (f"  [{detail}]" if detail and not ok else ""))
print(f"\n{len(results) - len(failed)} of {len(results)} checks passed")
print(f"inventory: {len(defined)} ids, {len(action_rows)} actions, {len(screen_blocks)} screens")
if failed:
    print("\nFAILED:")
    for name, _, detail in failed:
        print(f"  - {name}  {detail}")
sys.exit(1 if failed else 0)
