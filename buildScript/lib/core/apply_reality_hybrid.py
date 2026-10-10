"""Apply the reviewed REALITY patch only to the exact pinned source; never reset local changes."""
import hashlib
import json
import pathlib
import subprocess
import sys

HERE = pathlib.Path(__file__).resolve().parent

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest() if path.is_file() else None

def apply(source, manifest, patch, runner=subprocess.run):
    source = pathlib.Path(source).resolve()
    head = runner(["git", "rev-parse", "HEAD"], cwd=source, check=True, capture_output=True, text=True).stdout.strip()
    if head != manifest["sourceCommit"]: raise ValueError("REALITY patch requires exact pinned core commit")
    states = []
    for record in manifest["files"]:
        path = (source / record["path"]).resolve()
        if not path.is_relative_to(source): raise ValueError("Patch path escapes source")
        actual = digest(path)
        if actual == record["afterSha256"]: states.append("after")
        elif actual == record["beforeSha256"]: states.append("before")
        else: raise ValueError("Unexpected source contents; refusing to overwrite local changes")
    if all(state == "after" for state in states): return False
    if not all(state == "before" for state in states): raise ValueError("Partially applied REALITY patch")
    runner(["git", "apply", "--check", str(pathlib.Path(patch).resolve())], cwd=source, check=True)
    runner(["git", "apply", str(pathlib.Path(patch).resolve())], cwd=source, check=True)
    for record in manifest["files"]:
        if digest(source / record["path"]) != record["afterSha256"]: raise ValueError("Patched source checksum mismatch")
    return True

if __name__ == "__main__":
    source = pathlib.Path(sys.argv[1]) if len(sys.argv) == 2 else HERE.parents[2].parent / "sing-box"
    changed = apply(source, json.loads((HERE / "reality-hybrid.json").read_text()), HERE / "reality-hybrid.patch")
    print("Pinned REALITY hybrid patch verified" + (" and applied" if changed else " (already applied)"))
