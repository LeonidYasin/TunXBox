#!/usr/bin/env python3
"""Fail closed: device artifacts must not mutate public tags/releases."""
import os,pathlib,re,subprocess,unittest
ROOT=pathlib.Path(__file__).resolve().parents[1]
TEXT=(ROOT/'.github/workflows/prerelease-from-pr.yml').read_text()
def step(name):
 marker='      - name: '+name+'\n'
 return TEXT.split(marker,1)[1].split('      - name:',1)[0]
class ArtifactModeTests(unittest.TestCase):
 def test_signed_packages_and_upload_still_require_non_check_mode(self):
  for name in ('Prepare pinned signing identity','Package verified APKs','Upload APKs (artifact)'):
   self.assertEqual("        if: github.event.inputs.check_only != 'true'",step(name).splitlines()[0])
  upload=step('Upload APKs (artifact)');self.assertIn('path: dist/*',upload);self.assertNotIn('.signing',upload)
 def test_public_mutations_exclude_artifact_mode(self):
  for name in ('Publish rolling pre-release to GitHub Releases','Comment on PR with RC link'):
   condition=step(name).splitlines()[0];self.assertIn("github.event.inputs.artifact_only != 'true'",condition);self.assertIn("github.event.inputs.check_only != 'true'",condition)
  recovery=TEXT.split('  recover-verified-rc158:\n',1)[1].split('  build-and-publish-prerelease:',1)[0]
  self.assertIn("github.event.inputs.artifact_only != 'true'",recovery)
 def test_guard_rejects_conflicting_or_cross_checkout_modes(self):
  script=step('Validate artifact-only mode').split('        run: |\n',1)[1];script='\n'.join(line[10:] for line in script.splitlines())
  env=dict(os.environ,ARTIFACT_ONLY='true',CHECK_ONLY='false',RECOVER='false',LEGACY='false',SOURCE_REF='',GITHUB_EVENT_NAME='workflow_dispatch')
  self.assertEqual(0,subprocess.run(['bash','-c',script],env=env,capture_output=True).returncode)
  for key,value in [('CHECK_ONLY','true'),('RECOVER','true'),('LEGACY','true'),('SOURCE_REF','master'),('GITHUB_EVENT_NAME','pull_request')]:
   with self.subTest(key=key):self.assertNotEqual(0,subprocess.run(['bash','-c',script],env=dict(env,**{key:value}),capture_output=True).returncode)
 def test_all_gates_remain_before_signer(self):
  signer=TEXT.index('      - name: Prepare pinned signing identity')
  for name in ('Build debug instrumentation APKs','Test protocol and remote UI','Android 15 emulator smoke (4KB)','Android 16 emulator smoke (16KB)'):
   self.assertLess(TEXT.index('      - name: '+name),signer)
   self.assertNotIn('continue-on-error',step(name))
  self.assertIn('4096',step('Android 15 emulator smoke (4KB)'));self.assertIn('16384',step('Android 16 emulator smoke (16KB)'))
 def test_baseline_upgrade_verified_before_upload_without_legacy(self):
  gate=step('Verify artifact-only upgrade from rc158')
  self.assertIn("github.event.inputs.artifact_only == 'true'",gate);self.assertIn('v1.6.0-rc.158',gate)
  self.assertIn('python3 buildScript/verify_update.py previous-device-candidate/apk-manifest.json dist/apk-manifest.json',gate)
  self.assertNotIn('--allow-legacy',gate);self.assertLess(TEXT.index('- name: Verify artifact-only upgrade'),TEXT.index('- name: Upload APKs (artifact)'))
 def test_pin_alignment_cleanup_and_self_test_retained(self):
  self.assertIn('--certificate signing-certificate.sha256',step('Package verified APKs'))
  self.assertIn('verify_native_alignment.py',step('Verify native 16KB page alignment'))
  self.assertIn('if: always()',step('Remove private signing material'));self.assertIn('rm -rf .signing',step('Remove private signing material'))
  self.assertIn('python3 buildScript/test_artifact_only_workflow.py',step('Check project documentation'))
if __name__=='__main__':unittest.main()
