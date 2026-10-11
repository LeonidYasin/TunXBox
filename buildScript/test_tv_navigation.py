#!/usr/bin/env python3
"""Source-linked navigation graph coverage and confirmation budget; no telemetry."""
import json,pathlib,re,unittest,xml.etree.ElementTree as ET,heapq
ROOT=pathlib.Path(__file__).resolve().parents[1]
DATA=ROOT/'docs/reference/tv-navigation.json'
def graph():return json.loads(DATA.read_text())
def confirmations(data,start,end):
 queue=[(0,start)];best={start:0}
 while queue:
  cost,node=heapq.heappop(queue)
  if node==end:return cost
  if cost!=best[node]:continue
  for e in data['edges']:
   if e['from']!=node:continue
   n=cost+e['confirm']
   if n<best.get(e['to'],999):best[e['to']]=n;heapq.heappush(queue,(n,e['to']))
 raise AssertionError('Unreachable '+end)
def render():
 d=graph();lines=['# Граф навигации ТВ — source-linked','','Сгенерировано из [машиночитаемого графа](reference/tv-navigation.json). Это переходы и OK-подтверждения, не скрытая телеметрия. Направления D-pad и реальная длительность измеряются отдельно на устройстве. При изменении action/menu IDs CI требует обновить граф.','', '```mermaid','flowchart LR']
 for n,label in d['nodes'].items():lines.append('    '+n+'["'+label+'"]')
 for e in d['edges']:
  label=e.get('label',e.get('action',e.get('menu','автоматически')))+' / OK '+str(e['confirm'])
  lines.append('    '+e['from']+' -->|"'+label+'"| '+e['to'])
 lines += ['```','','## Основной путь','','Раньше для явной проверки перед YouTube: подключиться → открыть статус → запустить тест → открыть YouTube: **4 OK**, плюс перемещения/первое согласие Android. Новый кандидат: подключиться → автоматический результат → YouTube: **2 OK**. Это модель подтверждений при уже выбранном профиле, не доказательство всех физических D-pad путей. Ошибка не открывает обязательный modal и не крадёт фокус.','', '## D-pad и порядок','','- Верхний ряд: подключение → группы → добавить → обновление приложения → Phone; начальный фокус и стабильные IDs сохранены.','- Следующий ряд: профиль + накопленные байты сессии + краткий итог → Диагностика → YouTube (когда сервис запущен). Из Connect: Down к сводке, Right к диагностике, ещё Right к YouTube при исходной колонке 0. Плитка диагностики показывает текущий результат, возраст URL-проверки и скорости; полный отчёт доступен по OK. Проверить на 720p/1080p и больших шрифтах.','- Выбранная группа идёт первой среди групп, остальные сохраняют пользовательский порядок. Каждая группа — отдельный ряд: проверка и ранжирование группы по OK, затем её профили или добавление в пустую группу. Все группы доступны без переключения фильтра; длинный список прокручивается вертикально, не обязан помещаться целиком на экран.','- После групп отдельная строка операций: действия профиля, обновление и диагностика подписки, тесты группы, Home, импорт, перезапуск и закрытие. «Все возможности» — строго последняя строка: общая кнопка и отдельные кнопки каждого доступного раздела TvFunctionCatalog. Updater открывается одной OK-кнопкой без промежуточного каталога.','','## Следующая оптимизация (не реализована этим графом)','','Собрать редкие Home/restart/close и отчёты в один список «Ещё», убрать дубли импорта без потери методов, прямой updater уже есть в верхней строке. Сравнить частые и редкие сценарии: OK, D-pad, диалоги, возврат Back, focus stability, error recovery. Не уменьшать клики удаления ценой подтверждения и не скрывать ошибки. Все функции полного [каталога](features.md) должны оставаться достижимы.','', '## Критерии регулярного review','','После каждого UI PR обновлять JSON/граф, CI coverage/confirmation budget и реальные скриншоты/DPAD smoke. Проверять выбранный/активный профиль, offline/failure/no profiles, смену сети, отмену, YouTube return, installer return. Граф не утверждает готовность runtime функций до CI и device acceptance.','']
 return '\n'.join(lines)
class NavigationTest(unittest.TestCase):
 def test_all_runtime_actions_are_mapped(self):
  s=(ROOT/'app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt').read_text();s=s.split('private fun handleAction(id: Long)')[1].split('fun handlePlayPause()')[0]
  actual=set(re.findall(r'\b([A-Z][A-Z_]*)\s*->',s));mapped={e['action'] for e in graph()['edges'] if 'action' in e};self.assertEqual(actual,mapped)
 def test_row_order_matches_visible_code(self):
  s=(ROOT/'app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt').read_text()
  for kind,next_marker in [('actions','val updating'),('tools','operationsAdapter.setItems'),('operations','val rows=')]:
   body=s.split(kind+'Adapter.setItems(listOf(')[1].split(next_marker)[0]
   self.assertEqual(graph()['rows'][kind],re.findall(r'TvAction\(([A-Z_]+),',body))
  self.assertEqual(['STATUS','DIAGNOSTICS','YOUTUBE'],graph()['rows']['connection'])
  body=s.split('private fun refreshConnectionCards()')[1].split('private fun restoreFocus()')[0]
  self.assertLess(body.index('TvConnectionCard(STATUS'),body.index('TvQualityCard(DIAGNOSTICS'))
 def test_dynamic_group_rows_and_direct_catalog_match_runtime(self):
  s=(ROOT/'app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt').read_text()
  self.assertEqual(['actions','connection','group:selected-first','operations','tools'],graph()['rows']['order'])
  body=s.split('val rows=mutableListOf')[1].split('rowAdapter.setItems')[0]
  order=[body.index(x) for x in ['HeaderItem(ROW_ACTIONS','HeaderItem(ROW_CONNECTION','for(g in TvGroupRows.orderedGroupIds','HeaderItem(ROW_OPERATIONS','HeaderItem(ROW_TOOLS']]
  self.assertEqual(sorted(order),order)
  self.assertIn('TvFunctionCatalog.entries(requireContext()',s)
  self.assertIn('is TvFunctionCard -> openFullScreen(item.id)',s)
  select=s.split('private fun selectGroup(id: Long)')[1].split('private fun chooseGroup()')[0]
  self.assertNotIn('SagerNet.',select);self.assertNotIn('execute(',select)
 def test_direct_updater_budget(self):self.assertEqual(1,confirmations(graph(),'TV','UPDATER'))
 def test_three_launcher_cards_and_bundled_release_info(self):
  s=(ROOT/'app/src/main/java/io/nekohasekai/sagernet/ui/ModeSelectionActivity.kt').read_text()
  self.assertEqual(['release','tv','phone'],graph()['launcher'])
  order=[s.index('val '+x+' = card(') for x in graph()['launcher']]
  self.assertEqual(sorted(order),order);self.assertNotIn('val about = card(',s)
  self.assertIn('BuildConfig.VERSION_NAME',s);self.assertIn('releaseCard?.requestFocus()',s)
  page=(ROOT/'app/src/main/java/io/nekohasekai/sagernet/ui/ReleaseNotesActivity.kt').read_text()
  self.assertIn('R.string.release_changes_body',page);self.assertIn('R.string.release_checks_body',page)
  self.assertNotIn('GitHubUpdater',page);self.assertNotIn('SagerNet.',page)
  manifest=ET.parse(ROOT/'app/src/main/AndroidManifest.xml');ns='{http://schemas.android.com/apk/res/android}'
  activity=next(a for a in manifest.getroot().find('application').findall('activity') if a.get(ns+'name','').endswith('.ReleaseNotesActivity'))
  self.assertEqual('false',activity.get(ns+'exported'))
 def test_release_about_is_two_confirmations(self):self.assertEqual(2,confirmations(graph(),'MODE','ABOUT'))
 def test_ids_and_menu_entries_exist(self):
  d=graph();ids=set(d['nodes']);menu={x.get('{http://schemas.android.com/apk/res/android}id','').split('/')[-1] for x in ET.parse(ROOT/'app/src/main/res/menu/main_drawer_menu.xml').getroot().iter('item')}
  for e in d['edges']:
   self.assertIn(e['from'],ids);self.assertIn(e['to'],ids);self.assertIn(e['confirm'],[0,1]);self.assertTrue('menu' not in e or e['menu'] in menu)
 def test_connect_result_youtube_budget(self):self.assertEqual(2,confirmations(graph(),'TV','YOUTUBE'))
 def test_generated_doc_is_current(self):self.assertEqual(render(),(ROOT/'docs/tv-navigation.md').read_text())
if __name__=='__main__':unittest.main()
