import importlib.util
import json
from unittest import mock
import pathlib
import tempfile
import unittest
import zipfile

spec = importlib.util.spec_from_file_location('package_preview', pathlib.Path(__file__).with_name('package_preview_apks.py'))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class PackageNamingTest(unittest.TestCase):
    def test_stable_names_do_not_claim_preview_or_rc(self):
        self.assertEqual(module.apk_name("1.5.0", "a" * 40, ["arm64-v8a"], "stable", "oss", 47999999), "TunXBox-1.5.0-android-tv-phone-arm64-v8a-oss-release-vc47999999-gaaaaaaaa.apk")

    def test_stable_rejects_prerelease_version(self):
        with self.assertRaises(ValueError):
            module.apk_name("1.5.0-rc.120", "a" * 40, ["arm64-v8a"], "stable", "oss", 48000120)

    def test_mismatched_channel_flavor_is_rejected(self):
        with self.assertRaises(ValueError):
            module.apk_name("1.5.0", "a" * 40, ["arm64-v8a"], "stable", "preview", 47999999)

    def test_single_abi(self):
        name = module.apk_name('1.5.0-rc.120', 'a' * 40, ['arm64-v8a'], version_code=48000120)
        self.assertEqual(name, 'TunXBox-1.5.0-rc.120-android-tv-phone-arm64-v8a-preview-release-vc48000120-gaaaaaaaa.apk')

    def test_arm_combined_is_not_all_architectures(self):
        name = module.apk_name('1.5.0-rc.120', 'a' * 40, ['arm64-v8a', 'armeabi-v7a'], version_code=48000120)
        self.assertIn('-arm32-arm64-', name)
        self.assertNotIn('universal', name)

    def test_universal_requires_all_four_abis(self):
        name = module.apk_name('1.5.0-rc.120', 'a' * 40, sorted(module.SUPPORTED_ABIS), version_code=48000120)
        self.assertIn('-universal-', name)

    def test_does_not_duplicate_rc(self):
        self.assertNotIn('-rc-rc-', module.apk_name('1.5.0-rc.120', 'a' * 40, ['armeabi-v7a'], version_code=48000120))

    def test_rejects_bad_sha(self):
        with self.assertRaises(ValueError):
            module.apk_name('1.5.0-rc.120', 'abc', ['arm64-v8a'], version_code=48000120)

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


    def test_unnumbered_rc_names_are_rejected(self):
        for version in ['1.6.0', '1.6.0-rc', '1.6.0-rc.0', '1.6.0-rc.0120']:
            with self.assertRaises(ValueError):
                module.apk_name(version, 'a' * 40, ['arm64-v8a'], version_code=48000120)

    def test_version_code_is_actual_positive_integer(self):
        for code in [None, 0, -1, True, '48000120', 2100000001]:
            with self.assertRaises(ValueError):
                module.apk_name('1.6.0-rc.120', 'a' * 40, ['arm64-v8a'], version_code=code)

    def test_rc_number_must_match_version_code(self):
        with self.assertRaises(ValueError):
            module.apk_name('1.6.0-rc.119', 'a' * 40, ['arm64-v8a'], version_code=48000120)

    def test_invalid_or_duplicate_abis_are_rejected(self):
        for abis in [[], ['mips'], ['arm64-v8a', 'arm64-v8a']]:
            with self.assertRaises(ValueError):
                module.apk_name('1.6.0-rc.120', 'a' * 40, abis, version_code=48000120)

    def package_fixture(self, installed_version, code=48000120):
        tmp = tempfile.TemporaryDirectory()
        self.addCleanup(tmp.cleanup)
        root = pathlib.Path(tmp.name); source = root / 'input'; source.mkdir()
        for index, abis in enumerate([['arm64-v8a'], ['armeabi-v7a'], sorted(module.SUPPORTED_ABIS)]):
            with zipfile.ZipFile(source / f'input-{index}.apk', 'w') as archive:
                archive.writestr('AndroidManifest.xml', 'fixture')
                archive.writestr('classes.dex', 'fixture')
                for abi in abis: archive.writestr(f'lib/{abi}/libcore.so', 'fixture')
        cert = root / 'pin'; cert.write_text('b' * 64)
        output = root / 'output'
        with mock.patch.object(module, 'read_identity', return_value=('com.tunxbox.app', code, installed_version, 'b' * 64)):
            records = module.package(source, output, '1.6.0', 'a' * 40, root / 'unused', root / 'unused', cert, code)
        return records, output

    def test_packaged_names_use_installed_version_and_match_manifest_and_checksums(self):
        records, output = self.package_fixture('1.6.0-rc.120')
        manifest = json.loads((output / 'apk-manifest.json').read_text())
        sums = (output / 'SHA256SUMS.txt').read_text()
        self.assertEqual(manifest['installedVersionName'], '1.6.0-rc.120')
        self.assertEqual(manifest['apks'], records)
        for item in records:
            self.assertIn('-1.6.0-rc.120-', item['name'])
            self.assertIn('-vc48000120-gaaaaaaaa.apk', item['name'])
            self.assertTrue((output / item['name']).is_file())
            self.assertIn(item['sha256'] + '  ' + item['name'], sums)

    def test_package_rejects_wrong_release_series(self):
        with self.assertRaises(ValueError): self.package_fixture('1.7.0-rc.120')

    def test_package_rejects_wrong_installed_rc_number(self):
        with self.assertRaises(ValueError): self.package_fixture('1.6.0-rc.119')


if __name__ == '__main__':
    unittest.main()
