# GitHub Pages: публикация сайта TunXBox

Сайт генерируется из README/docs и SVG-майндмэп без внешних JavaScript, trackers, форм сбора данных или сторонних платёжных реквизитов. Почта опубликована по явному разрешению владельца. Гипотезы не выдаются за реализованные функции.

## Source / build

`python3 buildScript/build_project_site.py --output website` создаёт статический сайт. `python3 buildScript/test_project_site.py` проверяет ссылки, контакт, статус и отсутствие tracking/payment/secret полей. Вывод `website/` публикуется в корень ветки `gh-pages`; исходники приложения в master/main не мержатся ради сайта. Ветка сайта не заменяет APK Releases.

## Подтверждённая первая публикация

Создание gh-pages запустило native Pages pipeline автоматически. Первый запуск не прошёл из-за унаследованного dangling `buildScript/nkmr` при архивировании. Отдельный workflow `.github/workflows/prepare-static-pages.yml` **только в ветке сайта** оставил static pages/stylesheet/SVG/manifest и явно запросил Pages build. Master/main и APK-публикация не менялись.

[Deployment 38085797255](https://github.com/LeonidYasin/TunXBox/actions/runs/38085797255) завершился SUCCESS; публичный https://leonidyasin.github.io/TunXBox/ отвечает HTTP 200, email и карта подтверждены. Публичный неавторизованный GET Pages API может возвращать 404 — это не заменяет проверку deployment и публичного сайта.

Настройки на будущее: **Settings → Pages → Deploy from a branch → gh-pages → / (root)**. Уже работающий сайт повторно включать не требуется. Для GitHub баг-репортов отдельно **Settings → General → Features → Issues**; Discussions — по желанию. Эти функции сейчас выключены, поэтому рабочий канал — почта. У MCP пока нет инструментов управления этими настройками; [запрос](https://github.com/LeonidYasin/mcp-server/issues/68).

Публичный адрес: https://leonidyasin.github.io/TunXBox/ . После активации проверить HTTP 200 и наличие «TunXBox»/контакта/актуальной карты, а также ссылки и мобильную ширину. Наличие gh-pages ветки само по себе не доказывает live deployment. HTTPS и пользовательские настройки домена не менять неявно.

## Обновление

Сначала изменить Markdown, затем сгенерировать/проверить сайт и атомарно обновить статические файлы gh-pages. Обновление сайта — отдельная публикация от релиза APK. Не помещать private fixtures, ключи, подписки или build/signing workspace в site payload. Подробный branch/root/commit deployment proof сохранять после проверки.
