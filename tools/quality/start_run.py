#!/usr/bin/env python3
"""Record the commit and start time before generating fresh device/test evidence."""
from datetime import datetime, timezone
import json
from pathlib import Path
import subprocess

root = Path(__file__).resolve().parents[2]
report = root / 'build/reports/searchhh/run.json'
report.parent.mkdir(parents=True, exist_ok=True)
report.write_text(json.dumps({
    'sha': subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=root, text=True).strip(),
    'started_at': datetime.now(timezone.utc).isoformat(),
}, indent=2) + '\n')
