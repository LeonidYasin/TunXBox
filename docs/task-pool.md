# Пул задач и регламент автономных AI-агентов (Task Pool & Agent Protocol)

Этот документ описывает единый пул задач проекта TunXBox и правила для автономных AI-агентов (Jules, Claude, Codex и др.) по выбору, выполнению, проверке и слиянию задач.

---

## 1. Регламент работы агента (Autonomous Agent Workflow)

1. **Выбор задачи из пула (Task Pick)**:
   - Агент просматривает раздел [2. Пул задач (Task Pool)](#2-пул-задач-task-pool).
   - Выбирает наиболее приоритетную задачу в статусе `Backlog` или `Open`.
   - Обновляет статус выбранной задачи на `In Progress` с указанием своего имени/ID и имени создаваемой ветки (`feat/*` или `fix/*`).

2. **Разработка и локальная проверка (Development & Local Verification)**:
   - Все изменения выполняются в отдельной ветке.
   - Обязательно запускаются локальные unit-тесты: `python3 -m unittest discover -s buildScript -p 'test_*.py'`.
   - Проверяется целостность документации: `python3 buildScript/check_project_docs.py --update-index` и `python3 buildScript/check_project_docs.py`.

3. **Создание Pull Request и проверки в GitHub Actions (PR & Verification)**:
   - Агент отправляет ветку в репозиторий и создает PR в ветку `master` с помощью `scripts/github_mcp_cli.py` или Git API.
   - Проверяет результаты прохождения CI (GitHub Actions workflow runs & jobs) через `scripts/github_mcp_cli.py workflows list`.

4. **Автономное слияние (Autonomous Merge)**:
   - Если **все тесты в CI прошли успешно**, нет конфликтов слияния (`mergeable: true`) и нет замечаний/проблем, агент **самостоятельно мержит PR** командой `scripts/github_mcp_cli.py prs merge --number <PR_NUM>`.
   - Обновляет статус задачи в [docs/task-pool.md](task-pool.md) на `Done` и оставляет запись с номером смерженного PR и хэшем коммита.

---

## 2. Пул задач (Task Pool)

| ID | Приоритет | Название задачи / Описание | Статус | Исполнитель / Ветка / PR |
|---|---|---|---|---|
| **TASK-001** | High | Инструмент CLI работы с GitHub Actions, логами сборки и PR | `Done` | Jules (`scripts/github_mcp_cli.py`, PR #41) |
| **TASK-002** | High | Механизм пула задач и регламент автономной работы агентов | `Done` | Jules (`docs/task-pool.md`, `AGENTS.md`) |
| **TASK-003** | Medium | Расширенная проверка логов сборки и отчетов об ошибках через `github_mcp_cli.py` | `Backlog` | — |
| **TASK-004** | Medium | Поддержка версионирования и автоматической валидации конфигураций при обновлении приложения | `Backlog` | — |
| **TASK-005** | Low | Оптимизация поиска локальных устройств по LAN для TV-экрана | `Backlog` | — |

---

## 3. Статусы задач (Task Statuses)

- **`Backlog`**: Задача готова к взятию в работу агентом.
- **`In Progress`**: Задача взята агентом, создана ветка, идет разработка.
- **`Ready for Verification`**: PR создан, ожидается завершение CI.
- **`Done`**: PR проверен, CI успешен, PR смержен, задачи обновлены.
