#!/usr/bin/env python3
"""Require every declared smoke scenario exactly once, with no failed/skipped tests."""
import argparse,pathlib,re,xml.etree.ElementTree as ET
ROOT=pathlib.Path(__file__).resolve().parents[1]
CLASS='io.nekohasekai.sagernet.ui.EmulatorSmokeTest'
def verify(reports,source):
    names=re.findall(r'@Test\s+fun\s+([A-Za-z_]\w*)\s*\(',source.read_text())
    if not names or len(set(names))!=len(names):raise ValueError('Missing/duplicate declared smoke scenarios')
    expected=set(names);actual=[];totals=dict(tests=0,failures=0,errors=0,skipped=0)
    if not reports:raise ValueError('No Android JUnit XML reports')
    for p in reports:
        root=ET.parse(p).getroot()
        if root.tag!='testsuite':raise ValueError('Unexpected Android JUnit root')
        cases=root.findall('testcase')
        if int(root.get('tests','-1'))!=len(cases):raise ValueError('JUnit count does not match test cases')
        for k in totals:totals[k]+=int(root.get(k,'0'))
        for c in cases:
            if c.get('classname')!=CLASS:raise ValueError('Unexpected Android smoke class')
            if any(c.find(k) is not None for k in ('failure','error','skipped')):raise ValueError('Failed/error/skipped smoke scenario: '+str(c.get('name')))
            actual.append(c.get('name'))
    if any(totals[k]!=0 for k in ('failures','errors','skipped')):raise ValueError('Unsuccessful smoke suite: '+str(totals))
    if len(actual)!=len(set(actual)):raise ValueError('Duplicate smoke scenarios or duplicate reports')
    if set(actual)!=expected:raise ValueError(f'Missing scenarios={sorted(expected-set(actual))}; unexpected={sorted(set(actual)-expected)}')
    if totals['tests']!=len(expected):raise ValueError('Incorrect smoke test total')
    return totals
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--reports',type=pathlib.Path,default=ROOT/'app/build/outputs/androidTest-results/connected');p.add_argument('--source',type=pathlib.Path,default=ROOT/'app/src/androidTest/java/io/nekohasekai/sagernet/ui/EmulatorSmokeTest.kt');a=p.parse_args()
    totals=verify(sorted(a.reports.rglob('TEST-*.xml')),a.source);print('Verified all declared Android smoke scenarios:',totals)
