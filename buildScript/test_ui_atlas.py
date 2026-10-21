#!/usr/bin/env python3
"""Structural atlas checks; deliberately not visual acceptance."""
import json,unittest
from build_ui_atlas import generate,R
class AtlasTests(unittest.TestCase):
 def setUp(self):self.files=generate();self.data=json.loads(self.files['elements.json']);self.graph=json.loads(self.files['transitions.json'])
 def test_all_declared_xml_files_are_included(self):
  expected={p.relative_to(R).as_posix() for folder in ('layout','menu','xml') for p in (R/f'app/src/main/res/{folder}').glob('*.xml') if folder!='xml' or 'preferences' in p.name}
  self.assertEqual(expected,{r['source'] for r in self.data['xml_nodes']})
 def test_nodes_have_unique_stable_addresses(self):
  ids=[r['uid'] for r in self.data['xml_nodes']];self.assertEqual(len(ids),len(set(ids)))
  self.assertTrue(all(r['verification']=='SOURCE_ONLY' for r in self.data['xml_nodes']))
 def test_specified_edges_have_endpoints_and_provenance(self):
  for e in self.graph['edges']:
   self.assertIn(e.get('from',e.get('source')),self.graph['nodes']);self.assertIn(e.get('to',e.get('target')),self.graph['nodes']);self.assertTrue(e['evidence'])
 def test_generated_artifacts_have_no_drift(self):
  for name,text in self.files.items():self.assertEqual((R/'docs/reference/ui'/name).read_text(),text,name)
 def test_construction_sites_reference_real_lines(self):
  for s in self.data['programmatic_sites']:
   self.assertIn(s['code'],(R/s['source']).read_text().splitlines()[s['line']-1])
if __name__=='__main__':unittest.main()
