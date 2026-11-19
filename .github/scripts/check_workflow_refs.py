#!/usr/bin/env python3
"""Static checks over .github/workflows/*.yml.

Currently checks:
- Every repo script a workflow calls (`scripts/...` or `.github/scripts/...`
  ending in .sh or .py) exists in the checkout. A workflow that calls a missing
  script fails at run time with exit 127, after the job has already started.

Usage:
    python3 .github/scripts/check_workflow_refs.py [--root <repo-root>]

Exit codes:
    0 - all references resolve
    1 - one or more references are missing
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

# Matches repo script paths, with an optional leading `../` (used by jobs whose
# working-directory is a subfolder) and an optional `.github/` prefix.
SCRIPT_REF_RE = re.compile(r"(?:\.\./)*((?:\.github/)?scripts/[\w./-]+\.(?:sh|py))")


def workflow_files(root: Path):
    return sorted((root / ".github" / "workflows").glob("*.yml"))


def find_missing_script_refs(root: Path):
    problems = []
    for workflow in workflow_files(root):
        text = workflow.read_text(encoding="utf-8")
        for line_no, line in enumerate(text.splitlines(), start=1):
            for match in SCRIPT_REF_RE.finditer(line):
                rel = match.group(1)
                if not (root / rel).is_file():
                    problems.append(
                        f"{workflow.relative_to(root)}:{line_no}: calls '{rel}', which does not exist"
                    )
    return problems


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument(
        "--root",
        type=Path,
        default=Path(__file__).resolve().parents[2],
        help="repository root (default: the checkout containing this script)",
    )
    args = parser.parse_args(argv)

    root = args.root.resolve()
    problems = find_missing_script_refs(root)
    for problem in problems:
        print(f"::error::{problem}")
    if problems:
        print(f"{len(problems)} workflow reference(s) point to missing scripts.")
        return 1
    print("OK: every script called from a workflow exists.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
