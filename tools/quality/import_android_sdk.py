#!/usr/bin/env python3
"""Locate or safely import a stripped Android SDK without downloading or committing it.

This is an environment preparation/reporting tool. It never trusts an SDK merely
because a directory exists: platform, build-tools, platform-tools and Java are
checked explicitly. Archives are extracted with path traversal protection.
"""
from __future__ import annotations

import argparse
import json
import os
from pathlib import Path
import shutil
import tarfile
import zipfile

ROOT = Path(__file__).resolve().parents[2]
DEFAULT_DEST = Path.home() / ".cache" / "searchhh" / "android-sdk"
REQUIRED_PLATFORM = "android-36"


def candidates() -> list[Path]:
    values = [os.environ.get("ANDROID_SDK_ROOT"), os.environ.get("ANDROID_HOME"),
              "/opt/android-sdk", "/opt/android-sdk-linux", str(Path.home() / "android-sdk"),
              str(Path.home() / ".android" / "sdk")]
    return [Path(value).expanduser() for value in values if value]


def java_home() -> Path | None:
    configured = os.environ.get("JAVA_HOME")
    if configured and (Path(configured) / "bin" / "java").is_file():
        return Path(configured)
    java = shutil.which("java")
    return Path(java).parent.parent if java else None


def validate(sdk: Path) -> dict:
    platform = sdk / "platforms" / REQUIRED_PLATFORM
    build_tools = sorted((sdk / "build-tools").glob("*/aapt2")) if (sdk / "build-tools").is_dir() else []
    adb = sdk / "platform-tools" / "adb"
    sdkmanager = next(iter(sorted(sdk.glob("cmdline-tools/*/bin/sdkmanager"))), None)
    checks = {
        "sdk_directory": sdk.is_dir(),
        "platform_android_36": (platform / "android.jar").is_file(),
        "build_tools_aapt2": bool(build_tools),
        "platform_tools_adb": adb.is_file(),
        "sdkmanager": bool(sdkmanager and sdkmanager.is_file()),
        "java": java_home() is not None,
    }
    return {"path": str(sdk), "checks": checks, "ready": all(checks.values()),
            "java_home": str(java_home()) if java_home() else None,
            "selected_build_tools": str(build_tools[-1].parent) if build_tools else None}


def safe_members(members):
    for member in members:
        name = Path(member.name)
        if name.is_absolute() or ".." in name.parts:
            raise ValueError(f"unsafe archive member: {member.name}")
    return members


def import_archive(source: Path, destination: Path) -> Path:
    destination.mkdir(parents=True, exist_ok=True)
    if source.suffix == ".zip":
        with zipfile.ZipFile(source) as archive:
            names = [Path(item.filename) for item in archive.infolist()]
            if any(name.is_absolute() or ".." in name.parts for name in names):
                raise ValueError("unsafe zip path")
            archive.extractall(destination)
    elif source.name.endswith((".tar.gz", ".tgz", ".tar")):
        with tarfile.open(source) as archive:
            archive.extractall(destination, members=safe_members(archive.getmembers()))
    else:
        raise ValueError("SDK source must be a directory, .zip, .tar, .tar.gz or .tgz")
    direct = destination / "platforms"
    if direct.is_dir():
        return destination
    nested = [path for path in destination.iterdir() if path.is_dir() and (path / "platforms").is_dir()]
    if len(nested) == 1:
        return nested[0]
    return destination


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, help="SDK directory or stripped SDK archive")
    parser.add_argument("--destination", type=Path, default=DEFAULT_DEST)
    parser.add_argument("--report", type=Path, default=ROOT / "build/reports/android-sdk.json")
    args = parser.parse_args()
    errors: list[str] = []
    sdk: Path | None = None
    try:
        if args.source:
            source = args.source.expanduser().resolve()
            if source.is_dir():
                sdk = source
            elif source.is_file():
                sdk = import_archive(source, args.destination.expanduser().resolve())
            else:
                errors.append(f"source does not exist: {source}")
        else:
            for candidate in candidates():
                if validate(candidate)["ready"]:
                    sdk = candidate
                    break
            if sdk is None:
                errors.append("no complete stripped SDK found in configured or standard locations")
        result = validate(sdk) if sdk else {"path": None, "checks": {}, "ready": False}
    except (OSError, ValueError, tarfile.TarError, zipfile.BadZipFile) as error:
        errors.append(f"{type(error).__name__}: {error}")
        result = {"path": str(sdk) if sdk else None, "checks": {}, "ready": False}
    result.update({"schema_version": 1, "errors": errors, "network_used": False,
                   "release_approved": False})
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(result, indent=2) + "\n")
    print(json.dumps(result, indent=2))
    return 0 if result["ready"] else 2


if __name__ == "__main__":
    raise SystemExit(main())
