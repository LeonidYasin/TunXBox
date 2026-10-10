#!/usr/bin/env python3
"""Dependency-free static Pages site from public README/docs; no network or telemetry."""
import argparse, html, json, pathlib, re, posixpath, urllib.parse
ROOT=pathlib.Path(__file__).resolve().parents[1]
REPO='https://github.com/LeonidYasin/TunXBox'
EMAIL='leonidyasin@gmail.com'
CSS='''*{box-sizing:border-box}html{scroll-behavior:smooth}body{margin:0;background:#081620;color:#eaf3f7;font:17px/1.65 Arial,sans-serif}a{color:#70e4dc;text-underline-offset:4px}a:hover{color:white}a:focus-visible,button:focus-visible{outline:3px solid #ffbd75;outline-offset:5px}.skip{position:absolute;left:12px;top:-80px;padding:10px;background:#123847}.skip:focus{top:12px;z-index:10}header,main,footer{max-width:1160px;margin:auto;padding:24px}header{display:flex;justify-content:space-between;align-items:center;gap:20px;border-bottom:1px solid #28424f}.brand{font-size:24px;font-weight:bold;text-decoration:none;color:white;letter-spacing:-1px}nav{display:flex;flex-wrap:wrap;gap:20px}nav a{font-size:15px;text-decoration:none}.hero{padding:65px 0 45px;display:grid;grid-template-columns:1.4fr 1fr;gap:50px;align-items:center}.eyebrow{color:#70e4dc;font-size:13px;font-weight:bold;letter-spacing:2px;text-transform:uppercase}h1{font-size:clamp(36px,5vw,65px);line-height:1.08;letter-spacing:-2px;margin:20px 0}h2{font-size:32px;line-height:1.2;margin:42px 0 20px}h3{font-size:21px;line-height:1.3}p{color:#b9cdd7}.lead{font-size:20px;max-width:670px}.buttons{display:flex;flex-wrap:wrap;gap:12px;margin:28px 0}.button{display:inline-block;padding:13px 20px;border-radius:9px;background:#73e2da;color:#082326;text-decoration:none;font-weight:bold}.button:hover{background:#b7fff7;color:#082326}.button.secondary{background:transparent;color:#cde8ed;border:1px solid #416270}.chips{display:flex;flex-wrap:wrap;gap:8px}.chip{font-size:13px;color:#a9c2ce;background:#122b38;padding:5px 11px;border-radius:20px}.panel{border:1px solid #335463;border-radius:20px;padding:26px;background:linear-gradient(145deg,#193643,#112430);box-shadow:0 25px 80px #0005}.panel .display{background:#071923;padding:24px;border-radius:12px}.panel strong{font-size:24px}.signal{display:inline-block;width:10px;height:10px;border-radius:50%;background:#ffbd75;margin-right:10px}.small{font-size:14px;color:#a4bcc7}.grid{display:grid;grid-template-columns:repeat(3,1fr);gap:18px}.card{padding:24px;background:#112733;border:1px solid #294552;border-radius:12px}.card h3{margin-top:0}.card p{font-size:16px;margin-bottom:0}.accent{border-left:4px solid #ffbd75;padding:16px 22px;background:#172b35}.status{width:100%;border-collapse:collapse}td,th{border-bottom:1px solid #294552;padding:14px;text-align:left;vertical-align:top}th{color:#eaf3f7}td{color:#b9cdd7}.table-wrap{overflow-x:auto}.map{background:#102633;border:1px solid #294552;border-radius:14px;padding:12px}.map img{display:block;width:100%;height:auto}.doc{max-width:900px;margin:40px auto;overflow-wrap:anywhere}.doc h1{font-size:40px;letter-spacing:-1px}.doc h2{font-size:28px}.doc li{color:#b9cdd7}.doc pre{background:#102a37;padding:20px;border-radius:9px;overflow:auto;font:14px/1.5 monospace;white-space:pre}.doc code{color:#a1efe5}.doc p code{background:#173440;padding:2px 5px;border-radius:4px}.doc blockquote{border-left:3px solid #70e4dc;padding-left:20px}.doc td{font-size:14px}.doc-links{columns:2;list-style:none;padding:0}.doc-links li{padding:9px 0;break-inside:avoid}footer{border-top:1px solid #28424f;margin-top:50px;color:#a4bcc7;font-size:14px}section{scroll-margin-top:25px}svg{max-width:100%}@media(max-width:800px){.hero{grid-template-columns:1fr;padding-top:36px;gap:20px}.grid{grid-template-columns:repeat(2,1fr)}header{align-items:flex-start;flex-direction:column}h1{letter-spacing:-1px}.doc-links{columns:1}}@media(max-width:500px){main,header,footer{padding:18px}.grid{grid-template-columns:1fr}.hero{padding-top:24px}.panel{padding:18px}td,th{padding:10px}.doc h1{font-size:31px}nav{gap:14px}}@media(prefers-reduced-motion:reduce){html{scroll-behavior:auto}}'''
def esc(s):return html.escape(str(s),quote=True)
def slug(s):return re.sub(r'[^\w\- ]','',s.lower(),flags=re.UNICODE).replace(' ','-')
def template(title,body,depth=0):
 prefix='../'*depth
 return f'''<!doctype html><html lang="ru"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="description" content="TunXBox: открытый Android TV и Phone клиент, документация, roadmap и участие в проекте."><meta name="referrer" content="strict-origin-when-cross-origin"><meta name="color-scheme" content="dark"><title>{esc(title)} · TunXBox</title><link rel="stylesheet" href="{prefix}site.css"></head><body><a class="skip" href="#main">Перейти к содержимому</a><header><a class="brand" href="{prefix}index.html">TunXBox<span style="color:#70e4dc"> / </span></a><nav aria-label="Основная навигация"><a href="{prefix}index.html#downloads">Скачать</a><a href="{prefix}index.html#docs">Документация</a><a href="{prefix}index.html#support">Поддержать</a><a href="{REPO}">GitHub ↗</a></nav></header><main id="main">{body}</main><footer>Независимый форк NekoBox/SagerNet · GNU GPL v3 или позже · <a href="{REPO}/blob/feature/integration-1.6-review/LICENSE">Авторство и лицензия</a><br>Публичный контакт: <a href="mailto:{EMAIL}">{EMAIL}</a>. Сайт без аналитики, форм сбора данных и автоматических платежей. Telegram: <a href="https://t.me/LEONI_YA">@LEONI_YA</a>.</footer></body></html>'''
def mindmap():
 text=(ROOT/'docs/project-map.md').read_text();m=re.search(r'\nmindmap\n(.*?)\n```',text,re.S);assert m
 cats=[]
 for line in m[1].splitlines():
  n=len(line)-len(line.lstrip());label=line.strip()
  if n==4:cats.append([label,[]])
  elif n>4 and cats:cats[-1][1].append(label)
 parts=['<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1080 840" role="img" aria-labelledby="title desc"><title id="title">Майндмэп TunXBox</title><desc id="desc">Направления проекта и текущие планы; полная текстовая карта доступна в документации.</desc><rect width="1080" height="840" fill="#102633"/>']
 for i,(label,children) in enumerate(cats):
  left=i<5;idx=i if left else i-5;x=20 if left else 700;y=20+idx*158 if left else 70+idx*174;end=380 if left else 700
  parts.append(f'<path d="M540 410 L{end} {y+65}" stroke="#335867" stroke-width="7" fill="none"/>')
  parts.append(f'<rect x="{x}" y="{y}" width="360" height="138" rx="12" fill="#173440" stroke="#42626e"/>')
  parts.append(f'<text x="{x+14}" y="{y+27}" fill="#eaf3f7" font-family="Arial" font-size="17" font-weight="bold">{esc(label)}</text>')
  for j,c in enumerate(children):parts.append(f'<text x="{x+14}" y="{y+49+j*15}" fill="#b6d5df" font-family="Arial" font-size="13">{esc(c)}</text>')
 parts+=['<circle cx="540" cy="410" r="67" fill="#73e2da"/><text x="540" y="418" text-anchor="middle" fill="#082326" font-family="Arial" font-weight="bold" font-size="25">TunXBox</text></svg>'];return ''.join(parts)
def build(out):
 out.mkdir(parents=True,exist_ok=True);(out/'.nojekyll').write_text('');(out/'site.css').write_text(CSS);(out/'project-map.svg').write_text(mindmap())
 sources=[ROOT/'README.md',*sorted((ROOT/'docs').rglob('*.md'))]
 # Large source inventory stays linked to GitHub, not replicated into the landing payload.
 sources=[p for p in sources if p.relative_to(ROOT).as_posix()!='docs/reference/repository-index.md']
 targets={p.relative_to(ROOT).as_posix():('guide/readme.html' if p==ROOT/'README.md' else 'guide/'+p.relative_to(ROOT/'docs').with_suffix('.html').as_posix()) for p in sources}
 def href(url,source,target):
  d=urllib.parse.urlsplit(html.unescape(url))
  if d.scheme:
   return url if d.scheme in ['http','https','mailto'] else '#'
  if d.netloc:return '#'
  if not d.path:return url
  path=posixpath.normpath(posixpath.join(posixpath.dirname(source),urllib.parse.unquote(d.path)))
  mapped=targets.get(path)
  if mapped:return posixpath.relpath(mapped,posixpath.dirname(target))+(('#'+d.fragment) if d.fragment else '')
  return REPO+'/blob/feature/integration-1.6-review/'+urllib.parse.quote(path,safe='/')+(('#'+d.fragment) if d.fragment else '')
 def inline(text,source,target):
  pieces=[];pos=0
  for m in re.finditer(r'\[([^\]]+)\]\(([^)]+)\)',text):
   pieces.append(esc(text[pos:m.start()]));pieces.append('<a href="'+esc(href(m[2],source,target))+'">'+esc(m[1])+'</a>');pos=m.end()
  pieces.append(esc(text[pos:]));s=''.join(pieces)
  s=re.sub(r'`([^`]+)`',r'<code>\1</code>',s);s=re.sub(r'\*\*([^*]+)\*\*',r'<strong>\1</strong>',s);return s
 def markdown(text,source,target):
  lines=text.splitlines();body=[];i=0
  while i<len(lines):
   s=lines[i]
   if not s.strip():i+=1;continue
   if s.startswith('```'):
    lang=s[3:].strip();i+=1;code=[]
    while i<len(lines) and not lines[i].startswith('```'):code.append(lines[i]);i+=1
    if lang=='mermaid' and source=='docs/project-map.md' and any(x.strip()=='mindmap' for x in code):body.append('<div class="map"><a href="../project-map.svg"><img src="../project-map.svg" alt="Направления проекта TunXBox; далее приведён полный текст карты"></a></div>')
    else:body.append('<pre><code>'+esc('\n'.join(code))+'</code></pre>')
    i+=1;continue
   h=re.match(r'^(#{1,6})\s+(.+)$',s)
   if h:body.append(f'<h{len(h[1])} id="{esc(slug(h[2]))}">{inline(h[2],source,target)}</h{len(h[1])}>');i+=1;continue
   if s.lstrip().startswith('|'):
    rows=[]
    while i<len(lines) and lines[i].lstrip().startswith('|'):
     cells=lines[i].strip().strip('|').split('|')
     if not all(re.fullmatch(r'\s*:?-+:?\s*',c) for c in cells):rows.append(cells)
     i+=1
    body.append('<div class="table-wrap"><table>')
    for j,row in enumerate(rows):tag='th' if j==0 else 'td';body.append('<tr>'+''.join('<'+tag+'>'+inline(c.strip(),source,target)+'</'+tag+'>' for c in row)+'</tr>')
    body.append('</table></div>');continue
   if re.match(r'^\s*(?:[-*]|\d+\.)\s+',s):
    body.append('<ul>')
    while i<len(lines) and re.match(r'^\s*(?:[-*]|\d+\.)\s+',lines[i]):body.append('<li>'+inline(re.sub(r'^\s*(?:[-*]|\d+\.)\s+','',lines[i]),source,target)+'</li>');i+=1
    body.append('</ul>');continue
   para=[s];i+=1
   while i<len(lines) and lines[i].strip() and not re.match(r'^(?:#|```|\s*\||\s*[-*]\s|\s*\d+\.\s)',lines[i]):para.append(lines[i]);i+=1
   body.append('<p>'+inline(' '.join(para),source,target)+'</p>')
  return ''.join(body)
 docs=[]
 for p in sources:
  source=p.relative_to(ROOT).as_posix();target=targets[source];text=p.read_text();title=text.splitlines()[0].lstrip('# ').strip();dest=out/target;dest.parent.mkdir(parents=True,exist_ok=True);depth=len(pathlib.PurePosixPath(target).parts)-1
  dest.write_text(template(title,'<article class="doc"><p class="small"><a href="'+esc(REPO+'/blob/feature/integration-1.6-review/'+source)+'">Исходный Markdown на GitHub ↗</a></p>'+markdown(text,source,target)+'</article>',depth));docs.append((title,target))
 contributions=[('Тестировать','Телефоны, приставки, пульт, 4КБ/16КБ, разные сети и реальные сценарии.'),('Сообщать о багах','Точные шаги и безопасная сводка. Без ключей, подписок и персональных endpoint.'),('Писать код','Kotlin/Android, Go, протоколы, тесты, CI, надёжность и безопасность.'),('Переводить и объяснять','Локализация, инструкции, понятные ошибки и помощь новым пользователям.'),('Улучшать интерфейс','TV-layout, D-pad фокус, контраст, доступность и адаптация экранов.'),('Рассказывать о проекте','Обзоры, демонстрации, проверенные ссылки и честная обратная связь.'),('Помочь ресурсами','Тестовые устройства, воспроизводимые fixtures и согласованная CI-инфраструктура.'),('Поддержать деньгами','Разовая помощь, регулярное спонсорство или бюджет задачи — обсудить по почте.'),('Заказать сопровождение','Диагностика и согласованное исправление бага. Условия и гарантия — только отдельным договором.')]
 body='''<section class="hero"><div><div class="eyebrow">Открытый Android-клиент · TV + Phone</div><h1>Ваши серверы.<br>Ваш контроль.</h1><p class="lead">TunXBox — VPN-клиент для телевизора и телефона. Управление пультом, профили и группы, локальный обмен конфигурациями и понятная диагностика.</p><div class="buttons"><a class="button" href="#downloads">Скачать приложение</a><a class="button secondary" href="#support">Помочь проекту</a></div><div class="chips"><span class="chip">Независимый open-source форк</span><span class="chip">sing-box</span><span class="chip">GPL v3+</span></div></div><div class="panel"><div class="display"><span class="small">ДВА ИНТЕРФЕЙСА · ОДИН APK</span><h3>Телевизор / Телефон</h3><p>Пульт и касание<br>Профили и группы<br>QR и локальный обмен</p><p class="small"><span class="signal"></span>Живость канала важнее надписи «подключено»</p></div><p class="small">Это схема возможностей, не скриншот и не обещание работоспособности каждого протокола.</p></div></section>
<section id="downloads"><div class="eyebrow">Проверенные публикации, не просто ветка</div><h2>Выберите канал</h2><div class="grid"><div class="card"><h3>Стабильный 1.5.0</h3><p>Опубликованный неизменяемый baseline. Новые изменения не добавляются в него задним числом.</p><div class="buttons"><a class="button" href="'''+REPO+'''/releases/tag/v1.5.0">Открыть stable</a></div></div><div class="card"><h3>Тестовый 1.6.0-rc</h3><p>Новый APK появляется только после CI и публикации. Проверяйте настоящую версию, commit, ABI и checksum в manifest.</p><div class="buttons"><a class="button secondary" href="'''+REPO+'''/releases/tag/v1.6.0-rc">Открыть RC-канал</a></div></div><div class="card"><h3>Что проверять</h3><p>Установка поверх старой версии, сохранность групп, TCP REALITY, безопасная диагностика и стабильность Hysteria2.</p><div class="buttons"><a class="button secondary" href="guide/hysteria-health.html">План проверки</a></div></div></div><p class="small">TunXBox — клиент, не поставщик VPN-подписок. Прямая загрузка и установка обновлений из приложения пока в roadmap.</p></section>
<section id="status"><h2>Честный статус проекта</h2><div class="table-wrap"><table class="status"><tr><th>Направление</th><th>Статус</th></tr><tr><td>TV / Phone / QR</td><td>Базовые функции опубликованы. Физическая приёмка каждого устройства важна.</td></tr><tr><td>LAN / gateway:10808</td><td>PR4 merged в master; не часть immutable stable 1.5.0.</td></tr><tr><td>TCP REALITY и диагностика</td><td>Проверочная интеграция. PR5 ещё не merged; новый APK требует CI и приёмки.</td></tr><tr><td>Hysteria2</td><td>Нестабильность соединения и скорости — открытое расследование, причина неизвестна.</td></tr><tr><td>XHTTP / авто-gateway / updater</td><td>Отдельные направления; готовность не заявляется.</td></tr></table></div></section>
<section id="map"><h2>Карта развития</h2><p>От пульта и профилей до качества ядра, диагностики и участия сообщества.</p><div class="map"><a href="project-map.svg"><img src="project-map.svg" alt="Майндмэп TunXBox: интерфейсы, профили, диагностика, данные, сборка, безопасность и планы"></a></div><p><a href="guide/project-map.html">Полная текстовая карта →</a> · <a href="guide/roadmap.html">Roadmap и критерии готовности →</a></p></section>
<section id="support"><div class="eyebrow">Любой полезный вклад имеет значение</div><h2>Давайте сделаем TunXBox надёжнее</h2><p class="lead">Поддержка — это не только деньги. Помогите проверить реальные устройства, объяснить проблему, написать тест или сделать интерфейс удобнее.</p><div class="grid">'''+''.join('<div class="card"><h3>'+esc(t)+'</h3><p>'+esc(d)+'</p></div>' for t,d in contributions)+'''</div><div class="buttons"><a class="button" href="mailto:'''+EMAIL+'''?subject=TunXBox%20support">Написать владельцу</a><a class="button secondary" href="https://t.me/LEONI_YA">Telegram @LEONI_YA</a><a class="button secondary" href="'''+REPO+'''/issues">Сообщить об ошибке</a><a class="button secondary" href="guide/contributing-and-support.html">Как участвовать</a><a class="button secondary" href="'''+REPO+'''/pulls">Pull requests</a></div><div class="accent"><strong>Никаких выдуманных реквизитов или обещаний.</strong><p>Telegram владельца: @LEONI_YA. Платёжные реквизиты ещё не заданы. Автосписаний и платежей в приложении нет. Платные услуги — проект предложения, не действующая оферта. Бесплатные баг-репорты доступны через GitHub Issues, почту и Telegram. Issues включены; Discussions отдельно не заявляются. Не отправляйте секреты или сырые логи.</p></div></section>
<section id="docs"><h2>Документация целиком</h2><ul class="doc-links">'''+''.join('<li><a href="'+esc(t)+'">'+esc(title)+'</a></li>' for title,t in docs)+'''<li><a href="'''+REPO+'''/blob/feature/integration-1.6-review/docs/reference/repository-index.md">Полный реестр исходников ↗</a></li></ul></section>
<section id="license"><h2>Свободный код. Добровольная поддержка.</h2><p>GPL v3 или позже, с сохранением исходного авторства NekoBox/SagerNet. Оплата сопровождения не ограничивает права GPL и не покупает гарантию отсутствия всех багов. Гарантия на согласованную выполненную работу обсуждается отдельно.</p><a href="guide/support-services-proposal.html">Проект условий сопровождения →</a></section>'''
 (out/'index.html').write_text(template('VPN-клиент для TV и Phone',body))
 (out/'site-manifest.json').write_text(json.dumps({'schema':1,'source':'README.md + docs/','contact':EMAIL,'pages':[targets[p.relative_to(ROOT).as_posix()] for p in sources]},ensure_ascii=False,indent=2)+'\n')
 print(f'Static site: {len(sources)+1} HTML pages, source docs preserved, no external runtime dependencies')
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',type=pathlib.Path,default=ROOT/'website');args=p.parse_args();build(args.output)
