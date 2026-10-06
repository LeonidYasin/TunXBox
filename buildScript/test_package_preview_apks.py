import importlib.util
import pathlib
import tempfile
import unittest
import zipfile

spec = importlib.util.spec_from_file_location('package_preview', pathlib.Path(__file__).with_name('package_preview_apks.py'))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class PackageNamingTest(unittest.TestCase):
    def test_stable_names_do_not_claim_preview_or_rc(self):
        self.assertEqual(module.apk_name("1.5.0", "a" * 40, ["arm64-v8a"], "stable", "oss"), "TunXBox-1.5.0-android-tv-phone-arm64-v8a-oss-release-aaaaaaaa.apk")

    def test_stable_rejects_prerelease_version(self):
        with self.assertRaises(ValueError):
            module.apk_name("1.5.0-rc", "a" * 40, ["arm64-v8a"], "stable", "oss")

    def test_mismatched_channel_flavor_is_rejected(self):
        with self.assertRaises(ValueError):
            module.apk_name("1.5.0", "a" * 40, ["arm64-v8a"], "stable", "preview")

    def test_single_abi(self):
        name = module.apk_name('1.5.0', 'a' * 40, ['arm64-v8a'])
        self.assertEqual(name, 'TunXBox-1.5.0-rc-android-tv-phone-arm64-v8a-preview-release-aaaaaaaa.apk')

    def test_arm_combined_is_not_all_architectures(self):
        name = module.apk_name('1.5.0', 'a' * 40, ['arm64-v8a', 'armeabi-v7a'])
        self.assertIn('-arm32-arm64-', name)
        self.assertNotIn('universal', name)

    def test_universal_requires_all_four_abis(self):
        name = module.apk_name('1.5.0', 'a' * 40, sorted(module.SUPPORTED_ABIS))
        self.assertIn('-universal-', name)

    def test_does_not_duplicate_rc(self):
        self.assertNotIn('-rc-rc-', module.apk_name('1.5.0-rc', 'a' * 40, ['armeabi-v7a']))

    def test_rejects_bad_sha(self):
        with self.assertRaises(ValueError):
            module.apk_name('1.5.0', 'abc', ['arm64-v8a'])

    def test_reads_real_abis_not_filename(self):
        with tempfile.TemporaryDirectory() as directory:
            path = pathlib.Path(directory) / 'misleading-x86.apk'
            with zipfile.ZipFile(path, 'w') as archive:
                archive.writestr('AndroidManifest.xml', 'test')
                archive.writestr('classes.dex', 'test')
                archive.writestr('lib/arm64-v8a/libcore.so', 'test')
            self.assertEqual(module.apk_abis(path), ['arm64-v8a'])

    def test_rejects_non_apk(self):
        with tempfile.TemporaryDirectory() as directory:
            path = pathlib.Path(directory) / 'bad.apk'
            with zipfile.ZipFile(path, 'w') as archive:
                archive.writestr('text.txt', 'test')
            with self.assertRaises(ValueError):
                module.apk_abis(path)


if __name__ == '__main__':
    unittest.main()
