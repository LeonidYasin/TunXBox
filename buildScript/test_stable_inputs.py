import importlib.util
import pathlib
import tempfile
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('stable_inputs', ROOT / 'buildScript/validate_stable_inputs.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class StableInputsTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = pathlib.Path(self.tmp.name)
        (self.root / 'docs/releases').mkdir(parents=True)
        self.properties = self.root / 'nb4a.properties'
        self.base = 'PACKAGE_NAME=com.tunxbox.app\nVERSION_NAME=1.6.0\nVERSION_CODE=48\n'
        self.properties.write_text(self.base)
        self.notes = self.root / 'docs/releases/1.6.0.md'
        self.notes.write_text('# TunXBox 1.6.0\n\nActual release changes.\n')
        self.pin = self.root / 'signing-certificate.sha256'
        self.pin.write_text('a' * 64 + '\n')

    def test_valid_metadata_without_network_tag_or_private_key(self):
        result = module.validate(self.root)
        self.assertEqual(48999999, result['versionCode'])
        self.assertEqual('docs/releases/1.6.0.md', result['notes'])
        self.assertFalse(result['publishes'])

    def test_missing_empty_title_only_and_wrong_version_notes_fail(self):
        self.notes.unlink()
        with self.assertRaises(ValueError): module.validate(self.root)
        for text in ['', '# TunXBox 1.6.0\n', '# TunXBox 1.5.0\nBody']:
            with self.subTest(text=text):
                self.notes.write_text(text)
                with self.assertRaises(ValueError): module.validate(self.root)

    def test_rc_noncanonical_and_path_versions_fail(self):
        for version in ['1.6.0-rc.158', '01.6.0', '1.6', '../../secret', 'v1.6.0', '1.6.0/']:
            with self.subTest(version=version):
                self.properties.write_text(self.base.replace('1.6.0', version))
                with self.assertRaises(ValueError): module.validate(self.root)

    def test_wrong_or_missing_package_fails(self):
        for text in [self.base.replace('com.tunxbox.app', 'other.package'), self.base.split('\n', 1)[1]]:
            self.properties.write_text(text)
            with self.assertRaises(ValueError): module.validate(self.root)

    def test_duplicate_required_properties_fail(self):
        for key, value in [('PACKAGE_NAME', 'com.tunxbox.app'), ('VERSION_NAME', '1.6.0'), ('VERSION_CODE', '48')]:
            self.properties.write_text(self.base + key + '=' + value + '\n')
            with self.assertRaises(ValueError): module.validate(self.root)

    def test_invalid_or_out_of_range_version_code_fails(self):
        for code in ['0', '-1', '048', 'true', '2100', '48.0']:
            with self.subTest(code=code):
                self.properties.write_text(self.base.replace('VERSION_CODE=48', 'VERSION_CODE=' + code))
                with self.assertRaises(ValueError): module.validate(self.root)

    def test_invalid_public_certificate_fails(self):
        for pin in ['', 'a' * 63, 'z' * 64, 'a' * 65]:
            self.pin.write_text(pin)
            with self.assertRaises(ValueError): module.validate(self.root)

    def test_external_notes_symlink_fails(self):
        self.notes.unlink()
        with tempfile.TemporaryDirectory() as external:
            target = pathlib.Path(external) / 'notes'
            target.write_text('# TunXBox 1.6.0\nExternal body\n')
            self.notes.symlink_to(target)
            with self.assertRaises(ValueError): module.validate(self.root)

    def test_workflow_validates_inputs_before_expensive_sdk_setup(self):
        workflow = (ROOT / '.github/workflows/stable-release.yml').read_text()
        self.assertLess(workflow.index('python3 buildScript/validate_stable_inputs.py'), workflow.index('- name: Setup Android SDK'))
        self.assertIn('python3 buildScript/test_stable_inputs.py', workflow)


if __name__ == '__main__':
    unittest.main()
