import hashlib,importlib.util,io,json,os,pathlib,tarfile,tempfile,unittest,zipfile
from unittest.mock import patch
ROOT=pathlib.Path(__file__).resolve().parents[1]
def load(name,path):
 s=importlib.util.spec_from_file_location(name,ROOT/path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
fetch=load('source_fetch','buildScript/sources/fetch.py');pack=load('source_pack','buildScript/sources/package_dashboard.py')
class SourceTest(unittest.TestCase):
 def test_lock_has_immutable_revisions_and_hashes(self):
  for c in fetch.lock()['components'].values():self.assertRegex(c['revision'],r'^[0-9a-f]{40}$');self.assertRegex(c['sha256'],r'^[0-9a-f]{64}$');self.assertIn(c['revision'],c['archive'])
 def test_bundled_font_matches_historical_source_and_notice(self):
  c=fetch.lock()['components']['jetbrains-mono']
  self.assertEqual(c['version'],'1.0.3');self.assertEqual(c['license'],'Apache-2.0')
  self.assertEqual(fetch.digest(ROOT/c['bundledFile']),c['bundledSha256'])
  notice=(ROOT/'app/src/main/assets/third-party/jetbrains-mono-NOTICE.txt').read_text()
  for value in [c['revision'],c['bundledSha256'],'Apache License, Version 2.0','JetBrains s.r.o.','Philipp Nurullin','Konstantin Bulenkov']:self.assertIn(value,notice)
  license=(ROOT/'app/src/main/assets/third-party/jetbrains-mono-LICENSE.txt').read_text()
  self.assertIn('Apache License',license);self.assertIn('Version 2.0, January 2004',license)
 def test_fdroid_compilation_occurs_after_source_scan_without_ignores(self):
  recipe=(ROOT/'fdroid/metadata/com.tunxbox.app.yml').read_text()
  self.assertIn('    build: cd .. && bash buildScript/fdroid/prebuild.sh',recipe)
  self.assertNotIn('    prebuild:',recipe);self.assertNotIn('scanignore:',recipe)
  self.assertIn('    rm:\n      - release.keystore',recipe)
  deletion=recipe.split('    scandelete:',1)[1].split('    gradleprops:',1)[0]
  self.assertIn('app/src/main/assets/yacd.zip',deletion);self.assertNotIn('release.keystore',deletion)
 def test_offline_cache_verifies_hash(self):
  with tempfile.TemporaryDirectory() as d,patch.dict(os.environ,{'SOURCE_CACHE_DIR':d,'SOURCE_OFFLINE':'1'}):
   a=pathlib.Path(d)/'archives';a.mkdir();(a/'x').write_bytes(b'test');self.assertEqual(fetch.obtain('https://invalid',hashlib.sha256(b'test').hexdigest(),'x'),a/'x')
   with self.assertRaises(ValueError):fetch.obtain('https://invalid','0'*64,'x')
 def test_missing_offline_cache_fails(self):
  with tempfile.TemporaryDirectory() as d,patch.dict(os.environ,{'SOURCE_CACHE_DIR':d,'SOURCE_OFFLINE':'1'}):
   with self.assertRaises(FileNotFoundError):fetch.obtain('https://invalid','0'*64,'x')
 def test_portable_archive_restore(self):
  with tempfile.TemporaryDirectory() as d:
   p=pathlib.Path(d);(p/'backup').mkdir();(p/'backup/x').write_bytes(b'test')
   with patch.dict(os.environ,{'SOURCE_CACHE_DIR':str(p/'cache'),'SOURCE_ARCHIVE_DIR':str(p/'backup'),'SOURCE_OFFLINE':'1'}):self.assertEqual(fetch.obtain('https://invalid',hashlib.sha256(b'test').hexdigest(),'x').read_bytes(),b'test')
 def make_tar(self,path,name,link=None):
  with tarfile.open(path,'w:gz') as t:
   n=tarfile.TarInfo(name)
   if link:n.type=tarfile.SYMTYPE;n.linkname=link;t.addfile(n)
   else:n.size=4;t.addfile(n,io.BytesIO(b'test'))
 def test_archive_traversal_is_rejected(self):
  with tempfile.TemporaryDirectory() as d:
   p=pathlib.Path(d);self.make_tar(p/'a.tgz','root/../../evil')
   with self.assertRaises(ValueError):fetch.safe_extract(p/'a.tgz',p/'source')
 def test_archive_escaping_symlink_is_rejected(self):
  with tempfile.TemporaryDirectory() as d:
   p=pathlib.Path(d);self.make_tar(p/'a.tgz','root/link','../../evil')
   with self.assertRaises(ValueError):fetch.safe_extract(p/'a.tgz',p/'source')
 def test_unmanaged_directory_is_preserved(self):
  with tempfile.TemporaryDirectory() as d:
   p=pathlib.Path(d);self.make_tar(p/'a.tgz','root/file');(p/'source').mkdir();(p/'source/keep').write_text('keep')
   with self.assertRaises(ValueError):fetch.safe_extract(p/'a.tgz',p/'source')
   self.assertEqual((p/'source/keep').read_text(),'keep')
 def test_dashboard_zip_is_deterministic_and_compatible(self):
  with tempfile.TemporaryDirectory() as d:
   p=pathlib.Path(d);src=p/'public';src.mkdir();(src/'index.html').write_text('dashboard');(src/'app.js').write_text('console.log(1)')
   pack.package(src,p/'a.zip');pack.package(src,p/'b.zip');self.assertEqual((p/'a.zip').read_bytes(),(p/'b.zip').read_bytes())
   with zipfile.ZipFile(p/'a.zip') as z:self.assertIn('Yacd-meta-source/index.html',z.namelist());self.assertIn('Yacd-meta-source/TUNXBOX-SOURCE.json',z.namelist())
 def test_empty_dashboard_is_not_packaged(self):
  with tempfile.TemporaryDirectory() as d:
   with self.assertRaises(ValueError):pack.package(pathlib.Path(d),pathlib.Path(d)/'a.zip')
if __name__=='__main__':unittest.main()
