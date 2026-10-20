#!/usr/bin/env python3
"""One-off recovery: publish existing verified rc158 bytes, never rebuild or move tags."""
import hashlib,json,pathlib,subprocess,sys
SHA='0bd5770d861d72f2e0523eb1c519f6a7bac1816f'
RUN=39505780016
TAG='v1.6.0-rc.158'
REPO='LeonidYasin/TunXBox'
EXPECTED={
 'arm64-v8a':'897eca4f6afa8eed83c80dd7801223dfc200523e24cd12e61c2ba04cbe31585a',
 'armeabi-v7a':'dfd7073542046f5e11dce289438654e83ba2262b5c63c20b8314bd258f0b4a40',
 'universal':'3707eb179325771120dface021c151f164e3fa78af5a7a1b63138272ec9c857c'}
def gh(*args):return subprocess.check_output(['gh',*args],text=True)
def api(path):return json.loads(gh('api',f'repos/{REPO}/{path}'))
def require_gates(run,jobs):
    if run['head_sha']!=SHA or run['status']!='completed' or run['event']!='workflow_dispatch':raise ValueError('Wrong source run')
    if len(jobs)!=1:raise ValueError('Unexpected source jobs')
    steps={s['name']:s for s in jobs[0]['steps']}
    required=['Checkout code','Get actual commit SHA','Build debug instrumentation APKs','Test protocol and remote UI','Android 15 emulator smoke (4KB)','Android 16 emulator smoke (16KB)','Prepare pinned signing identity','Test APK packaging and update rules','Build Preview APK','Verify Android update metadata','Verify native 16KB page alignment','Package verified APKs','Upload APKs (artifact)']
    if any(steps.get(n,{}).get('conclusion')!='success' for n in required):raise ValueError('Original gate missing or failed')
    failed={s['name'] for s in steps.values() if s.get('conclusion')=='failure'}
    if failed!={'Publish rolling pre-release to GitHub Releases'}:raise ValueError('Unexpected failed source step')
def validate_files(root):
    m=json.loads((root/'apk-manifest.json').read_text())
    if (m['commit'],m['installedVersionName'],m['versionCode'],m['applicationId'],m['channel'])!=(SHA,'1.6.0-rc.158',48000158,'com.tunxbox.app','rc'):raise ValueError('Wrong manifest')
    names=set()
    for arch,digest in EXPECTED.items():
        name=f'TunXBox-1.6.0-rc.158-android-tv-phone-{arch}-preview-release-vc48000158-g0bd5770d.apk'
        record=next(r for r in m['apks'] if r['name']==name)
        if record['sha256']!=digest or hashlib.sha256((root/name).read_bytes()).hexdigest()!=digest:raise ValueError('APK bytes changed')
        names.add(name)
    if {r['name'] for r in m['apks']}!=names or {p.name for p in root.glob('*.apk')}!=names:raise ValueError('Unexpected APK set')
    subprocess.run(['sha256sum','-c','SHA256SUMS.txt'],cwd=root,check=True)
    return names
if __name__=='__main__':
    require_gates(api(f'actions/runs/{RUN}'),api(f'actions/runs/{RUN}/jobs?per_page=100')['jobs'])
    tag=subprocess.check_output(['git','rev-parse',TAG+'^{commit}'],text=True).strip()
    if tag!=SHA:raise ValueError('Tag does not match tested source; never move it')
    names=validate_files(pathlib.Path('recovery-apks'))
    # Original Android test source and verification helpers must be unchanged.
    for f in ['app/src/androidTest/java/io/nekohasekai/sagernet/ui/EmulatorSmokeTest.kt','buildScript/verify_emulator_results.py','buildScript/package_preview_apks.py','signing-certificate.sha256']:
        if subprocess.check_output(['git','show',SHA+':'+f])!=pathlib.Path(f).read_bytes():raise ValueError('Verification contract changed: '+f)
    import verify_emulator_results as v
    source=pathlib.Path('app/src/androidTest/java/io/nekohasekai/sagernet/ui/EmulatorSmokeTest.kt')
    for pages in ('4096','16384'):
        reports=list((pathlib.Path('recovery-evidence/build/emulator-results')/pages/'results').rglob('TEST*.xml'))
        print(pages,v.verify(reports,source))
    print('Exact tested source, unchanged contracts, 38 passed device tests, original APK bytes verified')
