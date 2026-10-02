#!/usr/bin/env python3
"""Resilient GitHub Actions artifact/log downloader.

GitHub returns a short-lived redirect for artifacts and job logs.  The redirect
can intermittently terminate with EOF, especially for larger archives.  This
client retries the API lookup and the redirected stream, validates the result,
and never prints signed URLs or credentials.
"""
from __future__ import annotations

import argparse
import io
import json
import os
import sys
import time
import urllib.error
import urllib.request
import zipfile
from pathlib import Path

API = "https://api.github.com"


def request(url: str, token: str, attempts: int = 6) -> bytes:
    last: Exception | None = None
    for attempt in range(attempts):
        req = urllib.request.Request(
            url,
            headers={
                "Accept": "application/vnd.github+json",
                "Authorization": f"Bearer {token}",
                "User-Agent": "SEARCHHH-quality-evidence-downloader",
            },
        )
        try:
            with urllib.request.urlopen(req, timeout=90) as response:
                return response.read()
        except (urllib.error.URLError, TimeoutError, OSError) as exc:
            last = exc
            if attempt + 1 < attempts:
                time.sleep(min(30, 2**attempt))
    raise RuntimeError(f"GitHub download failed after {attempts} attempts: {last}")


def api_json(url: str, token: str) -> dict:
    return json.loads(request(url, token).decode("utf-8"))


def download_artifact(repo: str, run_id: str, name: str, output: Path, token: str) -> None:
    artifacts = api_json(f"{API}/repos/{repo}/actions/runs/{run_id}/artifacts?per_page=100", token)
    matches = [a for a in artifacts.get("artifacts", []) if a.get("name") == name]
    if not matches:
        raise RuntimeError(f"artifact not found: {name}")
    if len(matches) > 1:
        raise RuntimeError(f"artifact name is ambiguous: {name}")
    artifact = matches[0]
    if artifact.get("expired"):
        raise RuntimeError(f"artifact has expired: {name}")
    blob = request(artifact["archive_download_url"], token)
    try:
        with zipfile.ZipFile(io.BytesIO(blob)) as archive:
            bad = archive.testzip()
            if bad:
                raise RuntimeError(f"downloaded artifact is corrupt at {bad}")
            output.parent.mkdir(parents=True, exist_ok=True)
            root = output.resolve()
            for member in archive.infolist():
                target = (output / member.filename).resolve()
                if root != target and root not in target.parents:
                    raise RuntimeError(f"artifact contains unsafe path: {member.filename}")
            archive.extractall(output)
    except zipfile.BadZipFile as exc:
        raise RuntimeError("downloaded artifact was not a complete ZIP after retries") from exc


def download_logs(repo: str, job_id: str, output: Path, token: str) -> None:
    blob = request(f"{API}/repos/{repo}/actions/jobs/{job_id}/logs", token)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_bytes(blob)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--repo", required=True, help="owner/name")
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--run-id")
    group.add_argument("--job-id")
    parser.add_argument("--artifact")
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    token = os.environ.get("GH_TOKEN") or os.environ.get("GITHUB_TOKEN")
    if not token:
        print("GH_TOKEN or GITHUB_TOKEN is required", file=sys.stderr)
        return 2
    try:
        if args.run_id:
            if not args.artifact:
                parser.error("--artifact is required with --run-id")
            download_artifact(args.repo, args.run_id, args.artifact, args.output, token)
        else:
            download_logs(args.repo, args.job_id, args.output, token)
    except RuntimeError as exc:
        print(str(exc), file=sys.stderr)
        return 1
    print(args.output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
