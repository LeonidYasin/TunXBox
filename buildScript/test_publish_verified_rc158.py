#!/usr/bin/env python3
"""Recovery cannot turn failed/missing source gates into publication permission."""
import copy,importlib.util,json,pathlib,tempfile,unittest
ROOT=pathlib.Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('recover',ROOT/'buildScript/publish_verified_rc158.py');r=importlib.util.module_from_spec(spec);spec.loader.exec_module(r)
NAMES=['Checkout code','Get actual commit SHA','Build debug instrumentation APKs','Test protocol and remote UI','Android 15 emulator smoke (4KB)','Android 16 emulator smoke (16KB)','Prepare pinned signing identity','Test APK packaging and update rules','Build Preview APK','Verify Android update metadata','Verify native 16KB page alignment','Package verified APKs','Upload APKs (artifact)']
class RecoveryTests(unittest.TestCase):
 def setUp(self):
  self.run=dict(head_sha=r.SHA,status='completed',event='workflow_dispatch');self.jobs=[{'steps':[dict(name=n,conclusion='success') for n in NAMES]+[dict(name='Publish rolling pre-release to GitHub Releases',conclusion='failure')]}]
 def test_only_original_publication_failed_is_accepted(self):r.require_gates(self.run,self.jobs)
 def test_wrong_source_incomplete_or_wrong_event_rejected(self):
  for k,v in [('head_sha','0'*40),('status','in_progress'),('event','pull_request')]:
   bad=dict(self.run,**{k:v})
   with self.subTest(k=k),self.assertRaises(ValueError):r.require_gates(bad,self.jobs)
 def test_missing_or_failed_ui_gate_rejected(self):
  for mode in ('missing','failure','skipped'):
   jobs=copy.deepcopy(self.jobs)
   if mode=='missing':jobs[0]['steps']=[s for s in jobs[0]['steps'] if s['name']!='Android 16 emulator smoke (16KB)']
   else:next(s for s in jobs[0]['steps'] if s['name']=='Android 16 emulator smoke (16KB)')['conclusion']=mode
   with self.subTest(mode=mode),self.assertRaises(ValueError):r.require_gates(self.run,jobs)
 def test_extra_failure_or_jobs_rejected(self):
  bad=copy.deepcopy(self.jobs);bad[0]['steps'].append(dict(name='Other failure',conclusion='failure'))
  for jobs in ([],self.jobs*2,bad):
   with self.assertRaises(ValueError):r.require_gates(self.run,jobs)
 def test_wrong_manifest_identity_rejected_before_packaging(self):
  with tempfile.TemporaryDirectory() as d:
   p=pathlib.Path(d);(p/'apk-manifest.json').write_text(json.dumps(dict(commit=r.SHA,installedVersionName='1.6.0-rc.158',versionCode=48000158,applicationId='wrong.package',channel='rc')))
   with self.assertRaises(ValueError):r.validate_files(p)
if __name__=='__main__':unittest.main()
