import base64, hashlib, importlib.util, json, pathlib, tempfile, unittest, os, subprocess
from unittest.mock import patch
from types import SimpleNamespace

def load(name):
    spec=importlib.util.spec_from_file_location(name,pathlib.Path(__file__).with_name(name+'.py'));module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module);return module
signing=load('signing_bundle');upgrade=load('verify_update');package=load('package_preview_apks')

class UpgradeTest(unittest.TestCase):
    def current(self, **kw): return dict(applicationId='com.tunxbox.app', signingCertificateSha256='a'*64, versionCode=47000012, **kw)
    def test_matching_signer_increasing_version(self):
        old=self.current();old['versionCode']-=1
        self.assertTrue(upgrade.verify(old,self.current()))
    def test_different_signer_is_rejected_even_with_legacy_flag(self):
        old=self.current();old['versionCode']-=1;old['signingCertificateSha256']='b'*64
        with self.assertRaises(ValueError):upgrade.verify(old,self.current(),True)
    def test_downgrade_rejected(self):
        with self.assertRaises(ValueError):upgrade.verify(self.current(),self.current())
    def test_legacy_requires_explicit_migration(self):
        with self.assertRaises(ValueError):upgrade.verify({},self.current())
        self.assertFalse(upgrade.verify({},self.current(),True))
    def test_application_id_change_rejected(self):
        new=self.current();new['applicationId']='other'
        with self.assertRaises(ValueError):upgrade.verify({},new,True)
    def test_prepare_matches_actual_certificate_and_private_permissions(self):
        with tempfile.TemporaryDirectory() as directory:
            root=pathlib.Path(directory);pin=root/'pin';pin.write_text(hashlib.sha256(b'public cert').hexdigest())
            bundle=json.dumps(dict(keystore=base64.b64encode(b'private key').decode(),storePassword='test-password',alias='tunxbox',keyPassword='test-password'))
            with patch.object(signing.subprocess,'run',return_value=SimpleNamespace(returncode=0,stdout=b'public cert')):
                env=signing.prepare(bundle,root,pin)
            self.assertEqual(env['TUNXBOX_REQUIRE_SIGNING'],'1');self.assertEqual((root/'.signing/release.keystore').stat().st_mode & 0o777,0o600)
    def test_wrong_certificate_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            root=pathlib.Path(directory);pin=root/'pin';pin.write_text('a'*64)
            bundle=json.dumps(dict(keystore='dGVzdA==',storePassword='password',alias='a',keyPassword='password'))
            with patch.object(signing.subprocess,'run',return_value=SimpleNamespace(returncode=0,stdout=b'wrong cert')):
                with self.assertRaises(ValueError): signing.prepare(bundle,root,pin)
    def test_apk_identity_is_extracted_from_actual_tools(self):
        with patch.object(package.subprocess,'check_output',side_effect=['Signer #1 certificate SHA-256 digest: '+'a'*64,"package: name='com.tunxbox.app' versionCode='47000123' versionName='1.5.0-rc.123'\n"]):
            self.assertEqual(package.read_identity(pathlib.Path('app.apk'),'apksigner','aapt'),('com.tunxbox.app',47000123,'1.5.0-rc.123','a'*64))

    def test_compile_sdk_codename_cannot_overwrite_application_name(self):
        metadata = "package: name='com.tunxbox.app' versionCode='47000090' versionName='1.5.0-rc.90' platformBuildVersionName='15' compileSdkVersion='35' compileSdkVersionCodename='15'\n"
        with patch.object(package.subprocess, 'check_output', side_effect=['Signer #1 certificate SHA-256 digest: ' + 'a' * 64, metadata]):
            self.assertEqual(package.read_identity(pathlib.Path('app.apk'), 'apksigner', 'aapt'), ('com.tunxbox.app', 47000090, '1.5.0-rc.90', 'a' * 64))

    def test_real_pkcs12_bundle_can_be_prepared_without_printing_secrets(self):
        with tempfile.TemporaryDirectory() as directory:
            root=pathlib.Path(directory);key=root/'test.p12';password='synthetic-local-test'
            env=dict(os.environ,SIGN_TEST_PASS=password)
            subprocess.run(['keytool','-genkeypair','-storetype','PKCS12','-keystore',str(key),'-alias','test','-keyalg','RSA','-keysize','2048','-validity','1','-dname','CN=LocalTest','-storepass:env','SIGN_TEST_PASS','-keypass:env','SIGN_TEST_PASS'],env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL,check=True)
            cert=subprocess.check_output(['keytool','-exportcert','-keystore',str(key),'-alias','test','-storepass:env','SIGN_TEST_PASS'],env=env,stderr=subprocess.DEVNULL)
            pin=root/'pin';pin.write_text(hashlib.sha256(cert).hexdigest())
            bundle=json.dumps(dict(keystore=base64.b64encode(key.read_bytes()).decode(),storePassword=password,alias='test',keyPassword=password))
            prepared=signing.prepare(bundle,root,pin)
            self.assertTrue(pathlib.Path(prepared['TUNXBOX_SIGNING_KEYSTORE']).is_file())

if __name__=='__main__':unittest.main()
