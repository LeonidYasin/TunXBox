# GitHub Pages: публикация сайта TunXBox

Сайт генерируется из README/docs и SVG-майндмэп без внешних JavaScript, trackers, форм сбора данных или сторонних платёжных реквизитов. Почта опубликована по явному разрешению владельца. Гипотезы не выдаются за реализованные функции.

## Source / build

`python3 buildScript/build_project_site.py --output website` создаёт статический сайт. `python3 buildScript/test_project_site.py` проверяет ссылки, контакт, статус и отсутствие tracking/payment/secret полей. Вывод `website/` публикуется в корень ветки `gh-pages`; исходники приложения в master/main не мержатся ради сайта. Ветка сайта не заменяет APK Releases.

## Активация (если сайт ещё не включён)

На GitHub: **Settings → Pages → Build and deployment → Deploy from a branch → gh-pages → / (root) → Save**. Для бесплатного канала багов отдельно **Settings → General → Features → Issues**; Discussions — по желанию. У доступных MCP инструментов нет Pages/has_issues настройки, поэтому отсутствие API ошибки не выдаётся за успешную активацию. [Запрос инструмента](https://github.com/LeonidYasin/mcp-server/issues/68).

Ожидаемый адрес: https://LeonidYasin.github.io/TunXBox/ . После активации проверить HTTP 200 и наличие «TunXBox»/контакта/актуальной карты, а также ссылки и мобильную ширину. Наличие gh-pages ветки само по себе не доказывает live deployment. HTTPS и пользовательские настройки домена не менять неявно.

## Обновление

Сначала изменить Markdown, затем сгенерировать/проверить сайт и атомарно обновить статические файлы gh-pages. Обновление сайта — отдельная публикация от релиза APK. Не помещать private fixtures, ключи, подписки или build/signing workspace в site payload. Подробный branch/root/commit deployment proof сохранять после проверки.
