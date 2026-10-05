#!/usr/bin/env python3
"""Package verified APKs under their actual download names (never GitHub labels)."""
import argparse
import hashlib
import json
import pathlib
import re
import shutil
import subprocess
import zipfile

SUPPORTED_ABIS = {"armeabi-v7a", "arm64-v8a", "x86", "x86_64"}


def apk_abis(path):
    with zipfile.ZipFile(path) as archive:
        names = archive.namelist()
        if "AndroidManifest.xml" not in names or "classes.dex" not in names:
            raise ValueError(f"Not an application APK: {path.name}")
        abis = {name.split('/')[1] for name in names
                if name.startswith("lib/") and name.endswith(".so") and len(name.split('/')) >= 3}
    if not abis or not abis <= SUPPORTED_ABIS:
        raise ValueError(f"Missing or unsupported native ABIs: {path.name}: {sorted(abis)}")
    return sorted(abis)


def apk_name(version, commit, abis):
    if not re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+(?:[-+][A-Za-z0-9.-]+)?", version):
        raise ValueError("Invalid version")
    if not re.fullmatch(r"[0-9a-f]{40}", commit):
        raise ValueError("Expected full lowercase commit SHA")
    if set(abis) == SUPPORTED_ABIS:
        architecture = "universal"
    elif set(abis) == {"armeabi-v7a", "arm64-v8a"}:
        architecture = "arm32-arm64"
    elif len(abis) == 1:
        architecture = abis[0]
    else:
        architecture = "_".join(sorted(abis))
    release_version = version if "-" in version else version + "-rc"
    return f"TunXBox-{release_version}-android-tv-phone-{architecture}-preview-release-{commit[:8]}.apk"


def package(apk_dir, output_dir, version, commit, apksigner):
    sources = sorted(apk_dir.glob("*.apk"))
    if not sources:
        raise ValueError(f"No release APKs in {apk_dir}")
    if output_dir.exists() and any(output_dir.iterdir()):
        raise ValueError("Output directory must be empty")
    output_dir.mkdir(parents=True, exist_ok=True)
    records = []
    seen = set()
    for source in sources:
        # All published APKs must be installable, not old unsigned artifacts.
        subprocess.run([str(apksigner), "verify", str(source)], check=True,
                       stdout=subprocess.DEVNULL)
        abis = apk_abis(source)
        name = apk_name(version, commit, abis)
        if name in seen:
            raise ValueError(f"Ambiguous duplicate ABI output: {name}")
        seen.add(name)
        target = output_dir / name
        shutil.copyfile(source, target)
        with target.open('rb') as packaged:
            digest = hashlib.file_digest(packaged, "sha256").hexdigest()
        records.append({"name": name, "abis": abis, "sha256": digest, "size": target.stat().st_size})
    # Gradle emits two ARM splits. Its universal output includes all bundled native
    # libraries, not just split filters: inspect bytes instead of assuming ARM-only.
    sets = {frozenset(r["abis"]) for r in records}
    arm_splits = {frozenset(["armeabi-v7a"]), frozenset(["arm64-v8a"])}
    combined = sets - arm_splits
    if not arm_splits <= sets or len(combined) != 1 or not frozenset(["armeabi-v7a", "arm64-v8a"]) <= next(iter(combined)):
        raise ValueError("Expected both ARM splits and one combined APK; do not publish an incomplete/mislabelled set")
    (output_dir / "SHA256SUMS.txt").write_text(
        ''.join(f"{r['sha256']}  {r['name']}\n" for r in records), encoding='utf-8')
    (output_dir / "apk-manifest.json").write_text(json.dumps({
        "version": version, "commit": commit, "platform": "android", "ui": ["tv", "phone"],
        "flavor": "preview", "buildType": "release", "signatureVerified": True, "apks": records
    }, indent=2) + '\n', encoding='utf-8')
    return records


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apk-dir', type=pathlib.Path, required=True)
    parser.add_argument('--output-dir', type=pathlib.Path, required=True)
    parser.add_argument('--version', required=True)
    parser.add_argument('--commit', required=True)
    parser.add_argument('--apksigner', type=pathlib.Path, required=True)
    args = parser.parse_args()
    for record in package(args.apk_dir, args.output_dir, args.version, args.commit, args.apksigner):
        print(f"Verified: {record['name']} ({', '.join(record['abis'])})")
