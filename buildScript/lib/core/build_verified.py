"""Reproducible native build entry: pinned source, verified patch, Go tests, then Android JNI."""
import pathlib
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parents[3]
if __name__ == "__main__":
    subprocess.run(["bash", "buildScript/lib/core/init.sh"], cwd=ROOT, check=True)
    source = ROOT.parent / "sing-box"
    subprocess.run([sys.executable, "buildScript/lib/core/apply_reality_hybrid.py", str(source)], cwd=ROOT, check=True)
    subprocess.run(["go", "test", "-tags", "with_utls", "./common/tls"], cwd=source, check=True)
    subprocess.run(["bash", "buildScript/lib/core/build.sh"], cwd=ROOT, check=True)
