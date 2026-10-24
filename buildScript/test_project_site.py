#!/usr/bin/env python3
"""Validate generated site without reaching private/provider endpoints."""
import importlib.util,pathlib,tempfile,unittest,urllib.parse,xml.etree.ElementTree as ET
from html.parser import HTMLParser
ROOT=pathlib.Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('site_builder',ROOT/'buildScript/build_project_site.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
class Links(HTMLParser):
 def __init__(self):super().__init__();self.links=[];self.tags=[];self.attrs=[]
 def handle_starttag(self,tag,attrs):
  self.tags.append(tag);self.attrs.append((tag,dict(attrs)))
  for k,v in attrs:
   if k in ('href','src'):self.links.append(v)
class SiteTests(unittest.TestCase):
 @classmethod
 def setUpClass(cls):cls.temp=tempfile.TemporaryDirectory();cls.root=pathlib.Path(cls.temp.name);m.build(cls.root)
 @classmethod
 def tearDownClass(cls):cls.temp.cleanup()
 def test_all_local_links_resolve(self):
  for p in self.root.rglob('*.html'):
   parser=Links();parser.feed(p.read_text())
   for href in parser.links:
    u=urllib.parse.urlsplit(href)
    if u.scheme or u.netloc or not u.path:continue
    dest=(p.parent/urllib.parse.unquote(u.path)).resolve()
    if dest.is_dir():dest=dest/'index.html'
    self.assertTrue(dest.is_relative_to(self.root));self.assertTrue(dest.is_file(),(p,href))
 def test_explicit_contact_and_honest_status(self):
  s=(self.root/'index.html').read_text();self.assertIn('mailto:leonidyasin@gmail.com',s);self.assertIn('https://t.me/LEONI_YA',s);self.assertIn('/issues',s);self.assertIn('причина неизвестна',s);self.assertIn('не действующая оферта',s);self.assertIn('PR5 ещё не merged',s)
 def test_no_tracking_forms_or_remote_scripts(self):
  for p in self.root.rglob('*.html'):
   q=Links();q.feed(p.read_text());self.assertTrue(all(a.get('type')=='application/ld+json' and 'src' not in a for t,a in q.attrs if t=='script'));self.assertNotIn('iframe',q.tags);self.assertNotIn('form',q.tags)
 def test_no_inherited_wallet_or_private_subscription(self):
  s=(self.root/'index.html').read_text();self.assertNotIn('TRhnA7',s);self.assertNotIn('/sub/',s);self.assertNotIn('PRIVATE KEY',s);self.assertNotIn('sekai.icu',s)
 def test_svg_and_mobile_accessibility(self):
  ET.parse(self.root/'project-map.svg');s=(self.root/'index.html').read_text();self.assertIn('name="viewport"',s);self.assertIn('href="#main"',s);self.assertIn('alt="',s);self.assertIn('prefers-reduced-motion',(self.root/'site.css').read_text())
 def test_downloads_point_to_published_stable_and_rolling_test_channel(self):
  for name in ['index.html','en/index.html']:
   s=(self.root/name).read_text();q=Links();q.feed(s)
   releases=[a['href'] for t,a in q.attrs if t=='a' and '/releases/tag/' in a.get('href','')]
   self.assertIn(m.REPO+'/releases/tag/v1.6.0',releases)
   self.assertIn(m.REPO+'/releases/tag/v1.6.0-rc',releases)
   self.assertIn(m.REPO+'/releases/tag/v1.5.0',releases)
   self.assertNotIn(m.REPO+'/releases/tag/v1.6.0-rc.158',releases)
   self.assertIn('rc.162',s)
 def test_seo_survives_regeneration(self):
  import json
  urls=[]
  for p in self.root.rglob('*.html'):
   self.assertNotIn("'+SOURCE_REF+'",p.read_text())
   q=Links();q.feed(p.read_text());canonical=[a['href'] for t,a in q.attrs if t=='link' and a.get('rel')=='canonical']
   self.assertEqual(1,len(canonical));urls.extend(canonical)
   self.assertEqual(1,len([1 for t,a in q.attrs if t=='meta' and a.get('name')=='description']))
  ns={'s':'http://www.sitemaps.org/schemas/sitemap/0.9'}
  sitemap=[n.text for n in ET.parse(self.root/'sitemap.xml').findall('s:url/s:loc',ns)]
  self.assertEqual(set(urls),set(sitemap))
  for name in ['index.html','en/index.html']:
   s=(self.root/name).read_text();q=Links();q.feed(s)
   self.assertEqual({'ru','en','x-default'},{a['hreflang'] for t,a in q.attrs if t=='link' and a.get('rel')=='alternate'})
   import re
   data=json.loads(re.search(r'<script type="application/ld\+json">(.*?)</script>',s).group(1))
   self.assertEqual('1.6.0',data['softwareVersion']);self.assertNotIn('aggregateRating',data)
if __name__=='__main__':unittest.main()
