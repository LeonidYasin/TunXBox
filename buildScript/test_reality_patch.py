import importlib.util
import hashlib
import pathlib
import subprocess
import tempfile
import unittest

path = pathlib.Path(__file__).parent / "lib/core/apply_reality_hybrid.py"
spec = importlib.util.spec_from_file_location("reality_patch", path)
module = importlib.util.module_from_spec(spec); spec.loader.exec_module(module)
hash_of = lambda text: hashlib.sha256(text.encode()).hexdigest()

class RealityPatchTest(unittest.TestCase):
    def fixture(self, current="before", head="a" * 40):
        temp = tempfile.TemporaryDirectory(); self.addCleanup(temp.cleanup)
        root = pathlib.Path(temp.name); (root / "file.go").write_text(current)
        manifest = {"sourceCommit": "a" * 40, "files": [{"path": "file.go", "beforeSha256": hash_of("before"), "afterSha256": hash_of("after")}]}
        calls = []
        def runner(args, **kwargs):
            calls.append(args)
            if args[:2] == ["git", "rev-parse"]: return subprocess.CompletedProcess(args, 0, stdout=head)
            if args[:2] == ["git", "apply"] and "--check" not in args: (root / "file.go").write_text("after")
            return subprocess.CompletedProcess(args, 0)
        return root, manifest, calls, runner

    def test_exact_source_applies_and_verifies(self):
        root, manifest, calls, runner = self.fixture()
        self.assertTrue(module.apply(root, manifest, root / "patch", runner))
        self.assertEqual((root / "file.go").read_text(), "after")
        self.assertEqual(len(calls), 3)

    def test_second_application_is_idempotent(self):
        root, manifest, calls, runner = self.fixture("after")
        self.assertFalse(module.apply(root, manifest, root / "patch", runner))
        self.assertEqual(len(calls), 1)

    def test_unexpected_source_is_not_overwritten(self):
        root, manifest, calls, runner = self.fixture("local changes")
        with self.assertRaises(ValueError): module.apply(root, manifest, root / "patch", runner)
        self.assertEqual((root / "file.go").read_text(), "local changes")
        self.assertEqual(len(calls), 1)

    def test_wrong_commit_is_rejected(self):
        root, manifest, calls, runner = self.fixture(head="b" * 40)
        with self.assertRaises(ValueError): module.apply(root, manifest, root / "patch", runner)
        self.assertEqual(len(calls), 1)

    def test_partial_application_is_rejected(self):
        root, manifest, calls, runner = self.fixture("after")
        manifest["files"].append({"path":"new.go", "beforeSha256":None, "afterSha256":hash_of("new")})
        with self.assertRaises(ValueError): module.apply(root, manifest, root / "patch", runner)
        self.assertFalse((root / "new.go").exists())

    def test_path_escape_is_rejected(self):
        root, manifest, calls, runner = self.fixture(); manifest["files"][0]["path"]="../outside.go"
        with self.assertRaises(ValueError): module.apply(root, manifest, root / "patch", runner)
        self.assertEqual(len(calls), 1)

if __name__ == "__main__": unittest.main()
