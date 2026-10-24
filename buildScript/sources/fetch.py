#!/usr/bin/env python3
"""Verified source/data snapshots. SOURCE_ARCHIVE_DIR permits an offline restore."""
import argparse, hashlib, json, os, pathlib, shutil, stat, tarfile, tempfile, time, urllib.request
ROOT = pathlib.Path(__file__).resolve().parents[2]
LOCK_PATH = ROOT / 'buildScript/sources/lock.json'
def lock():
    data = json.loads(LOCK_PATH.read_text())
    if data['schema'] != 1: raise ValueError('Unsupported source lock schema')
    return data
def digest(path):
    h = hashlib.sha256()
    with open(path, 'rb') as f:
        for b in iter(lambda: f.read(1024 * 1024), b''): h.update(b)
    return h.hexdigest()
def obtain(url, expected, name):
    if len(expected) != 64 or any(c not in '0123456789abcdef' for c in expected):
        raise ValueError('Invalid SHA256 for ' + name)
    cache = pathlib.Path(os.environ.get('SOURCE_CACHE_DIR', ROOT / '.source-cache')) / 'archives'
    cache.mkdir(parents=True, exist_ok=True)
    dest = cache / name
    if dest.exists():
        if digest(dest) == expected: return dest
        raise ValueError('Corrupt cached file: ' + str(dest))
    archive = os.environ.get('SOURCE_ARCHIVE_DIR')
    local = pathlib.Path(archive) / name if archive else None
    if local and not local.is_file(): raise FileNotFoundError('Offline archive is missing ' + str(local))
    if not local and os.environ.get('SOURCE_OFFLINE') == '1':
        raise FileNotFoundError('Offline cache is missing ' + name)
    fd, tmp = tempfile.mkstemp(prefix=name + '.', dir=cache); os.close(fd)
    try:
        if local: shutil.copyfile(local, tmp)
        else:
            mirror = os.environ.get('SOURCE_MIRROR_BASE_URL')
            if mirror:
                if not mirror.startswith('https://'): raise ValueError('Source mirror must use HTTPS')
                url = mirror.rstrip('/') + '/' + name
            req = urllib.request.Request(url, headers={'User-Agent': 'TunXBox-source-preservation'})
            for attempt in range(3):
                try:
                    with urllib.request.urlopen(req, timeout=120) as src, open(tmp, 'wb') as dst:
                        shutil.copyfileobj(src, dst)
                    break
                except OSError:
                    if attempt == 2: raise
                    time.sleep(2 ** attempt)
        if digest(tmp) != expected: raise ValueError('SHA256 mismatch for ' + name)
        os.replace(tmp, dest)
    finally:
        if os.path.exists(tmp): os.unlink(tmp)
    return dest
def safe_extract(archive, destination):
    """Extract regular source files/directories and in-tree links; reject devices/traversal."""
    destination = pathlib.Path(destination).resolve()
    with tarfile.open(archive, 'r:gz') as tar:
        members = tar.getmembers()
        roots = {pathlib.PurePosixPath(m.name).parts[0] for m in members if m.name}
        if len(roots) != 1: raise ValueError('Expected one archive root')
        prefix = next(iter(roots))
        with tempfile.TemporaryDirectory(dir=destination.parent) as temp:
            base = pathlib.Path(temp).resolve()
            for m in members:
                path = pathlib.PurePosixPath(m.name)
                if path.is_absolute() or '..' in path.parts: raise ValueError('Unsafe archive path')
                if not (m.isfile() or m.isdir() or m.issym() or m.islnk()):
                    raise ValueError('Unsupported archive entry ' + m.name)
                if m.issym() or m.islnk():
                    target = ((base / m.name).parent / m.linkname) if m.issym() else (base / m.linkname)
                    if not target.resolve().is_relative_to(base / prefix): raise ValueError('Unsafe archive link')
            # data filter is available on Python 3.12+; above checks cover older build hosts.
            if hasattr(tarfile, 'data_filter'): tar.extractall(base, filter='data')
            else: tar.extractall(base, members=members)
            source = base / prefix
            if destination.exists():
                marker = destination / '.tunxbox-source.json'
                if not marker.is_file(): raise ValueError('Refusing to replace unmanaged directory ' + str(destination))
                shutil.rmtree(destination)
            shutil.move(source, destination)
def component(name, destination=None):
    c = lock()['components'][name]
    archive = obtain(c['archive'], c['sha256'], name + '-' + c['revision'] + '.tar.gz')
    dest = pathlib.Path(destination) if destination else ROOT / '.source-cache/src' / (name + '-' + c['revision'])
    dest.parent.mkdir(parents=True, exist_ok=True)
    safe_extract(archive, dest)
    (dest / '.tunxbox-source.json').write_text(json.dumps(c, indent=2) + '\n')
    return dest.resolve()
def asset(name):
    c = lock()['assets'][name]
    return obtain(c['url'], c['sha256'], name + '-' + c['version'] + '.db')
def main():
    p = argparse.ArgumentParser(description=__doc__);sub = p.add_subparsers(dest='command', required=True)
    c=sub.add_parser('component');c.add_argument('name', choices=lock()['components']);c.add_argument('--dest')
    a=sub.add_parser('asset');a.add_argument('name', choices=lock()['assets'])
    sub.add_parser('prefetch')
    args=p.parse_args()
    if args.command=='component': print(component(args.name,args.dest))
    elif args.command=='asset': print(asset(args.name))
    else:
        for name,c in lock()['components'].items():
            path=obtain(c['archive'],c['sha256'],name+'-'+c['revision']+'.tar.gz');print(path)
        for name in lock()['assets']: print(asset(name))
if __name__=='__main__': main()
