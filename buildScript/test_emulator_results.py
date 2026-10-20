#!/usr/bin/env python3
import importlib.util,pathlib,tempfile,unittest,xml.etree.ElementTree as ET
ROOT=pathlib.Path(__file__).resolve().parents[1]
def load(name):
    s=importlib.util.spec_from_file_location(name,ROOT/'buildScript'/(name+'.py'));m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
v=load('verify_emulator_results');w=load('run_verified_emulator_smoke')
class ValidatorTests(unittest.TestCase):
    def setUp(self):
        self.tmp=tempfile.TemporaryDirectory();self.root=pathlib.Path(self.tmp.name);self.source=self.root/'Smoke.kt';self.source.write_text('\n'.join('@Test fun case%d() {}'%i for i in range(11)))
    def tearDown(self):self.tmp.cleanup()
    def report(self,names=None,**stats):
        names=names if names is not None else ['case%d'%i for i in range(11)];t=ET.Element('testsuite',tests=str(len(names)),failures='0',errors='0',skipped='0');t.attrib.update({k:str(x) for k,x in stats.items()})
        for n in names:ET.SubElement(t,'testcase',classname=v.CLASS,name=n)
        p=self.root/'TEST-smoke.xml';ET.ElementTree(t).write(p);return p
    def test_all_eleven_pass(self):self.assertEqual(11,v.verify([self.report()],self.source)['tests'])
    def test_missing_case_fails(self):
        with self.assertRaises(ValueError):v.verify([self.report(['case%d'%i for i in range(10)])],self.source)
    def test_unknown_case_fails(self):
        with self.assertRaises(ValueError):v.verify([self.report(['wrong%d'%i for i in range(11)])],self.source)
    def test_suite_failure_error_skip_each_fail(self):
        for k in ['failures','errors','skipped']:
            with self.subTest(k=k),self.assertRaises(ValueError):v.verify([self.report(**{k:1})],self.source)
    def test_case_failure_detected_even_if_suite_lies(self):
        p=self.report();t=ET.parse(p);ET.SubElement(t.getroot().find('testcase'),'failure');t.write(p)
        with self.assertRaises(ValueError):v.verify([p],self.source)
    def test_duplicate_reports_fail(self):
        p=self.report()
        with self.assertRaises(ValueError):v.verify([p,p],self.source)
    def test_wrong_class_fails(self):
        p=self.report();t=ET.parse(p);t.getroot().find('testcase').set('classname','Other');t.write(p)
        with self.assertRaises(ValueError):v.verify([p],self.source)
    def test_empty_and_stale_counts_fail(self):
        with self.assertRaises(ValueError):v.verify([],self.source)
        with self.assertRaises(ValueError):v.verify([self.report(tests=10)],self.source)
    def test_harness_preserves_run_and_diagnostics(self):
        old=(ROOT/'buildScript/run_emulator_smoke.sh').read_text();new=w.patched_harness(old)
        self.assertIn('trap collect EXIT',new);self.assertIn('expectedPageSize="$pages"',new);self.assertIn('app:connected${variant}DebugAndroidTest',new);self.assertIn('python3 buildScript/verify_emulator_results.py',new)
        with self.assertRaises(ValueError):w.patched_harness(old.replace("python3 buildScript/verify_emulator_results.py","echo bypass"))
if __name__=='__main__':unittest.main()
