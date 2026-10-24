"""Deterministic SEO post-processing; no network, tracking or ownership claims."""
import html,json,pathlib,re,xml.etree.ElementTree as ET
BASE='https://leonidyasin.github.io/TunXBox/'
REPO='https://github.com/LeonidYasin/TunXBox'
# Change only after the immutable stable release has actually been published.
STABLE='1.6.0'
HERE=pathlib.Path(__file__).resolve().parent

def apply(out):
    out=pathlib.Path(out)
    (out/'en').mkdir(exist_ok=True)
    (out/'en/index.html').write_text((HERE/'site/english.html').read_text())
    (out/'favicon.svg').write_text('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108"><rect width="108" height="108" rx="20" fill="#0F172A"/><path fill="#67E8F9" d="M34 30H74V38H58V54L72 68L66 74L54 62L42 74L36 68L50 54V38H34Z"/></svg>\n')
    urls=[]
    for p in sorted(out.rglob('*.html')):
        rel=p.relative_to(out).as_posix()
        url=BASE+('' if rel=='index.html' else 'en/' if rel=='en/index.html' else rel)
        s=p.read_text()
        # Idempotent: replace only the SEO-owned tags, never body/scripts/styles.
        s=re.sub(r'<link\b[^>]*(?:rel="(?:canonical|alternate|icon)")[^>]*>','',s)
        s=re.sub(r'<meta\b[^>]*(?:name="(?:description|robots|twitter:[^"]+)"|property="og:[^"]+")[^>]*>','',s)
        s=re.sub(r'<script type="application/ld\+json">.*?</script>','',s,flags=re.S)
        title=html.unescape(re.search(r'<title>(.*?)</title>',s,re.S).group(1))
        if rel=='index.html':
            title='TunXBox — VPN и прокси-клиент для Android TV и телефона'
            s=re.sub(r'<title>.*?</title>','<title>'+title+'</title>',s,flags=re.S)
            desc='TunXBox — открытый VPN и прокси-клиент на sing-box для Android TV и телефона. Управление пультом, профили, группы и локальный обмен через QR. Скачать APK.'
            if 'href="en/"' not in s:s=s.replace('<nav aria-label="Основная навигация">','<nav aria-label="Основная навигация"><a href="en/" lang="en">English</a>')
        elif rel=='en/index.html':desc='TunXBox is an open-source sing-box proxy client for Android TV and phones. Remote-friendly controls, profiles, groups and local QR configuration transfer.'
        else:desc=title+' — документация TunXBox для Android TV и телефона.'
        esc=lambda x:html.escape(x,quote=True)
        prefix='../'*(len(p.relative_to(out).parts)-1)
        extra=f'<meta name="description" content="{esc(desc)}"><link rel="canonical" href="{url}"><meta name="robots" content="index,follow"><meta property="og:type" content="website"><meta property="og:site_name" content="TunXBox"><meta property="og:title" content="{esc(title)}"><meta property="og:description" content="{esc(desc)}"><meta property="og:url" content="{url}"><meta name="twitter:card" content="summary"><meta name="twitter:title" content="{esc(title)}"><meta name="twitter:description" content="{esc(desc)}"><link rel="icon" type="image/svg+xml" href="{prefix}favicon.svg">'
        if rel in ('index.html','en/index.html'):
            extra+=f'<link rel="alternate" hreflang="ru" href="{BASE}"><link rel="alternate" hreflang="en" href="{BASE}en/"><link rel="alternate" hreflang="x-default" href="{BASE}en/">'
            data={'@context':'https://schema.org','@type':'SoftwareApplication','name':'TunXBox','url':url,'operatingSystem':'Android 5.0 or later','applicationCategory':'UtilitiesApplication','softwareVersion':STABLE,'isAccessibleForFree':True,'license':'https://www.gnu.org/licenses/gpl-3.0.html','downloadUrl':REPO+'/releases/tag/v'+STABLE,'codeRepository':REPO,'inLanguage':'ru' if rel=='index.html' else 'en','description':desc}
            extra+='<script type="application/ld+json">'+json.dumps(data,ensure_ascii=False).replace('</','<\\/')+'</script>'
        s=s.replace('</head>',extra+'</head>');p.write_text(s);urls.append(url)
    ns='http://www.sitemaps.org/schemas/sitemap/0.9';ET.register_namespace('',ns);tree=ET.Element('{'+ns+'}urlset')
    for url in urls:ET.SubElement(ET.SubElement(tree,'{'+ns+'}url'),'{'+ns+'}loc').text=url
    ET.ElementTree(tree).write(out/'sitemap.xml',encoding='utf-8',xml_declaration=True)
    (out/'robots.txt').write_text('User-agent: *\nAllow: /TunXBox/\nSitemap: '+BASE+'sitemap.xml\n')
    (out/'SEO.md').write_text('Every HTML route has canonical/description/social metadata; RU/EN homes have reciprocal hreflang and SoftwareApplication JSON-LD. No invented ratings, offers or verification tokens. Submit sitemap.xml only after account-owner verification in Google Search Console/Yandex Webmaster. Project-path robots.txt is not origin-root robots.txt. Indexing/ranking is not guaranteed. Stable-version metadata changes only after actual publication. Regenerate with buildScript/build_project_site.py; SEO is applied automatically.\n')
    p=out/'site-manifest.json';m=json.loads(p.read_text());m['seo']={'canonicalBase':BASE,'sitemap':'sitemap.xml','languages':['ru','en'],'searchConsoleVerified':False};p.write_text(json.dumps(m,ensure_ascii=False,indent=2)+'\n')
