#!/usr/bin/env python3
"""Stable ZIP layout compatible with libcore's Yacd-* extractor."""
import json, os, pathlib, sys, tempfile, zipfile
ROOT=pathlib.Path(__file__).resolve().parents[2]
def package(source, target=None):
    source=pathlib.Path(source)
    if not (source/'index.html').is_file(): raise ValueError('Dashboard build has no index.html')
    files=sorted(p for p in source.rglob('*') if p.is_file())
    if not any(p.suffix=='.js' for p in files): raise ValueError('Dashboard build has no JavaScript')
    target=pathlib.Path(target or ROOT/'app/src/main/assets/yacd.zip');target.parent.mkdir(parents=True,exist_ok=True)
    fd,tmp=tempfile.mkstemp(dir=target.parent,suffix='.zip');os.close(fd)
    try:
        with zipfile.ZipFile(tmp,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as z:
            for p in files:
                info=zipfile.ZipInfo('Yacd-meta-source/'+p.relative_to(source).as_posix(),(2023,6,14,0,0,0))
                info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=0o100644<<16
                z.writestr(info,p.read_bytes())
            c=json.loads((ROOT/'buildScript/sources/lock.json').read_text())['components']['yacd']
            info=zipfile.ZipInfo('Yacd-meta-source/TUNXBOX-SOURCE.json',(2023,6,14,0,0,0));info.external_attr=0o100644<<16
            z.writestr(info,json.dumps(c,sort_keys=True,indent=2)+'\n')
        os.replace(tmp,target)
    finally:
        if os.path.exists(tmp):os.unlink(tmp)
    if target==ROOT/'app/src/main/assets/yacd.zip':
        (target.parent/'yacd.version.txt').write_text(c['revision'])
    return target
if __name__=='__main__':print(package(sys.argv[1]))
