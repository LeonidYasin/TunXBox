#!/usr/bin/env python3
"""Deterministic structural UI census. Not a reachability or pixel-fit proof."""
import argparse,json,pathlib,re,xml.etree.ElementTree as ET,hashlib
R=pathlib.Path(__file__).resolve().parents[1]; OUT=R/'docs/reference/ui'
def attrs(n):return {k.split('}')[-1]:v for k,v in n.attrib.items()}
def generate():
 strings={}
 for locale in ['values','values-ru']:
  strings[locale]={}
  for p in sorted((R/f'app/src/main/res/{locale}').glob('*.xml')):
   for n in ET.parse(p).getroot():
    if n.tag=='string':strings[locale][n.get('name')]=''.join(n.itertext())
 records=[]; sources=[]; edges=[]
 for folder in ['layout','menu','xml']:
  for p in sorted((R/f'app/src/main/res/{folder}').glob('*.xml')):
   if folder=='xml' and 'preferences' not in p.name:continue
   raw=p.read_text(); rel=p.relative_to(R).as_posix()
   for i,n in enumerate(ET.parse(p).iter()):
    a=attrs(n);tag=n.tag.split('}')[-1]
    label=a.get('text',a.get('title',a.get('hint','')));key=label.removeprefix('@string/')
    risks=[]
    if 'maxLines' in a or a.get('singleLine')=='true' or 'ellipsize' in a:risks.append('TEXT_LIMIT_REQUIRES_RENDER')
    if re.match(r'^\d+(dp|dip)$',a.get('layout_height','')) and ('Text' in tag or 'Button' in tag):risks.append('FIXED_HEIGHT_REQUIRES_FONT_SCALE')
    if tag in ['ImageButton','ImageView'] and not a.get('contentDescription') and a.get('importantForAccessibility')!='no':risks.append('LABEL_REQUIRES_BINDING_REVIEW')
    if label and not label.startswith(('@','?')):risks.append('LITERAL_REQUIRES_LOCALIZATION_REVIEW')
    records.append(dict(uid=f'{rel}#{i}',source=rel,ordinal=i,type=tag,id=a.get('id'),key=a.get('key'),attributes=a,label_en=strings['values'].get(key,label),label_ru=strings['values-ru'].get(key),review_flags=risks,verification='SOURCE_ONLY'))
    for k,v in a.items():
     if k.startswith('nextFocus') or k in ['layout','fragment']:edges.append(dict(source=rel,origin=a.get('id',f'ordinal:{i}'),kind=k,target=v,evidence='DECLARED_XML_NOT_RUNTIME'))
 for p in sorted((R/'app/src/main/java').rglob('*')):
  if p.suffix not in ['.kt','.java']:continue
  if '/ui/' not in str(p) and '/widget/' not in str(p):continue
  for line,text in enumerate(p.read_text().splitlines(),1):
   if re.search(r'\b(?:Button|TextView|EditText|ImageView|ImageButton|CheckBox|RadioButton|Switch|ScrollView|ProgressBar|Spinner)\s*\(|set(?:Positive|Negative|Neutral)Button|setItems\(|TvAction\(|TvQualityCard\(',text):
    sources.append(dict(source=p.relative_to(R).as_posix(),line=line,code=text.strip(),verification='CONSTRUCTION_SITE_NOT_RUNTIME_INSTANCE'))
   if re.search(r'startActivity\(|displayFragment\(|\.show\(.*(?:parentFragmentManager|supportFragmentManager)|setFragmentResult\(',text):
    edges.append(dict(source=p.relative_to(R).as_posix(),line=line,kind='source-transition-site',code=text.strip(),evidence='CANDIDATE_REQUIRES_HANDLER_TRACE'))
 tv=json.loads((R/'docs/reference/tv-navigation.json').read_text())
 graph=dict(version=1,scope='Explicit existing TV edges + traced phone drawer + XML focus/include edges + candidate source sites; not exhaustive runtime graph',nodes=tv['nodes'],edges=[dict(e,evidence='EXISTING_TV_SPEC_REQUIRES_RUNTIME_CHECK') for e in tv['edges']],source_edges=edges)
 main=(R/'app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt').read_text()
 for nav,screen in re.findall(r'R.id.(nav_\w+)\s*->\s*displayFragment\((\w+)\(',main):graph['nodes'][screen]=screen;graph['edges'].append(dict(source='PHONE',target=screen,action=nav,evidence='TRACED_MainActivity_dispatch'))
 graph['nodes'].update(PHONE='Режим телефона',SHORT_QR='Короткий QR без сети',EXTENDED='Расширенная диагностика')
 graph['edges'] += [dict(source='DIAGNOSTICS',target='EXTENDED',action='diagnostic_extended',evidence='TRACED_DiagnosticDialog_MainBrowseFragment'),dict(source='REPORT_PHONE',target='SHORT_QR',action='diagnostic_offline_button',evidence='TRACED_DiagnosticShareDialog_showOffline')]
 data=dict(schema=1,scope='All XML nodes in base layout/menu/*preferences.xml and UI/widget construction sites. Anonymous containers included; variants, dynamic instances, Compose/WebView external DOM and runtime reachability are not inferred.',xml_nodes=records,programmatic_sites=sources)
 files={'elements.json':json.dumps(data,ensure_ascii=False,indent=2)+'\n','transitions.json':json.dumps(graph,ensure_ascii=False,indent=2)+'\n'}
 lines=['# Реестр элементов интерфейса','','**SOURCE_ONLY**: это адресуемый реестр исходников, не число видимых кнопок и не визуальная приёмка. Контейнеры и скрытые/условные ресурсы включены. Каждая строка требует проверки связи с экраном и состояниями. Полные атрибуты и RU/EN — в `elements.json`.','','| Источник / узел | Тип / ID / key | EN → RU | Риск для проверки |','|---|---|---|---|']
 def esc(x):return str(x or '—').replace('|','\\|').replace('\n',' ')
 for e in records:lines.append('| '+ ' | '.join(map(esc,[e['uid'],e['type']+' '+str(e['id'] or e['key'] or ''),e['label_en']+' → '+str(e['label_ru'] or '(fallback / динамический)'),', '.join(e['review_flags']) or 'SOURCE_ONLY']))+' |')
 lines+=['','## Программные элементы','','Это места конструирования, не гарантированные уникальные элементы. Создаваемые в цикле карточки имеют переменную кратность.','']
 for x in sources:lines.append(f"- `{x['source']}:{x['line']}` — `{x['code'].replace('`','')}`")
 files['elements.md']='\n'.join(lines)+'\n'
 dot=['digraph TunXBox {','rankdir=LR;']
 for k,v in graph['nodes'].items():dot.append(f'{json.dumps(k)} [label={json.dumps(v,ensure_ascii=False)}];')
 for e in graph['edges']:
  a=e.get('source',e.get('from'));b=e.get('target',e.get('to'));dot.append(f'{json.dumps(a)} -> {json.dumps(b)} [label={json.dumps(e.get("action",e.get("label","")),ensure_ascii=False)}];')
 files['navigation.dot']='\n'.join(dot+['}'])+'\n'
 files['coverage.json']=json.dumps(dict(xml_files=len(set(x['source'] for x in records)),xml_nodes=len(records),programmatic_construction_sites=len(sources),specified_navigation_edges=len(graph['edges']),candidate_source_and_xml_edges=len(edges),flagged_xml_nodes=sum(bool(x['review_flags']) for x in records),visual_acceptance='NOT_PERFORMED',runtime_completeness='NOT_CLAIMED',source_digest=hashlib.sha256(files['elements.json'].encode()).hexdigest()),indent=2)+'\n'
 return files
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');a=p.parse_args();OUT.mkdir(parents=True,exist_ok=True)
 for name,text in generate().items():
  if a.check:assert (OUT/name).read_text()==text,'stale atlas: '+name
  else:(OUT/name).write_text(text)
 print((OUT/'coverage.json').read_text())
