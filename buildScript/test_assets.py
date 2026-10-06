import os,pathlib,subprocess,tempfile,unittest
SCRIPT=pathlib.Path(__file__).parent/'lib/assets.sh'
class AssetsTest(unittest.TestCase):
 def run_script(self,mode,authenticated=False):
  with tempfile.TemporaryDirectory() as d:
   root=pathlib.Path(d);dest=root/'app/src/main/assets/sing-box';dest.mkdir(parents=True);(dest/'geoip.db.xz').write_bytes(b'old-working-asset')
   bin=root/'bin';bin.mkdir()
   curl=bin/'curl';curl.write_text('''#!/bin/bash
if [[ "$*" == *api.github.com* ]]; then
 if [[ "$MODE" == api_fail ]]; then exit 22; fi
 if [[ "$MODE" == invalid_tag ]]; then echo '{"tag_name":""}'; else echo '{"tag_name":"20261006"}'; fi
else
 if [[ "$MODE" == download_fail ]]; then exit 22; fi
 while [[ $# -gt 0 ]]; do if [[ "$1" == --output ]]; then printf 'database-content' > "$2"; exit; fi; shift; done
 exit 2
fi
''');curl.chmod(0o755)
   gh=bin/'gh';gh.write_text('#!/bin/bash\n[[ "$MODE" != api_fail ]] || exit 1\nprintf 20261006\n');gh.chmod(0o755)
   env=dict(os.environ,MODE=mode,PATH=str(bin)+':'+os.environ['PATH']);env.pop('GH_TOKEN',None)
   if authenticated:env['GH_TOKEN']='synthetic-test-token'
   r=subprocess.run(['bash',str(SCRIPT.resolve())],cwd=root,env=env,capture_output=True)
   return r.returncode,(dest/'geoip.db.xz').read_bytes(),(dest/'geosite.db.xz').exists()
 def test_api_failure_preserves_working_assets(self):
  code,old,other=self.run_script('api_fail');self.assertNotEqual(code,0);self.assertEqual(old,b'old-working-asset');self.assertFalse(other)
 def test_empty_tag_is_not_used_in_download(self):
  code,old,other=self.run_script('invalid_tag');self.assertNotEqual(code,0);self.assertEqual(old,b'old-working-asset')
 def test_download_failure_preserves_working_assets(self):
  code,old,other=self.run_script('download_fail');self.assertNotEqual(code,0);self.assertEqual(old,b'old-working-asset')
 def test_authenticated_success_updates_both_assets(self):
  code,old,other=self.run_script('success',True);self.assertEqual(code,0);self.assertNotEqual(old,b'old-working-asset');self.assertTrue(other)
if __name__=='__main__':unittest.main()
