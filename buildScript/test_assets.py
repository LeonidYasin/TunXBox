import hashlib,json,lzma,os,pathlib,shutil,subprocess,tempfile,unittest
ROOT=pathlib.Path(__file__).resolve().parents[1]
class AssetsTest(unittest.TestCase):
 def run_script(self,mode):
  with tempfile.TemporaryDirectory() as d:
   root=pathlib.Path(d);dest=root/'app/src/main/assets/sing-box';dest.mkdir(parents=True);(dest/'geoip.db.xz').write_bytes(b'old-working-asset')
   for f in ['buildScript/lib/assets.sh','buildScript/sources/fetch.py','buildScript/sources/lock.json']:
    target=root/f;target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(ROOT/f,target)
   backup=root/'backup';backup.mkdir();data=json.loads((root/'buildScript/sources/lock.json').read_text())
   for name in data['assets']:
    content=(name+'-database-content').encode();data['assets'][name].update(version='test-1',sha256=hashlib.sha256(content).hexdigest())
    if not(mode=='missing' and name=='geosite'):(backup/(name+'-test-1.db')).write_bytes(b'corrupt' if mode=='corrupt' and name=='geosite' else content)
   (root/'buildScript/sources/lock.json').write_text(json.dumps(data))
   env=dict(os.environ,SOURCE_ARCHIVE_DIR=str(backup),SOURCE_OFFLINE='1',SOURCE_CACHE_DIR=str(root/'cache'))
   result=subprocess.run(['bash',str(root/'buildScript/lib/assets.sh')],cwd=root,env=env,capture_output=True)
   return result.returncode,(dest/'geoip.db.xz').read_bytes(),(dest/'geosite.db.xz').exists()
 def test_missing_offline_asset_preserves_working_assets(self):
  code,old,other=self.run_script('missing');self.assertNotEqual(code,0);self.assertEqual(old,b'old-working-asset');self.assertFalse(other)
 def test_corrupt_asset_preserves_working_assets(self):
  code,old,other=self.run_script('corrupt');self.assertNotEqual(code,0);self.assertEqual(old,b'old-working-asset');self.assertFalse(other)
 def test_verified_offline_assets_update_both(self):
  code,new,other=self.run_script('success');self.assertEqual(code,0);self.assertEqual(lzma.decompress(new),b'geoip-database-content');self.assertTrue(other)
if __name__=='__main__':unittest.main()
