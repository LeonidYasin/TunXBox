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


def read_identity(source, apksigner, aapt):
    output = subprocess.check_output([str(apksigner), "verify", "--print-certs", str(source)], text=True)
    fingerprints = re.findall(r"Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]+)", output)
    if len(fingerprints) != 1 or len(fingerprints[0]) != 64:
        raise ValueError("Expected exactly one APK signing certificate")
    metadata = subprocess.check_output([str(aapt), "dump", "badging", str(source)], text=True).splitlines()[0]
    fields = dict(re.findall(r"(?:^|\s)(name|versionCode|versionName)='([^']*)'", metadata))
    return fields['name'], int(fields['versionCode']), fields['versionName'], fingerprints[0].lower()


def package(apk_dir, output_dir, version, commit, apksigner, aapt, certificate, expected_version_code):
    sources = sorted(apk_dir.glob("*.apk"))
    if not sources:
        raise ValueError(f"No release APKs in {apk_dir}")
    if output_dir.exists() and any(output_dir.iterdir()):
        raise ValueError("Output directory must be empty")
    output_dir.mkdir(parents=True, exist_ok=True)
    expected_cert = certificate.read_text().strip().lower()
    if not re.fullmatch("[0-9a-f]{64}", expected_cert): raise ValueError("Invalid pinned certificate")
    records = []
    identity = None
    seen = set()
    for source in sources:
        # All published APKs must be installable, not old unsigned artifacts.
        app_id, code, installed_version, fingerprint = read_identity(source, apksigner, aapt)
        if app_id != "com.tunxbox.app" or fingerprint != expected_cert or code != expected_version_code:
            raise ValueError("APK application, signing certificate or versionCode is incompatible")
        current_identity = (app_id, code, installed_version, fingerprint)
        if identity is not None and identity != current_identity:
            raise ValueError("Split APKs have inconsistent upgrade identities")
        identity = current_identity
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
        "flavor": "preview", "buildType": "release", "signatureVerified": True,
        "applicationId": identity[0], "versionCode": identity[1], "installedVersionName": identity[2],
        "signingCertificateSha256": identity[3], "apks": records
    }, indent=2) + '\n', encoding='utf-8')
    return records


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apk-dir', type=pathlib.Path, required=True)
    parser.add_argument('--output-dir', type=pathlib.Path, required=True)
    parser.add_argument('--version', required=True)
    parser.add_argument('--commit', required=True)
    parser.add_argument('--apksigner', type=pathlib.Path, required=True)
    parser.add_argument('--aapt', type=pathlib.Path, required=True)
    parser.add_argument('--certificate', type=pathlib.Path, required=True)
    parser.add_argument('--expected-version-code', type=int, required=True)
    args = parser.parse_args()
    for record in package(args.apk_dir, args.output_dir, args.version, args.commit, args.apksigner, args.aapt, args.certificate, args.expected_version_code):
        print(f"Verified: {record['name']} ({', '.join(record['abis'])})")
