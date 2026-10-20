#!/usr/bin/env python3
"""Create a portable verified snapshot. This is not a claim to archive every transitive source."""
import argparse, hashlib, json, os, pathlib, shutil, subprocess, tarfile, tempfile
from fetch import ROOT, LOCK_PATH, obtain, asset, lock, digest

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--output',type=pathlib.Path,default=ROOT/'source-backups/tunxbox-sources.tar.gz')
    p.add_argument('--pnpm-store',type=pathlib.Path,help='Optional dependency store; includes packages, not proof all binaries are source-built')
    p.add_argument('--go-modules',action='store_true',help='Archive downloaded module sources for core and build tools')
    a=p.parse_args();a.output=a.output.resolve();a.output.parent.mkdir(parents=True,exist_ok=True)
    dirty=subprocess.check_output(['git','status','--porcelain','--untracked-files=normal'],cwd=ROOT,text=True)
    generated={'app/src/main/assets/yacd.zip','app/src/main/assets/yacd.version.txt',
               'fastlane/metadata/android/en-US/images/icon.png','fastlane/metadata/android/ru-RU/images/icon.png'}
    if any(line[3:] not in generated for line in dirty.splitlines() if line.strip()):
        raise RuntimeError('Commit source changes before creating a source backup; only known generated assets may be dirty')
    with tempfile.TemporaryDirectory() as tmp:
        dest=pathlib.Path(tmp)/'tunxbox-source-backup';snap=dest/'archives';snap.mkdir(parents=True)
        manifest={'schema':1,'coverage':'pinned-direct-snapshots','allTransitiveSourcesArchived':False,'files':{},'limitations':lock()['notes']+['Android/Maven source JARs and SDK/NDK toolchains are not included.','Keep a copy outside GitHub. GitHub forks/releases alone do not protect against a GitHub outage.']}
        for name,c in lock()['components'].items():
            name=name+'-'+c['revision']+'.tar.gz';f=obtain(c['archive'],c['sha256'],name);shutil.copyfile(f,snap/name)
        for name,c in lock()['assets'].items():
            shutil.copyfile(asset(name),snap/(name+'-'+c['version']+'.db'))
        shutil.copyfile(LOCK_PATH,dest/'lock.json')
        # git archive includes only tracked source, not local.properties, caches or private signing material.
        app=dest/'TunXBox-git-source.tar'
        raw=dest/'application-raw.tar'
        subprocess.run(['git','archive','--format=tar','--output='+str(raw),'HEAD'],cwd=ROOT,check=True)
        with tarfile.open(raw,'r:') as src,tarfile.open(app,'w:') as dst:
            for m in src:
                if pathlib.Path(m.name).suffix in ['.keystore','.jks']:continue
                dst.addfile(m,src.extractfile(m) if m.isfile() else None)
        raw.unlink()
        manifest['applicationCommit']=subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip()
        if a.go_modules:
            env=dict(os.environ,GOTOOLCHAIN='local')
            for name,cwd in [('core',ROOT/'libcore'),('gomobile',ROOT/'.source-cache/src'/('gomobile-'+lock()['components']['gomobile']['revision']))]:
                proc=subprocess.run(['go','mod','download','-json'],cwd=cwd,env=env,text=True,capture_output=True,check=True)
                (dest/(name+'-go-modules.json')).write_text(proc.stdout)
                decoder=json.JSONDecoder();text=proc.stdout;modules=[]
                while text.strip():
                    obj,n=decoder.raw_decode(text.lstrip());text=text.lstrip()[n:];modules.append(obj)
                for m in modules:
                    for key in ['Zip','GoMod']:
                        if m.get(key):
                            f=pathlib.Path(m[key]);out=dest/'go-modules'/m['Path']/(m['Version']+('.zip' if key=='Zip' else '.mod'));out.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(f,out)
            manifest['goModuleSourcesIncluded']=True
        if a.pnpm_store:
            if not a.pnpm_store.is_dir():raise ValueError('pnpm store does not exist')
            shutil.copytree(a.pnpm_store,dest/'pnpm-store',symlinks=False)
            manifest['pnpmStoreIncluded']=True
        (dest/'RESTORE.txt').write_text('Verify the companion .sha256 file before unpacking. Verify each file against MANIFEST.json.\nSet SOURCE_ARCHIVE_DIR to this backup archives/ directory and SOURCE_OFFLINE=1.\nSource snapshots are restored by buildScript/sources/fetch.py using lock.json hashes.\nOptional Go/npm caches need tool-specific restoration; all-transitive completeness is NOT claimed.\nPrivate signing keys are deliberately excluded. Preserve signing backups separately and privately.\n')
        for f in sorted(dest.rglob('*')):
            if f.is_file():manifest['files'][f.relative_to(dest).as_posix()]=digest(f)
        (dest/'MANIFEST.json').write_text(json.dumps(manifest,indent=2)+'\n')
        with tarfile.open(a.output,'w:gz') as tar:tar.add(dest,arcname=dest.name)
    a.output.with_suffix(a.output.suffix+'.sha256').write_text(digest(a.output)+'  '+a.output.name+'\n')
    print(a.output)
if __name__=='__main__':main()
