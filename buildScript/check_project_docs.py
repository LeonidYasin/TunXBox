#!/usr/bin/env python3
"""Check docs-as-code links, tracked-file inventory and keyed XML preference coverage."""
import argparse, collections, json, pathlib, re, subprocess, urllib.parse, xml.etree.ElementTree as ET
ROOT = pathlib.Path(__file__).resolve().parents[1]
INDEX = ROOT/'docs/reference/repository-index.md'
def paths_from_git():
    return subprocess.check_output(['git','ls-files','-z'],cwd=ROOT).decode().strip('\0').split('\0')
def make_index(paths):
    groups=collections.defaultdict(list)
    for path in sorted(paths):
        if path.startswith('app/src/main/java/'):
            stem='app/src/main/java/'; parts=path[len(stem):].split('/')
            group=stem+'/'.join(parts[:5]) if parts[:3]==['io','nekohasekai','sagernet'] else stem+'/'.join(parts[:4])
        elif path.startswith('app/src/main/res/'): group='/'.join(path.split('/')[:5])
        elif path.startswith('app/src/'): group='/'.join(path.split('/')[:3])
        else: group=path.split('/')[0] if '/' in path else 'Корень'
        groups[group].append(path)
    lines=['# Полный реестр файлов репозитория','','Сгенерирован из tracked Git tree. Это inventory, не утверждение, что каждый ресурс/класс является активной UI-функцией. Generated/ignored AAR, downloaded cores/assets, private signing material и build outputs сюда не входят.','','Исторический tracked `release.keystore` — только имя артефакта; его содержимое не раскрывается и он не является гарантией текущей private pinned signing identity.','','Для обновления: `python3 buildScript/check_project_docs.py --update-index`. Для source archive можно явно передать JSON-массив путей через `--inventory`.','',f'Всего tracked файлов: **{len(paths)}**.','','| Раздел | Файлов |','|---|---|']
    for key,values in sorted(groups.items()): lines.append(f'| `{key}` | {len(values)} |')
    for key,values in sorted(groups.items()):
        lines+=['','## '+key,'']
        for path in values: lines.append(f'- [`{path}`](../../{urllib.parse.quote(path,safe="/._-")})')
    return '\n'.join(lines)+'\n'
def main():
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--inventory',type=pathlib.Path);p.add_argument('--update-index',action='store_true');a=p.parse_args()
    paths=json.loads(a.inventory.read_text()) if a.inventory else paths_from_git();paths=sorted(set(paths+["docs/reference/repository-index.md"]));assert paths and all(isinstance(x,str) for x in paths)
    if a.update_index: INDEX.parent.mkdir(parents=True,exist_ok=True);INDEX.write_text(make_index(paths))
    expected=make_index(paths)
    assert INDEX.read_text()==expected,'File inventory is stale: run --update-index'
    errors=[];checked=0
    for source in [ROOT/'README.md',*(ROOT/'docs').rglob('*.md')]:
        for target in re.findall(r'(?<!!)\[[^\]]*\]\(([^)]+)\)',source.read_text()):
            parsed=urllib.parse.urlsplit(target)
            if parsed.scheme or parsed.netloc or not parsed.path: continue
            resolved=(source.parent/urllib.parse.unquote(parsed.path)).resolve()
            if not resolved.is_relative_to(ROOT) or not resolved.exists(): errors.append(f'{source.relative_to(ROOT)}: {target}')
            checked+=1
    assert not errors,'Broken local links: '+str(errors[:30])
    reference=(ROOT/'docs/reference/preferences.md').read_text();entries=0
    for source in sorted((ROOT/'app/src/main/res/xml').glob('*preferences.xml')):
        keyed=[]
        for node in ET.parse(source).iter():
            attrs={k.split('}')[-1]:v for k,v in node.attrib.items()}
            if attrs.get('key'): keyed.append(attrs['key'])
        if not keyed: continue
        match=re.search(r'^## '+re.escape(source.name)+r'\n(.*?)(?=^## |\Z)',reference,re.S|re.M)
        assert match, 'Missing preferences schema: '+source.name
        actual=re.findall(r'^\| ([^|]+) \|',match.group(1),re.M)[1:]
        assert actual==keyed,'Preference key inventory differs: '+source.name
        entries+=len(keyed)
    print(f'Docs verified: {len(paths)} tracked files, {entries} XML preference entries, {checked} local links')
if __name__=='__main__':main()
