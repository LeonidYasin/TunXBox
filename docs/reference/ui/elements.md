# Реестр элементов интерфейса

**SOURCE_ONLY**: это адресуемый реестр исходников, не число видимых кнопок и не визуальная приёмка. Контейнеры и скрытые/условные ресурсы включены. Каждая строка требует проверки связи с экраном и состояниями. Полные атрибуты и RU/EN — в `elements.json`.

| Источник / узел | Тип / ID / key | EN → RU | Риск для проверки |
|---|---|---|---|
| app/src/main/res/layout/activity_main_tv.xml#0 | FrameLayout @+id/tv_container |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/card_profile_tv.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/card_profile_tv.xml#1 | TextView @+id/profile_name |  → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER |
| app/src/main/res/layout/card_profile_tv.xml#2 | TextView @+id/profile_protocol |  → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER |
| app/src/main/res/layout/card_profile_tv.xml#3 | TextView @+id/profile_address |  → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER |
| app/src/main/res/layout/card_profile_tv.xml#4 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/card_profile_tv.xml#5 | View @+id/status_dot |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/card_profile_tv.xml#6 | TextView @+id/profile_status |  → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER |
| app/src/main/res/layout/item_keyboard_key.xml#0 | TextView @+id/item_title | { → (fallback / динамический) | FIXED_HEIGHT_REQUIRES_FONT_SCALE, LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/layout/layout_about.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_about.xml#1 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_about.xml#2 | FrameLayout @+id/about_fragment_holder |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_add_entity.xml#0 | com.google.android.material.card.MaterialCardView  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_add_entity.xml#1 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_add_entity.xml#2 | TextView  | Add Profile → Добавить профиль | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#0 | androidx.coordinatorlayout.widget.CoordinatorLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#1 | com.google.android.material.appbar.AppBarLayout @+id/appbar |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#2 | com.google.android.material.appbar.CollapsingToolbarLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#3 | com.google.android.material.appbar.MaterialToolbar @+id/toolbar | TunXBox → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#4 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#5 | LinearLayout @+id/search_layout |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#6 | com.google.android.material.card.MaterialCardView  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#7 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#8 | com.google.android.material.chip.Chip @+id/show_system_apps | Show system apps → Показать системные приложения | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#9 | com.google.android.material.textfield.TextInputLayout  | Search… → Поиск… | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#10 | com.google.android.material.textfield.TextInputEditText @+id/search |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#11 | LinearLayout @+id/loading |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#12 | com.google.android.material.progressindicator.CircularProgressIndicator  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#13 | com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView @+id/list |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_list.xml#14 | include @+id/app_placeholder |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_placeholder.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_placeholder.xml#1 | TextView @+id/empty_message | Unable to read installed apps.\nThis is usually because the system has restricted app read permissions.\nPlease grant permissions in the system settings. → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_placeholder.xml#2 | com.google.android.material.button.MaterialButton @+id/open_settings | Open System Settings → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#1 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#2 | ScrollView @+id/update_page_scroll |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#3 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#4 | TextView  | Choose stable or test. Download and verify the APK here, without a browser. Android permission and installation confirmation remain mandatory. No VPN settings are changed. → Выберите стабильный или тестовый канал. APK скачивается и проверяется здесь, без браузера. Разрешение и подтверждение установки Android обязательны. Настройки VPN не меняются. | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#5 | RadioGroup @+id/update_channels |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#6 | RadioButton @+id/update_stable | Stable (default) → Стабильный (по умолчанию) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#7 | RadioButton @+id/update_test | Test / release candidate → Тестовый / release candidate | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#8 | Button @+id/update_check | Check latest version → Проверить последнюю версию | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#9 | TextView  | Update status and details → Статус и сведения об обновлении | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#10 | ScrollView @+id/update_log_scroll |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#11 | TextView @+id/update_status |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#12 | ProgressBar @+id/update_progress |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#13 | Button @+id/update_download | Download / resume and verify APK → Скачать / продолжить и проверить APK | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#14 | Button @+id/update_pause | Pause download → Приостановить загрузку | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#15 | Button @+id/update_install | Install verified APK → Установить проверенный APK | SOURCE_ONLY |
| app/src/main/res/layout/layout_app_updates.xml#16 | Button @+id/update_details | Show full details → Показать сведения целиком | SOURCE_ONLY |
| app/src/main/res/layout/layout_appbar.xml#0 | com.google.android.material.appbar.AppBarLayout @+id/appbar |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_appbar.xml#1 | com.google.android.material.appbar.MaterialToolbar @+id/toolbar | TunXBox → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#0 | androidx.coordinatorlayout.widget.CoordinatorLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#1 | com.google.android.material.appbar.AppBarLayout @+id/appbar |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#2 | com.google.android.material.appbar.CollapsingToolbarLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#3 | com.google.android.material.appbar.MaterialToolbar @+id/toolbar | TunXBox → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#4 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#5 | com.google.android.material.card.MaterialCardView  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#6 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#7 | com.google.android.material.chip.ChipGroup @+id/bypassGroup |  → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER |
| app/src/main/res/layout/layout_apps.xml#8 | com.google.android.material.chip.Chip @+id/appProxyModeDisable | Off → Выключенный | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#9 | com.google.android.material.chip.Chip @+id/appProxyModeOn | Proxy → Прокси | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#10 | com.google.android.material.chip.Chip @+id/appProxyModeBypass | Bypass → Обход | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#11 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#12 | com.google.android.material.chip.Chip @+id/show_system_apps | Show system apps → Показать системные приложения | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#13 | com.google.android.material.chip.Chip @+id/autoSelectProxyApps | Auto select proxy apps → Автоматический выбор прокси-приложений | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#14 | com.google.android.material.textfield.TextInputLayout  | Search… → Поиск… | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#15 | com.google.android.material.textfield.TextInputEditText @+id/search |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#16 | LinearLayout @+id/loading |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#17 | com.google.android.material.progressindicator.CircularProgressIndicator  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#18 | com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView @+id/list |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps.xml#19 | include @+id/app_placeholder |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps_item.xml#0 | com.google.android.material.card.MaterialCardView @+id/content |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps_item.xml#1 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps_item.xml#2 | ImageView @+id/itemicon |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps_item.xml#3 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_apps_item.xml#4 | TextView @+id/title | Package label → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER, LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/layout/layout_apps_item.xml#5 | TextView @+id/desc | com.package.name (10000) → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER, LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/layout/layout_apps_item.xml#6 | androidx.appcompat.widget.SwitchCompat @+id/itemcheck |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_asset_item.xml#0 | com.google.android.material.card.MaterialCardView @+id/content |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_asset_item.xml#1 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_asset_item.xml#2 | com.google.android.material.progressindicator.LinearProgressIndicator @+id/subscription_update_progress |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_asset_item.xml#3 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_asset_item.xml#4 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_asset_item.xml#5 | TextView @+id/asset_name |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_asset_item.xml#6 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_asset_item.xml#7 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_asset_item.xml#8 | TextView @+id/asset_status |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_asset_item.xml#9 | com.google.android.material.button.MaterialButton @+id/remote_asset_actions | Actions → Действия | SOURCE_ONLY |
| app/src/main/res/layout/layout_asset_item.xml#10 | com.google.android.material.button.MaterialButton @+id/rules_update | Update → Обновить | SOURCE_ONLY |
| app/src/main/res/layout/layout_assets.xml#0 | androidx.coordinatorlayout.widget.CoordinatorLayout @+id/coordinator |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_assets.xml#1 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_assets.xml#2 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_assets.xml#3 | androidx.swiperefreshlayout.widget.SwipeRefreshLayout @+id/refresh_layout |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_assets.xml#4 | androidx.recyclerview.widget.RecyclerView @+id/recycler_view |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_backup.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_backup.xml#1 | Button @+id/reset_settings | Restore default settings → Восстановить настройки по умолчанию | SOURCE_ONLY |
| app/src/main/res/layout/layout_backup.xml#2 | CheckBox @+id/backup_configurations | Groups and configurations → Группы и конфигурации | SOURCE_ONLY |
| app/src/main/res/layout/layout_backup.xml#3 | CheckBox @+id/backup_rules | Routing rules → Правила маршрутизации | SOURCE_ONLY |
| app/src/main/res/layout/layout_backup.xml#4 | CheckBox @+id/backup_settings | Settings → Настройки | SOURCE_ONLY |
| app/src/main/res/layout/layout_backup.xml#5 | Button @+id/action_export | Export → Экспортировать | SOURCE_ONLY |
| app/src/main/res/layout/layout_backup.xml#6 | Button @+id/action_share | Share → Поделиться | SOURCE_ONLY |
| app/src/main/res/layout/layout_backup.xml#7 | TextView  | If the routing settings are not backed up with configurations,         then custom outbounds will be lost. → Если правила маршрутизации резервируются без конфигураций, то пользовательские исходящие соединения будут потеряны. | SOURCE_ONLY |
| app/src/main/res/layout/layout_backup.xml#8 | Button @+id/action_import_file | Import from file → Импортировать из файла | SOURCE_ONLY |
| app/src/main/res/layout/layout_chain_settings.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_chain_settings.xml#1 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_chain_settings.xml#2 | androidx.fragment.app.FragmentContainerView @+id/settings |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_chain_settings.xml#3 | LinearLayout @+id/list_cell |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_chain_settings.xml#4 | com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView @+id/configuration_list |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_config_settings.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_config_settings.xml#1 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_config_settings.xml#2 | androidx.fragment.app.FragmentContainerView @+id/settings |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_debug.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_debug.xml#1 | Button @+id/debug_crash | Crash From Kt → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/layout/layout_debug.xml#2 | Button @+id/reset_settings | Reset Settings → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/layout/layout_edit_config.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_config.xml#1 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_config.xml#2 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_config.xml#3 | FrameLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_config.xml#4 | com.blacksquircle.ui.editorkit.widget.TextProcessor @+id/editor |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_config.xml#5 | com.blacksquircle.ui.editorkit.widget.TextScroller @+id/scroller |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_config.xml#6 | LinearLayout @+id/keyboard_container |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_config.xml#7 | ImageView @+id/action_tab |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_config.xml#8 | moe.matsuri.nb4a.ui.ExtendedKeyboard @+id/extended_keyboard |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_config.xml#9 | ImageView @+id/action_undo |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_config.xml#10 | ImageView @+id/action_redo |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_config.xml#11 | ImageView @+id/action_format |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_group.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_group.xml#1 | androidx.core.widget.NestedScrollView  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_group.xml#2 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_group.xml#3 | com.google.android.material.textfield.TextInputLayout @+id/group_name_layout |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_group.xml#4 | com.google.android.material.textfield.TextInputEditText @+id/group_name | Group name → Имя группы | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_group.xml#5 | com.google.android.material.textfield.TextInputLayout @+id/group_links_layout |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_group.xml#6 | com.google.android.material.textfield.TextInputEditText @+id/group_subscription_link | Subscription Link → Ссылка для подписки | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_group.xml#7 | com.google.android.material.card.MaterialCardView @+id/deduplication_card |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_group.xml#8 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_group.xml#9 | TextView  | Deduplication → Дедупликация | SOURCE_ONLY |
| app/src/main/res/layout/layout_edit_group.xml#10 | com.google.android.material.checkbox.MaterialCheckBox @+id/deduplication |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_empty.xml#0 | androidx.coordinatorlayout.widget.CoordinatorLayout @+id/fragment_holder |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_empty_route.xml#0 | com.google.android.material.card.MaterialCardView  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_empty_route.xml#1 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_empty_route.xml#2 | TextView  | Make sure you have read the documentation before adding custom rules,         otherwise you may not be able to connect to the Internet. → Перед добавлением собственных правил убедитесь, что вы прочитали документацию, иначе подключение к Интернету может не удасться. | SOURCE_ONLY |
| app/src/main/res/layout/layout_group.xml#0 | androidx.coordinatorlayout.widget.CoordinatorLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group.xml#1 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group.xml#2 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group.xml#3 | com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView @+id/group_list |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_item.xml#0 | com.google.android.material.card.MaterialCardView @+id/content |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_item.xml#1 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_item.xml#2 | com.google.android.material.progressindicator.LinearProgressIndicator @+id/subscription_update_progress |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_item.xml#3 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_item.xml#4 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_item.xml#5 | TextView @+id/group_name | Group name → Имя группы | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_item.xml#6 | TextView @+id/group_user | user name → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/layout/layout_group_item.xml#7 | androidx.appcompat.widget.AppCompatImageView @+id/edit |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_item.xml#8 | androidx.appcompat.widget.AppCompatImageView @+id/options |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_item.xml#9 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_item.xml#10 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_item.xml#11 | TextView @+id/group_traffic | %s Used / %s Remaining → %s Использовано / %s Осталось | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_item.xml#12 | TextView @+id/group_status | Empty group → Пустая группа | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_item.xml#13 | com.google.android.material.button.MaterialButton @+id/group_update | Update → Обновить | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_list.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_list.xml#1 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_list.xml#2 | com.google.android.material.tabs.TabLayout @+id/group_tab |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_group_list.xml#3 | androidx.viewpager2.widget.ViewPager2 @+id/group_pager |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_icon_list_item_2.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_icon_list_item_2.xml#1 | ImageView @android:id/icon |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_icon_list_item_2.xml#2 | Space  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_icon_list_item_2.xml#3 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_icon_list_item_2.xml#4 | TextView @android:id/text1 |  → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER |
| app/src/main/res/layout/layout_icon_list_item_2.xml#5 | TextView @android:id/text2 |  → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER |
| app/src/main/res/layout/layout_import.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_import.xml#1 | CheckBox @+id/backup_configurations | Groups and configurations → Группы и конфигурации | SOURCE_ONLY |
| app/src/main/res/layout/layout_import.xml#2 | CheckBox @+id/backup_rules | Routing rules → Правила маршрутизации | SOURCE_ONLY |
| app/src/main/res/layout/layout_import.xml#3 | CheckBox @+id/backup_settings | Settings → Настройки | SOURCE_ONLY |
| app/src/main/res/layout/layout_import.xml#4 | TextView  | Importing will overwrite the existing data. → При импорте существующие данные будут перезаписаны. | SOURCE_ONLY |
| app/src/main/res/layout/layout_lan_discovery.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_lan_discovery.xml#1 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_lan_discovery.xml#2 | ScrollView  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_lan_discovery.xml#3 | LinearLayout @+id/lan_content |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_lan_discovery.xml#4 | TextView @+id/lan_explanation | Manual search only on a trusted Wi-Fi or Ethernet network with permission. Check gateways first, then device addresses, then neighbours. At most four /24-or-smaller windows, 1024 addresses, 8 ports, 8 concurrent probes and 45 seconds. No external traffic or credentials are sent; finding a listener does NOT prove Internet works. → Только ручной поиск в доверенной Wi-Fi или Ethernet-сети с разрешения её владельца. Сначала шлюзы, затем адреса устройства, затем соседи. Не более четырёх диапазонов /24 или меньше, 1024 адресов, 8 портов, 8 одновременных проверок и 45 секунд. Внешний трафик и пароли не отправляются; обнаруженный сервер НЕ доказывает доступность интернета. | SOURCE_ONLY |
| app/src/main/res/layout/layout_lan_discovery.xml#5 | TextView @+id/lan_scope |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_lan_discovery.xml#6 | com.google.android.material.button.MaterialButton @+id/lan_network | Refresh / choose network → Обновить / выбрать сеть | SOURCE_ONLY |
| app/src/main/res/layout/layout_lan_discovery.xml#7 | com.google.android.material.button.MaterialButton @+id/lan_gateway_action | Determine gateway again / add profile → Определить шлюз заново / добавить профиль | SOURCE_ONLY |
| app/src/main/res/layout/layout_lan_discovery.xml#8 | TextView @+id/lan_ports_label | Ports (comma-separated, up to 8 distinct values) → Порты (через запятую, до 8 разных значений) | SOURCE_ONLY |
| app/src/main/res/layout/layout_lan_discovery.xml#9 | EditText @+id/lan_ports |  → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER |
| app/src/main/res/layout/layout_lan_discovery.xml#10 | CheckBox @+id/lan_consent | I am allowed to probe this trusted network and approve the scope shown above. → У меня есть разрешение проверять эту доверенную сеть. Подтверждаю указанную область поиска. | SOURCE_ONLY |
| app/src/main/res/layout/layout_lan_discovery.xml#11 | com.google.android.material.button.MaterialButton @+id/lan_scan | Start manual search → Начать поиск вручную | SOURCE_ONLY |
| app/src/main/res/layout/layout_lan_discovery.xml#12 | com.google.android.material.button.MaterialButton @+id/lan_cancel | @android:string/cancel → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_lan_discovery.xml#13 | TextView @+id/lan_status | Ready. Nothing is scanned until you confirm and start. → Готово. Поиск не выполняется без подтверждения и запуска. | SOURCE_ONLY |
| app/src/main/res/layout/layout_lan_discovery.xml#14 | LinearLayout @+id/lan_results |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_loading.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_loading.xml#1 | com.google.android.material.progressindicator.LinearProgressIndicator  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_loading.xml#2 | androidx.core.widget.NestedScrollView  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_loading.xml#3 | TextView @+id/loadingText | Loading... → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/layout/layout_logcat.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_logcat.xml#1 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_logcat.xml#2 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_logcat.xml#3 | ScrollView @+id/scroolview |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_logcat.xml#4 | TextView @+id/textview |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_loglevel_help.xml#0 | TextView @+id/help | Long press the preference to set the buffer size. → Удерживайте для настройки размера буфера. | SOURCE_ONLY |
| app/src/main/res/layout/layout_main.xml#0 | androidx.drawerlayout.widget.DrawerLayout @+id/drawer_layout |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_main.xml#1 | androidx.coordinatorlayout.widget.CoordinatorLayout @+id/coordinator |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_main.xml#2 | androidx.coordinatorlayout.widget.CoordinatorLayout @+id/fragment_holder |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_main.xml#3 | com.google.android.material.progressindicator.CircularProgressIndicator @+id/fabProgress |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_main.xml#4 | io.nekohasekai.sagernet.widget.ServiceButton @+id/fab |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_main.xml#5 | io.nekohasekai.sagernet.widget.StatsBar @+id/stats |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_main.xml#6 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_main.xml#7 | TextView @+id/tx |  → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER |
| app/src/main/res/layout/layout_main.xml#8 | TextView @+id/rx |  → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER |
| app/src/main/res/layout/layout_main.xml#9 | TextView @+id/status | Success: HTTPS handshake took %dms → HTTPS-соединение установлено за %d мс | TEXT_LIMIT_REQUIRES_RENDER |
| app/src/main/res/layout/layout_main.xml#10 | com.google.android.material.navigation.NavigationView @+id/nav_view |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_main.xml#11 | com.google.android.material.navigation.NavigationView @+id/nav_view_black |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_mtu_help.xml#0 | TextView @+id/help | Long press the preference to set custom MTU. → Удерживайте для настройки своего MTU. | SOURCE_ONLY |
| app/src/main/res/layout/layout_network.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_network.xml#1 | com.google.android.material.card.MaterialCardView  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_network.xml#2 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_network.xml#3 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_network.xml#4 | TextView  | NAT behaviour discovery → Обнаружение поведения NAT | SOURCE_ONLY |
| app/src/main/res/layout/layout_network.xml#5 | TextView  | Determine the client\'s NAT mapping behaviour and the NAT         filtering behaviour defined in RFC 3478 using STUN. → Определить поведение сопоставления адресов NAT и поведение фильтрации NAT в соответствии с RFC 5780 при помощи STUN. | SOURCE_ONLY |
| app/src/main/res/layout/layout_network.xml#6 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_network.xml#7 | Button @+id/stunTest | Start → Старт | SOURCE_ONLY |
| app/src/main/res/layout/layout_password_dialog.xml#0 | ScrollView  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_password_dialog.xml#1 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_password_dialog.xml#2 | TextView @android:id/message |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_password_dialog.xml#3 | com.google.android.material.textfield.TextInputLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_password_dialog.xml#4 | com.google.android.material.textfield.TextInputEditText @android:id/edit |  → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER |
| app/src/main/res/layout/layout_profile.xml#0 | com.google.android.material.card.MaterialCardView @+id/content |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#1 | LinearLayout @+id/content_lin |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#2 | LinearLayout @+id/selected_view |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#3 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#4 | LinearLayout @+id/container |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#5 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#6 | TextView @+id/profile_name | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#7 | androidx.appcompat.widget.AppCompatImageView @+id/edit |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#8 | LinearLayout @+id/share |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#9 | LinearLayout @+id/share_layer |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#10 | androidx.appcompat.widget.AppCompatImageView @+id/shareIcon |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#11 | androidx.appcompat.widget.AppCompatImageView @+id/remove |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#12 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#13 | TextView @+id/profile_address | Server → Сервер | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#14 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#15 | TextView @+id/traffic_text | %1$s↑ %2$s↓ → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#16 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#17 | TextView @+id/profile_type |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#18 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile.xml#19 | TextView @+id/profile_status |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_profile_list.xml#0 | com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView @+id/configuration_list |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_progress.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_progress.xml#1 | com.google.android.material.progressindicator.CircularProgressIndicator  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_progress.xml#2 | TextView @+id/content |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_progress_list.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_progress_list.xml#1 | com.google.android.material.progressindicator.CircularProgressIndicator @+id/progress_circular |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_progress_list.xml#2 | TextView @+id/now_testing |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_progress_list.xml#3 | TextView @+id/progress |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_promotions.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_promotions.xml#1 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_promotions.xml#2 | androidx.core.widget.NestedScrollView  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_promotions.xml#3 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_promotions.xml#4 | TextView  | Advertising in TunXBox → Реклама в TunXBox | SOURCE_ONLY |
| app/src/main/res/layout/layout_promotions.xml#5 | TextView  | TunXBox currently has no published advertising offers. This section no longer automatically opens the upstream NekoBox promotions site.\n\nTunXBox is a client, not a VPN subscription provider. Imported profiles and subscriptions belong to the providers you choose; their availability and terms are outside this project’s control. → В TunXBox сейчас нет размещённых рекламных предложений. Этот раздел больше не открывает автоматически рекламный сайт исходного NekoBox.\n\nTunXBox — клиент, а не продавец VPN-подписок. Импортированные профили и подписки относятся к выбранным вами провайдерам; их доступность и условия не контролируются этим проектом. | SOURCE_ONLY |
| app/src/main/res/layout/layout_promotions.xml#6 | com.google.android.material.button.MaterialButton @+id/promotions_project | TunXBox source code → Исходный код TunXBox | SOURCE_ONLY |
| app/src/main/res/layout/layout_promotions.xml#7 | com.google.android.material.button.MaterialButton @+id/promotions_issues | Report a TunXBox issue → Сообщить о проблеме TunXBox | SOURCE_ONLY |
| app/src/main/res/layout/layout_route.xml#0 | androidx.coordinatorlayout.widget.CoordinatorLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route.xml#1 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route.xml#2 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route.xml#3 | com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView @+id/route_list |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#0 | com.google.android.material.card.MaterialCardView @+id/content |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#1 | LinearLayout @+id/content_lin |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#2 | LinearLayout @+id/container |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#3 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#4 | TextView @+id/profile_name | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#5 | androidx.appcompat.widget.AppCompatImageView @+id/subscription |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#6 | androidx.appcompat.widget.AppCompatImageView @+id/edit |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#7 | LinearLayout @+id/share |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#8 | LinearLayout @+id/share_layer |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#9 | androidx.appcompat.widget.AppCompatImageView @+id/shareIcon |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#10 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#11 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#12 | TextView @+id/profile_type |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#13 | TextView @+id/route_outbound |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_route_item.xml#14 | androidx.appcompat.widget.SwitchCompat @+id/enable |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_scanner.xml#0 | FrameLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_scanner.xml#1 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_scanner.xml#2 | androidx.camera.view.PreviewView @+id/previewView |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_scanner.xml#3 | com.king.zxing.ViewfinderView @+id/viewfinderView |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_scanner.xml#4 | ImageView @+id/ivFlashlight |  → (fallback / динамический) | LABEL_REQUIRES_BINDING_REVIEW |
| app/src/main/res/layout/layout_scanner_tv.xml#0 | FrameLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_scanner_tv.xml#1 | androidx.camera.view.PreviewView @+id/previewView |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_scanner_tv.xml#2 | com.king.zxing.ViewfinderView @+id/viewfinderView |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_scanner_tv.xml#3 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_scanner_tv.xml#4 | TextView  | Scan QR code → Сканировать QR-код | SOURCE_ONLY |
| app/src/main/res/layout/layout_scanner_tv.xml#5 | ImageView @+id/ivFlashlight |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_scanner_tv.xml#6 | ImageView @+id/ivImportImage |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_scanner_tv.xml#7 | ImageView @+id/ivClose |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_scanner_tv.xml#8 | TextView @+id/tvHint | Scan a QR shown by another device/app, or choose a QR image. → Сканируйте QR, показанный другим устройством/приложением, или выберите изображение QR. | SOURCE_ONLY |
| app/src/main/res/layout/layout_settings_activity.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_settings_activity.xml#1 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_settings_activity.xml#2 | androidx.fragment.app.FragmentContainerView @+id/settings |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_settings_activity.xml#3 | androidx.recyclerview.widget.RecyclerView @+id/configuration_list |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#0 | androidx.coordinatorlayout.widget.CoordinatorLayout @+id/coordinator |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#1 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#2 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#3 | LinearLayout @+id/wait_layout |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#4 | TextView  | This may take a few minutes… → Пожалуйста, подождите… | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#5 | androidx.core.widget.NestedScrollView @+id/main_layout |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#6 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#7 | com.google.android.material.card.MaterialCardView  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#8 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#9 | TextView  | Stun server → STUN сервер | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#10 | EditText @+id/nat_stun_server | stun.voipgate.com:3478 → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/layout/layout_stun.xml#11 | com.google.android.material.card.MaterialCardView  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#12 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#13 | TextView  | NAT check result → Провести проверку NAT | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#14 | TextView @+id/nat_result |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#15 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_stun.xml#16 | Button @+id/stunTest | Start → Старт | SOURCE_ONLY |
| app/src/main/res/layout/layout_tools.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_tools.xml#1 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_tools.xml#2 | com.google.android.material.tabs.TabLayout @+id/tools_tab |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_tools.xml#3 | androidx.viewpager2.widget.ViewPager2 @+id/tools_pager |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_urltest_preference_dialog.xml#0 | ScrollView  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_urltest_preference_dialog.xml#1 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_urltest_preference_dialog.xml#2 | com.google.android.material.textfield.TextInputLayout @+id/input_layout |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_urltest_preference_dialog.xml#3 | com.google.android.material.textfield.TextInputEditText @android:id/edit |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_urltest_preference_dialog.xml#4 | LinearLayout @+id/concurrent_layout |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_urltest_preference_dialog.xml#5 | TextView  | Test concurrency → Число параллельных подключений | SOURCE_ONLY |
| app/src/main/res/layout/layout_urltest_preference_dialog.xml#6 | com.google.android.material.textfield.TextInputEditText @+id/edit_concurrent |  → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER |
| app/src/main/res/layout/layout_webview.xml#0 | LinearLayout  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_webview.xml#1 | include  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/layout_webview.xml#2 | WebView @+id/webview |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/layout/simple_menu_dropdown_item.xml#0 | CheckedTextView  |  → (fallback / динамический) | TEXT_LIMIT_REQUIRES_RENDER, FIXED_HEIGHT_REQUIRES_FONT_SCALE |
| app/src/main/res/menu/add_group_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_group_menu.xml#1 | item @+id/action_update_all | Update all subscriptions → Обновить все подписки | SOURCE_ONLY |
| app/src/main/res/menu/add_group_menu.xml#2 | item @+id/action_new_group | Create group → Создать группу | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#1 | item @+id/action_search | @android:string/search_go → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#2 | item @+id/action_add | Add Profile → Добавить профиль | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#3 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#4 | item @+id/action_scan_qr_code | Scan QR code → Отсканировать QR-код | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#5 | item @+id/action_import_clipboard | Import from Clipboard → Импорт из буфера обмена | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#6 | item @+id/action_import_file | Import from file → Импортировать из файла | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#7 | item @+id/action_send_to_tv | Send current group — show QR → Передать текущую группу — показать QR | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#8 | item @+id/action_gateway_proxy | Gateway proxy:10808 → Прокси шлюза:10808 | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#9 | item @+id/action_lan_discovery | Find LAN proxies → Поиск прокси в локальной сети | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#10 | item  | Manual Settings → Ручные настройки | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#11 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#12 | item @+id/action_new_socks | SOCKS → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#13 | item @+id/action_new_http | HTTP → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#14 | item @+id/action_new_ss | Shadowsocks → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#15 | item @+id/action_new_vmess | VMess → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#16 | item @+id/action_new_vless | VLESS → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/menu/add_profile_menu.xml#17 | item @+id/action_new_trojan | Trojan → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#18 | item @+id/action_new_trojan_go | Trojan Go → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#19 | item @+id/action_new_mieru | Mieru → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#20 | item @+id/action_new_naive | Naïve → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#21 | item @+id/action_new_hysteria | Hysteria → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#22 | item @+id/action_new_tuic | TUIC → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#23 | item @+id/action_new_shadowtls | ShadowTLS → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#24 | item @+id/action_new_anytls | AnyTLS → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#25 | item @+id/action_new_ssh | SSH → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#26 | item @+id/action_new_wg | WireGuard → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#27 | item @+id/action_new_config | Custom Config → Пользовательская конфигурация | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#28 | item @+id/action_new_chain | Proxy Chain → Прокси-цепочка | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#29 | item @+id/action_misc |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#30 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#31 | item @+id/action_update_subscription | Update current Group\'s subscription → Обновить подписку текущей группы | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#32 | item @+id/action_clear_traffic_statistics | Clear traffic statistics → Очистить статистику трафика | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#33 | item @+id/action_remove_duplicate | Remove duplicate servers → Удалить дубликаты серверов | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#34 | item @+id/action_connection_tcp_ping | TCPing → TCP Пинг | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#35 | item @+id/action_connection_url_test | URL Test → URL Тест | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#36 | item @+id/action_connection_test_clear_results | Clear test results → Очистить результаты теста | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#37 | item @+id/action_connection_test_delete_unavailable | Clear unavailable → Удалить недоступные | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#38 | item @+id/action_order | Order → Сортировка | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#39 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#40 | group  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#41 | item @+id/action_order_origin | Origin → По источнику | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#42 | item @+id/action_order_by_name | By Name → По имени | SOURCE_ONLY |
| app/src/main/res/menu/add_profile_menu.xml#43 | item @+id/action_order_by_delay | By Delay → По задержке | SOURCE_ONLY |
| app/src/main/res/menu/add_route_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/add_route_menu.xml#1 | item @+id/action_new_route | Create Route → Создать маршрут | SOURCE_ONLY |
| app/src/main/res/menu/add_route_menu.xml#2 | item @+id/action_reset_route | Reset → Сброс | SOURCE_ONLY |
| app/src/main/res/menu/add_route_menu.xml#3 | item @+id/action_manage_assets | Manage Route Assets → Управление ресурсами маршрутов | SOURCE_ONLY |
| app/src/main/res/menu/app_list_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/app_list_menu.xml#1 | item @+id/action_invert_selections | Invert selections → Инвертировать выбранное | SOURCE_ONLY |
| app/src/main/res/menu/app_list_menu.xml#2 | item @+id/action_clear_selections | Clear selections → Очистить выбранное | SOURCE_ONLY |
| app/src/main/res/menu/app_list_menu.xml#3 | item @+id/action_export_clipboard | Export to Clipboard → Экспорт в буфер обмена | SOURCE_ONLY |
| app/src/main/res/menu/app_list_menu.xml#4 | item @+id/action_import_clipboard | Import from Clipboard → Импорт из буфера обмена | SOURCE_ONLY |
| app/src/main/res/menu/group_action_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/group_action_menu.xml#1 | item @+id/action_share_subscription | Share Subscription → Поделиться подпиской | SOURCE_ONLY |
| app/src/main/res/menu/group_action_menu.xml#2 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/group_action_menu.xml#3 | item @+id/action_universal_clipboard | Export to Clipboard → Экспорт в буфер обмена | SOURCE_ONLY |
| app/src/main/res/menu/group_action_menu.xml#4 | item @+id/action_universal_qr | QR code → QR-код | SOURCE_ONLY |
| app/src/main/res/menu/group_action_menu.xml#5 | item @+id/action_export | Export → Экспортировать | SOURCE_ONLY |
| app/src/main/res/menu/group_action_menu.xml#6 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/group_action_menu.xml#7 | item @+id/action_export_clipboard | Export to Clipboard → Экспорт в буфер обмена | SOURCE_ONLY |
| app/src/main/res/menu/group_action_menu.xml#8 | item @+id/action_export_file | Export to file → Экспорт в файл | SOURCE_ONLY |
| app/src/main/res/menu/group_action_menu.xml#9 | item @+id/action_subscription_diagnostics | Subscription update diagnostics → Диагностика обновления подписки | SOURCE_ONLY |
| app/src/main/res/menu/group_action_menu.xml#10 | item @+id/action_clear | Clear → Очистить | SOURCE_ONLY |
| app/src/main/res/menu/import_asset_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/import_asset_menu.xml#1 | item @+id/action_import_file | Import from file → Импортировать из файла | SOURCE_ONLY |
| app/src/main/res/menu/logcat_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/logcat_menu.xml#1 | item @+id/action_refresh | Update → Обновить | SOURCE_ONLY |
| app/src/main/res/menu/logcat_menu.xml#2 | item @+id/action_send_logcat | Export debug information → Экспорт отладочной информации | SOURCE_ONLY |
| app/src/main/res/menu/logcat_menu.xml#3 | item @+id/action_clear_logcat | Clear Logcat → Очистить журнал | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#1 | group  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#2 | item @+id/nav_configuration | Configuration → Конфигурация | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#3 | item @+id/nav_group | Group → Группы | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#4 | item @+id/nav_route | Route → Маршруты | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#5 | item @+id/nav_settings | Settings → Настройки | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#6 | group @+id/misc |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#7 | item @+id/nav_logcat | Logs → Журналы | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#8 | item @+id/nav_traffic | sing-box Dashboard → Панель sing-box | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#9 | item @+id/nav_lan_discovery | Find LAN proxies → Поиск прокси в локальной сети | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#10 | item @+id/nav_tools | Tools → Инструменты | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#11 | group @+id/about |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#12 | item @+id/nav_tuiguang | Ads → Реклама | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#13 | item @+id/nav_faq | Document → Документы | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#14 | item @+id/nav_app_updates | Update application → Обновление приложения | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#15 | item @+id/nav_about | About → О приложении | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#16 | group @+id/tv_mode |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#17 | item @+id/nav_switch_tv_mode | 📺 Switch to TV Mode → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#18 | group @+id/app_lifecycle |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#19 | item @+id/nav_restart_app | Restart app → Перезагрузить приложение | SOURCE_ONLY |
| app/src/main/res/menu/main_drawer_menu.xml#20 | item @+id/nav_close_app | Close app → Закрыть приложение | SOURCE_ONLY |
| app/src/main/res/menu/main_options_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/main_options_menu.xml#1 | item @+id/action_switch_tv_mode | 📺 TV Mode → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/menu/main_phone_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/main_phone_menu.xml#1 | item @+id/action_switch_tv_mode | 📺 TV Mode → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/menu/per_app_proxy_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/per_app_proxy_menu.xml#1 | item @+id/action_invert_selections | Invert selections → Инвертировать выбранное | SOURCE_ONLY |
| app/src/main/res/menu/per_app_proxy_menu.xml#2 | item @+id/action_clear_selections | Clear selections → Очистить выбранное | SOURCE_ONLY |
| app/src/main/res/menu/per_app_proxy_menu.xml#3 | item @+id/action_export_clipboard | Export to Clipboard → Экспорт в буфер обмена | SOURCE_ONLY |
| app/src/main/res/menu/per_app_proxy_menu.xml#4 | item @+id/action_import_clipboard | Import from Clipboard → Импорт из буфера обмена | SOURCE_ONLY |
| app/src/main/res/menu/profile_apply_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/profile_apply_menu.xml#1 | item @+id/action_apply | Apply → Применить | SOURCE_ONLY |
| app/src/main/res/menu/profile_config_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/profile_config_menu.xml#1 | item @+id/action_delete | Remove → Удалить | SOURCE_ONLY |
| app/src/main/res/menu/profile_config_menu.xml#2 | item @+id/action_apply | Apply → Применить | SOURCE_ONLY |
| app/src/main/res/menu/profile_config_menu.xml#3 | item @+id/action_create_shortcut | Create Shortcut → Создать ярлык | SOURCE_ONLY |
| app/src/main/res/menu/profile_config_menu.xml#4 | item @+id/action_move | Move → Переместить | SOURCE_ONLY |
| app/src/main/res/menu/profile_config_menu.xml#5 | item @+id/action_custom_outbound_json | Custom outbound JSON → Пользовательский исходящий JSON | SOURCE_ONLY |
| app/src/main/res/menu/profile_config_menu.xml#6 | item @+id/action_custom_config_json | Custom config JSON → Пользовательская конфигурация JSON | SOURCE_ONLY |
| app/src/main/res/menu/profile_share_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/profile_share_menu.xml#1 | item @+id/action_group_qr | QR code → QR-код | SOURCE_ONLY |
| app/src/main/res/menu/profile_share_menu.xml#2 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/profile_share_menu.xml#3 | item @+id/action_standard_qr | Standard → Стандарт | SOURCE_ONLY |
| app/src/main/res/menu/profile_share_menu.xml#4 | item @+id/action_universal_qr | SN Link → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/menu/profile_share_menu.xml#5 | item @+id/action_group_clipboard | Export to Clipboard → Экспорт в буфер обмена | SOURCE_ONLY |
| app/src/main/res/menu/profile_share_menu.xml#6 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/profile_share_menu.xml#7 | item @+id/action_standard_clipboard | Standard → Стандарт | SOURCE_ONLY |
| app/src/main/res/menu/profile_share_menu.xml#8 | item @+id/action_universal_clipboard | SN Link → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/menu/profile_share_menu.xml#9 | item @+id/action_group_configuration | Configuration → Конфигурация | SOURCE_ONLY |
| app/src/main/res/menu/profile_share_menu.xml#10 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/profile_share_menu.xml#11 | item @+id/action_config_export_clipboard | Export to Clipboard → Экспорт в буфер обмена | SOURCE_ONLY |
| app/src/main/res/menu/profile_share_menu.xml#12 | item @+id/action_config_export_file | Export to file → Экспорт в файл | SOURCE_ONLY |
| app/src/main/res/menu/scanner_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/scanner_menu.xml#1 | item @+id/action_import_file | Import from file → Импортировать из файла | SOURCE_ONLY |
| app/src/main/res/menu/traffic_item_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/traffic_item_menu.xml#1 | item  | Copy → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/traffic_item_menu.xml#2 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/traffic_item_menu.xml#3 | item @+id/copy_label | Copy Name → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/traffic_item_menu.xml#4 | item @+id/copy_package_name | Copy Package Name → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/traffic_item_menu.xml#5 | item  | Open → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/traffic_item_menu.xml#6 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/traffic_item_menu.xml#7 | item @+id/open_app | Open App → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/traffic_item_menu.xml#8 | item @+id/open_settings | Open Settings → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/traffic_item_menu.xml#9 | item @+id/open_market | Open Market → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/traffic_item_menu.xml#10 | item @+id/create_rule | Create Rule → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/traffic_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/traffic_menu.xml#1 | item @+id/action_clear_traffic_statistics | Clear traffic statistics → Очистить статистику трафика | SOURCE_ONLY |
| app/src/main/res/menu/yacd_menu.xml#0 | menu  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/menu/yacd_menu.xml#1 | item @+id/action_set_url | Set panel URL → Установить URL панель | SOURCE_ONLY |
| app/src/main/res/menu/yacd_menu.xml#2 | item @+id/close | @string/mal_close → Закрыть | SOURCE_ONLY |
| app/src/main/res/xml/anytls_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/anytls_preferences.xml#1 | EditTextPreference name | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/anytls_preferences.xml#2 | PreferenceCategory  | Server Settings → Настройки сервера | SOURCE_ONLY |
| app/src/main/res/xml/anytls_preferences.xml#3 | EditTextPreference serverAddress | Server → Сервер | SOURCE_ONLY |
| app/src/main/res/xml/anytls_preferences.xml#4 | EditTextPreference serverPort | Remote Port → Удаленный порт | SOURCE_ONLY |
| app/src/main/res/xml/anytls_preferences.xml#5 | EditTextPreference password | Password → Пароль | SOURCE_ONLY |
| app/src/main/res/xml/anytls_preferences.xml#6 | PreferenceCategory  | TLS Security Settings → Настройки безопасности | SOURCE_ONLY |
| app/src/main/res/xml/anytls_preferences.xml#7 | EditTextPreference sni | Server Name Indication → SNI (индикация имени сервера) | SOURCE_ONLY |
| app/src/main/res/xml/anytls_preferences.xml#8 | SwitchPreference allowInsecure | Allow Insecure → Разрешить небезопасное подключение | SOURCE_ONLY |
| app/src/main/res/xml/anytls_preferences.xml#9 | EditTextPreference alpn | Application-Layer Protocol Negotiation → Согласование протокола уровня приложений | SOURCE_ONLY |
| app/src/main/res/xml/anytls_preferences.xml#10 | EditTextPreference certificates | Certificates → Сертификаты | SOURCE_ONLY |
| app/src/main/res/xml/anytls_preferences.xml#11 | moe.matsuri.nb4a.ui.SimpleMenuPreference utlsFingerprint | uTLS fingerprint → Отпечаток uTLS | SOURCE_ONLY |
| app/src/main/res/xml/balancer_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/balancer_preferences.xml#1 | EditTextPreference profileName | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/balancer_preferences.xml#2 | moe.matsuri.nb4a.ui.SimpleMenuPreference balancerType | Type → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/balancer_preferences.xml#3 | moe.matsuri.nb4a.ui.SimpleMenuPreference balancerStrategy | Strategy → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/balancer_preferences.xml#4 | io.nekohasekai.sagernet.widget.GroupPreference balancerGroup | Group → Группы | SOURCE_ONLY |
| app/src/main/res/xml/config_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/config_preferences.xml#1 | EditTextPreference profileName | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/config_preferences.xml#2 | SwitchPreference isOutboundOnly | The JSON set is outbound → Сделать JSON исходящим | SOURCE_ONLY |
| app/src/main/res/xml/config_preferences.xml#3 | moe.matsuri.nb4a.ui.EditConfigPreference serverConfig | Custom Config → Пользовательская конфигурация | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#1 | PreferenceCategory  | App Settings → Настройки приложения | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#2 | SwitchPreference isAutoConnect | Auto Connect → Автоматическое подключение | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#3 | moe.matsuri.nb4a.ui.ColorPickerPreference appTheme | Theme → Тема | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#4 | moe.matsuri.nb4a.ui.SimpleMenuPreference nightTheme | Night Mode → Ночной режим | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#5 | moe.matsuri.nb4a.ui.SimpleMenuPreference serviceMode | Service Mode → Сервисный режим | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#6 | moe.matsuri.nb4a.ui.SimpleMenuPreference tunImplementation | TUN Implementation → Реализация TUN | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#7 | moe.matsuri.nb4a.ui.MTUPreference mtu | MTU → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#8 | moe.matsuri.nb4a.ui.SimpleMenuPreference speedInterval | Speed Notification Update Interval → Интервал обновления уведомлений о скорости | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#9 | SwitchPreference profileTrafficStatistics | Profile Traffic Statistics → Статистика трафика профиля | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#10 | SwitchPreference showDirectSpeed | Show Direct Speed → Показать прямую скорость | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#11 | SwitchPreference showGroupInNotification | Show group name in notification → Показывать название группы в уведомлении | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#12 | SwitchPreference alwaysShowAddress | Always Show Address → Всегда показывать адрес | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#13 | SwitchPreference meteredNetwork | Metered Hint → Подсказка о соединении с лимитным тарифным планом | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#14 | SwitchPreference acquireWakeLock | Acquire WakeLock → Использовать WakeLock | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#15 | moe.matsuri.nb4a.ui.LongClickListPreference logLevel | Log Level → Тип журнала | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#16 | moe.matsuri.nb4a.ui.EditConfigPreference globalCustomConfig | Custom Config → Пользовательская конфигурация | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#17 | PreferenceCategory  | Route Settings → Настройки маршрута | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#18 | SwitchPreference proxyApps | Apps VPN mode → Режим VPN для приложений | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#19 | SwitchPreference bypassLan | Bypass LAN → Обход LAN | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#20 | SwitchPreference bypassLanInCore | Bypass LAN in Core → Обход LAN в ядре | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#21 | moe.matsuri.nb4a.ui.SimpleMenuPreference trafficSniffing | Enable Traffic Sniffing → Включить анализ трафика | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#22 | SwitchPreference resolveDestination | Resolve Destination → Определить адрес назначения | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#23 | moe.matsuri.nb4a.ui.SimpleMenuPreference ipv6Mode | IPv6 Route → Маршрут IPv6 | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#24 | moe.matsuri.nb4a.ui.SimpleMenuPreference rulesProvider | Rule Assets Provider → Поставщик ресурсов правил | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#25 | PreferenceCategory  | DNS Settings → Настройки DNS | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#26 | EditTextPreference remoteDns | Remote DNS → Удаленный DNS | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#27 | moe.matsuri.nb4a.ui.SimpleMenuPreference domain_strategy_for_remote | Domain strategy for Remote → Правило домена для Удаленного | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#28 | EditTextPreference directDns | Direct DNS → Прямой DNS | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#29 | moe.matsuri.nb4a.ui.SimpleMenuPreference domain_strategy_for_direct | Domain strategy for Direct → Правило домена для Прямого | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#30 | moe.matsuri.nb4a.ui.SimpleMenuPreference domain_strategy_for_server | Domain strategy for Server address → Правило домена для Адреса сервера | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#31 | SwitchPreference enableDnsRouting | Enable DNS Routing → Включить маршрутизацию DNS | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#32 | SwitchPreference enableFakeDns | Enable FakeDNS → Включить FakeDNS | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#33 | PreferenceCategory  | Inbound Settings → Входящие настройки | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#34 | EditTextPreference mixedPort | Proxy Port → Порт прокси SOCKS5 | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#35 | SwitchPreference appendHttpProxy | Append HTTP Proxy to VPN → Добавить HTTP-прокси к VPN | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#36 | SwitchPreference allowAccess | Allow Connections from the LAN → Разрешить подключения из локальной сети | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#37 | PreferenceCategory  | Misc Settings → Прочие настройки | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#38 | moe.matsuri.nb4a.ui.UrlTestPreference connectionTestURL | Connection Test URL → URL-адрес проверки подключения | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#39 | SwitchPreference enableClashAPI | Enable Clash API → Включить Clash API | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#40 | SwitchPreference networkChangeResetConnections | Reset outbound connections when network changes → Сбросить исходящие соединения при изменении сети | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#41 | SwitchPreference wakeResetConnections | Reset outbound connections when device wake from sleep → Сбросить исходящие соединения при выходе из спящего режима | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#42 | SwitchPreference globalAllowInsecure | Always allow insecure → Всегда разрешать небезопасные | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#43 | SwitchPreference allowInsecureOnRequest | Disable certificate checking when updating         subscriptions → Отключить проверку сертификатов при обновлении подписок | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#44 | moe.matsuri.nb4a.ui.SimpleMenuPreference appTLSVersion | Subscription Min TLS Version → Минимальная версия TLS протокола | SOURCE_ONLY |
| app/src/main/res/xml/global_preferences.xml#45 | SwitchPreference showBottomBar | Show bottom bar like SagerNet → Показать нижнюю панель, как в SagerNet | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#1 | EditTextPreference groupName | Group name → Имя группы | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#2 | moe.matsuri.nb4a.ui.SimpleMenuPreference groupType | Group Type → Тип группы | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#3 | moe.matsuri.nb4a.ui.SimpleMenuPreference groupOrder | Order → Сортировка | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#4 | SwitchPreference groupIsSelector | Use selector → Использовать селектор | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#5 | io.nekohasekai.sagernet.widget.OutboundPreference groupFrontProxy | Front proxy → Прямой прокси | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#6 | io.nekohasekai.sagernet.widget.OutboundPreference groupLandingProxy | Landing Proxy → Обратный прокси | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#7 | PreferenceCategory groupSubscription | Subscription Settings → Настройки подписки | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#8 | io.nekohasekai.sagernet.widget.LinkOrContentPreference subscriptionLink | Subscription Link → Ссылка для подписки | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#9 | SwitchPreference subscriptionForceResolve | Force Resolve → Принудительно разрешать доменные имена | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#10 | SwitchPreference subscriptionDeduplication | Deduplication → Дедупликация | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#11 | PreferenceCategory subscriptionUpdate | Update Settings → Обновить настройки | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#12 | SwitchPreference subscriptionUpdateWhenConnectedOnly | Update only when connected → Обновлять только при подключении | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#13 | io.nekohasekai.sagernet.widget.UserAgentPreference subscriptionUserAgent | UserAgent → UserAgent | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#14 | SwitchPreference subscriptionAutoUpdate | Auto Update → Автоматическое обновление | SOURCE_ONLY |
| app/src/main/res/xml/group_preferences.xml#15 | EditTextPreference subscriptionAutoUpdateDelay | Auto Update Delay (In minutes) → Задержка автоматического обновления (в минутах) | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#1 | EditTextPreference profileName | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#2 | moe.matsuri.nb4a.ui.SimpleMenuPreference protocolVersion | Protocol Version → Версия протокола | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#3 | PreferenceCategory  | Server Settings → Настройки сервера | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#4 | EditTextPreference serverAddress | Server → Сервер | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#5 | EditTextPreference serverPorts | Remote Port → Удаленный порт | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#6 | EditTextPreference serverObfs | Obfuscation Password → Обфускация пароля | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#7 | moe.matsuri.nb4a.ui.SimpleMenuPreference serverAuthType | Authentication Type → Тип аутентификации | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#8 | EditTextPreference serverPassword | Authentication Payload → Полезные данные аутентификации | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#9 | moe.matsuri.nb4a.ui.SimpleMenuPreference serverProtocol | Protocol → Протокол | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#10 | EditTextPreference serverSNI | Server Name Indication → SNI (индикация имени сервера) | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#11 | EditTextPreference serverALPN | Application-Layer Protocol Negotiation → Согласование протокола уровня приложений | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#12 | EditTextPreference serverCertificates | Certificates → Сертификаты | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#13 | SwitchPreference serverAllowInsecure | Allow Insecure → Разрешить небезопасное подключение | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#14 | EditTextPreference serverUploadSpeed | Max Upload Speed (in Mbps) → Максимальная скорость загрузки (в Мбит / с) | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#15 | EditTextPreference serverDownloadSpeed | Max Download Speed (in Mbps) → Максимальная скорость загрузки (в Мбит / с) | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#16 | EditTextPreference serverStreamReceiveWindow | QUIC Stream Receive Window → Окно приема потока QUIC | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#17 | EditTextPreference serverConnectionReceiveWindow | QUIC Connection Receive Window → Окно получения соединения QUIC | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#18 | SwitchPreference serverDisableMtuDiscovery | Disable Path MTU Discovery → Отключить обнаружение MTU пути | SOURCE_ONLY |
| app/src/main/res/xml/hysteria_preferences.xml#19 | EditTextPreference hopInterval | Port Hopping Interval(second) → Интервал переключения портов(второй) | SOURCE_ONLY |
| app/src/main/res/xml/mieru_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/mieru_preferences.xml#1 | EditTextPreference profileName | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/mieru_preferences.xml#2 | PreferenceCategory  | Server Settings → Настройки сервера | SOURCE_ONLY |
| app/src/main/res/xml/mieru_preferences.xml#3 | EditTextPreference serverAddress | Server → Сервер | SOURCE_ONLY |
| app/src/main/res/xml/mieru_preferences.xml#4 | EditTextPreference serverPort | Remote Port → Удаленный порт | SOURCE_ONLY |
| app/src/main/res/xml/mieru_preferences.xml#5 | moe.matsuri.nb4a.ui.SimpleMenuPreference serverProtocol | Protocol → Протокол | SOURCE_ONLY |
| app/src/main/res/xml/mieru_preferences.xml#6 | EditTextPreference serverUsername | Username → Имя пользователя | SOURCE_ONLY |
| app/src/main/res/xml/mieru_preferences.xml#7 | EditTextPreference serverPassword | Password → Пароль | SOURCE_ONLY |
| app/src/main/res/xml/mieru_preferences.xml#8 | EditTextPreference serverMTU | MTU → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/naive_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/naive_preferences.xml#1 | EditTextPreference profileName | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/naive_preferences.xml#2 | PreferenceCategory  | Server Settings → Настройки сервера | SOURCE_ONLY |
| app/src/main/res/xml/naive_preferences.xml#3 | EditTextPreference serverAddress | Server → Сервер | SOURCE_ONLY |
| app/src/main/res/xml/naive_preferences.xml#4 | EditTextPreference serverPort | Remote Port → Удаленный порт | SOURCE_ONLY |
| app/src/main/res/xml/naive_preferences.xml#5 | EditTextPreference serverUsername | Username (Optional) → Имя пользователя (необязательно) | SOURCE_ONLY |
| app/src/main/res/xml/naive_preferences.xml#6 | EditTextPreference serverPassword | Password (Optional) → Пароль (необязательно) | SOURCE_ONLY |
| app/src/main/res/xml/naive_preferences.xml#7 | moe.matsuri.nb4a.ui.SimpleMenuPreference serverProtocol | Protocol → Протокол | SOURCE_ONLY |
| app/src/main/res/xml/naive_preferences.xml#8 | EditTextPreference serverHeaders | Extra Headers → Дополнительные заголовки | SOURCE_ONLY |
| app/src/main/res/xml/naive_preferences.xml#9 | EditTextPreference serverSNI | Server Name Indication → SNI (индикация имени сервера) | SOURCE_ONLY |
| app/src/main/res/xml/naive_preferences.xml#10 | EditTextPreference serverCertificates | Certificates → Сертификаты | SOURCE_ONLY |
| app/src/main/res/xml/naive_preferences.xml#11 | EditTextPreference serverInsecureConcurrency | Insecure Concurrency → Небезопасный параллелизм | SOURCE_ONLY |
| app/src/main/res/xml/naive_preferences.xml#12 | PreferenceCategory  | sing-box server → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/naive_preferences.xml#13 | SwitchPreference sUoT | UDP over TCP → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/name_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/name_preferences.xml#1 | EditTextPreference profileName | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/neko_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/neko_preferences.xml#1 | EditTextPreference name | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/route_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/route_preferences.xml#1 | EditTextPreference routeName | Route Name → Название маршрута | SOURCE_ONLY |
| app/src/main/res/xml/route_preferences.xml#2 | moe.matsuri.nb4a.ui.EditConfigPreference serverConfig | Custom Config → Пользовательская конфигурация | SOURCE_ONLY |
| app/src/main/res/xml/route_preferences.xml#3 | PreferenceCategory  | Route Settings → Настройки маршрута | SOURCE_ONLY |
| app/src/main/res/xml/route_preferences.xml#4 | io.nekohasekai.sagernet.widget.AppListPreference routePackages | Applications → Приложения | SOURCE_ONLY |
| app/src/main/res/xml/route_preferences.xml#5 | EditTextPreference routeDomain | domain → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/route_preferences.xml#6 | EditTextPreference routeIP | dst ip → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/route_preferences.xml#7 | EditTextPreference routePort | dst port → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/route_preferences.xml#8 | EditTextPreference routeSource | src ip → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/route_preferences.xml#9 | EditTextPreference routeSourcePort | src port → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/route_preferences.xml#10 | moe.matsuri.nb4a.ui.SimpleMenuPreference routeNetwork | Network → Сеть | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/route_preferences.xml#11 | EditTextPreference routeProtocol | Protocol → Протокол | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/route_preferences.xml#12 | io.nekohasekai.sagernet.widget.OutboundPreference routeOutbound | outbound → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/shadowsocks_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/shadowsocks_preferences.xml#1 | EditTextPreference name | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/shadowsocks_preferences.xml#2 | PreferenceCategory  | Server Settings → Настройки сервера | SOURCE_ONLY |
| app/src/main/res/xml/shadowsocks_preferences.xml#3 | EditTextPreference serverAddress | Server → Сервер | SOURCE_ONLY |
| app/src/main/res/xml/shadowsocks_preferences.xml#4 | EditTextPreference serverPort | Remote Port → Удаленный порт | SOURCE_ONLY |
| app/src/main/res/xml/shadowsocks_preferences.xml#5 | moe.matsuri.nb4a.ui.SimpleMenuPreference method | Encrypt Method → Метод шифрования | SOURCE_ONLY |
| app/src/main/res/xml/shadowsocks_preferences.xml#6 | EditTextPreference password | Password → Пароль | SOURCE_ONLY |
| app/src/main/res/xml/shadowsocks_preferences.xml#7 | PreferenceCategory  | Plugin → Плагин | SOURCE_ONLY |
| app/src/main/res/xml/shadowsocks_preferences.xml#8 | moe.matsuri.nb4a.ui.SimpleMenuPreference pluginName | Plugin → Плагин | SOURCE_ONLY |
| app/src/main/res/xml/shadowsocks_preferences.xml#9 | EditTextPreference pluginConfig | Configure… → Настроить… | SOURCE_ONLY |
| app/src/main/res/xml/shadowsocks_preferences.xml#10 | PreferenceCategory  | sing-box server → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/shadowsocks_preferences.xml#11 | SwitchPreference sUoT | UDP over TCP → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/shadowtls_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/shadowtls_preferences.xml#1 | EditTextPreference name | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/shadowtls_preferences.xml#2 | PreferenceCategory  | Server Settings → Настройки сервера | SOURCE_ONLY |
| app/src/main/res/xml/shadowtls_preferences.xml#3 | EditTextPreference serverAddress | Server → Сервер | SOURCE_ONLY |
| app/src/main/res/xml/shadowtls_preferences.xml#4 | EditTextPreference serverPort | Remote Port → Удаленный порт | SOURCE_ONLY |
| app/src/main/res/xml/shadowtls_preferences.xml#5 | moe.matsuri.nb4a.ui.SimpleMenuPreference version | Protocol Version → Версия протокола | SOURCE_ONLY |
| app/src/main/res/xml/shadowtls_preferences.xml#6 | EditTextPreference password | Password → Пароль | SOURCE_ONLY |
| app/src/main/res/xml/shadowtls_preferences.xml#7 | PreferenceCategory serverSecurityCategory | TLS Security Settings → Настройки безопасности | SOURCE_ONLY |
| app/src/main/res/xml/shadowtls_preferences.xml#8 | EditTextPreference sni | Server Name Indication → SNI (индикация имени сервера) | SOURCE_ONLY |
| app/src/main/res/xml/shadowtls_preferences.xml#9 | EditTextPreference alpn | Application-Layer Protocol Negotiation → Согласование протокола уровня приложений | SOURCE_ONLY |
| app/src/main/res/xml/shadowtls_preferences.xml#10 | EditTextPreference certificates | Certificates → Сертификаты | SOURCE_ONLY |
| app/src/main/res/xml/shadowtls_preferences.xml#11 | SwitchPreference allowInsecure | Allow Insecure → Разрешить небезопасное подключение | SOURCE_ONLY |
| app/src/main/res/xml/shadowtls_preferences.xml#12 | moe.matsuri.nb4a.ui.SimpleMenuPreference utlsFingerprint | uTLS fingerprint → Отпечаток uTLS | SOURCE_ONLY |
| app/src/main/res/xml/socks_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/socks_preferences.xml#1 | EditTextPreference profileName | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/socks_preferences.xml#2 | PreferenceCategory  | Server Settings → Настройки сервера | SOURCE_ONLY |
| app/src/main/res/xml/socks_preferences.xml#3 | moe.matsuri.nb4a.ui.SimpleMenuPreference serverProtocol | Version → Версия | SOURCE_ONLY |
| app/src/main/res/xml/socks_preferences.xml#4 | EditTextPreference serverAddress | Server → Сервер | SOURCE_ONLY |
| app/src/main/res/xml/socks_preferences.xml#5 | EditTextPreference serverPort | Remote Port → Удаленный порт | SOURCE_ONLY |
| app/src/main/res/xml/socks_preferences.xml#6 | EditTextPreference serverUsername | Username (Optional) → Имя пользователя (необязательно) | SOURCE_ONLY |
| app/src/main/res/xml/socks_preferences.xml#7 | EditTextPreference serverPassword | Password (Optional) → Пароль (необязательно) | SOURCE_ONLY |
| app/src/main/res/xml/socks_preferences.xml#8 | PreferenceCategory  | sing-box server → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/socks_preferences.xml#9 | SwitchPreference sUoT | UDP over TCP → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/ssh_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/ssh_preferences.xml#1 | EditTextPreference profileName | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/ssh_preferences.xml#2 | PreferenceCategory  | Server Settings → Настройки сервера | SOURCE_ONLY |
| app/src/main/res/xml/ssh_preferences.xml#3 | EditTextPreference serverAddress | Server → Сервер | SOURCE_ONLY |
| app/src/main/res/xml/ssh_preferences.xml#4 | EditTextPreference serverPort | Remote Port → Удаленный порт | SOURCE_ONLY |
| app/src/main/res/xml/ssh_preferences.xml#5 | EditTextPreference serverUsername | Username → Имя пользователя | SOURCE_ONLY |
| app/src/main/res/xml/ssh_preferences.xml#6 | moe.matsuri.nb4a.ui.SimpleMenuPreference serverAuthType | Authentication Type → Тип аутентификации | SOURCE_ONLY |
| app/src/main/res/xml/ssh_preferences.xml#7 | EditTextPreference serverPassword | Password → Пароль | SOURCE_ONLY |
| app/src/main/res/xml/ssh_preferences.xml#8 | EditTextPreference serverPrivateKey | Private Key → Закрытый ключ | SOURCE_ONLY |
| app/src/main/res/xml/ssh_preferences.xml#9 | EditTextPreference serverPassword1 | Private Key Passphrase → Парольная фраза закрытого ключа | SOURCE_ONLY |
| app/src/main/res/xml/ssh_preferences.xml#10 | EditTextPreference serverCertificates | Public Key → Открытый ключ | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#1 | EditTextPreference name | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#2 | PreferenceCategory  | Server Settings → Настройки сервера | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#3 | EditTextPreference serverAddress | Server → Сервер | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#4 | EditTextPreference serverPort | Remote Port → Удаленный порт | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#5 | EditTextPreference username | Username (Optional) → Имя пользователя (необязательно) | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#6 | EditTextPreference password | Password (Optional) → Пароль (необязательно) | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#7 | EditTextPreference uuid | User ID → Логин пользователя | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#8 | EditTextPreference alterId | Alter ID → Изменить ID | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#9 | moe.matsuri.nb4a.ui.SimpleMenuPreference encryption | Encryption → Шифрование | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#10 | moe.matsuri.nb4a.ui.SimpleMenuPreference packetEncoding | Packet Encoding → Кодирование пакетов | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#11 | moe.matsuri.nb4a.ui.SimpleMenuPreference type | Network → Сеть | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#12 | EditTextPreference host | HTTP Host → HTTP-хост | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#13 | EditTextPreference path | HTTP Path → HTTP-путь | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#14 | moe.matsuri.nb4a.ui.SimpleMenuPreference security | Transport layer encryption → Шифрование транспортного уровня | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#15 | PreferenceCategory serverWsCategory | WebSocket Settings → Настройки WebSocket | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#16 | EditTextPreference wsMaxEarlyData | Max early data → Максимальный объём данных | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#17 | EditTextPreference earlyDataHeaderName | Early Data Header Name → Имя заголовка ранних данных | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#18 | PreferenceCategory serverSecurityCategory | TLS Security Settings → Настройки безопасности | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#19 | EditTextPreference sni | Server Name Indication → SNI (индикация имени сервера) | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#20 | EditTextPreference alpn | Application-Layer Protocol Negotiation → Согласование протокола уровня приложений | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#21 | EditTextPreference certificates | Certificates → Сертификаты | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#22 | SwitchPreference allowInsecure | Allow Insecure → Разрешить небезопасное подключение | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#23 | PreferenceCategory serverTlsCamouflageCategory | TLS Camouflage Settings → Настройки защиты TLS | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#24 | moe.matsuri.nb4a.ui.SimpleMenuPreference utlsFingerprint | uTLS fingerprint → Отпечаток uTLS | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#25 | EditTextPreference realityPubKey | Reality Public Key → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/standard_v2ray_preferences.xml#26 | EditTextPreference realityShortId | Reality ShortId → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/standard_v2ray_preferences.xml#27 | PreferenceCategory serverMuxCategory | Mulitplex → Мультиплекс | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#28 | SwitchPreference enableMux | Enable Multiplexer → Включить мультиплексор | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#29 | moe.matsuri.nb4a.ui.SimpleMenuPreference muxType | Mux protocol → Протокол мультиплексирования | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#30 | EditTextPreference muxConcurrency | Mux Concurrent Connections → Число одновременных подключений Mux | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#31 | SwitchPreference muxPadding | Padding → Отступ | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#32 | PreferenceCategory serverECHCategory | ECH → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |
| app/src/main/res/xml/standard_v2ray_preferences.xml#33 | SwitchPreference enableECH | Enable → Включить | SOURCE_ONLY |
| app/src/main/res/xml/standard_v2ray_preferences.xml#34 | EditTextPreference echConfig | ECH Config → Конфигурация ECH | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#1 | EditTextPreference profileName | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#2 | PreferenceCategory  | Server Settings → Настройки сервера | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#3 | EditTextPreference serverAddress | Server → Сервер | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#4 | EditTextPreference serverPort | Remote Port → Удаленный порт | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#5 | EditTextPreference serverPassword | Password → Пароль | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#6 | EditTextPreference serverSNI | Server Name Indication → SNI (индикация имени сервера) | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#7 | SwitchPreference serverAllowInsecure | Allow Insecure → Разрешить небезопасное подключение | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#8 | moe.matsuri.nb4a.ui.SimpleMenuPreference serverNetwork | Network → Сеть | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#9 | moe.matsuri.nb4a.ui.SimpleMenuPreference serverEncryption | Encryption → Шифрование | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#10 | PreferenceCategory serverWsCategory | WebSocket Settings → Настройки WebSocket | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#11 | EditTextPreference serverHost | WebSocket Host → Хост WebSocket | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#12 | EditTextPreference serverPath | WebSocket Path → Путь к WebSocket | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#13 | PreferenceCategory serverSsCategory | Shadowsocks Settings → Настройки Shadowsocks | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#14 | moe.matsuri.nb4a.ui.SimpleMenuPreference serverMethod | Encrypt Method → Метод шифрования | SOURCE_ONLY |
| app/src/main/res/xml/trojan_go_preferences.xml#15 | EditTextPreference serverPassword1 | Password → Пароль | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#1 | EditTextPreference profileName | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#2 | PreferenceCategory  | Server Settings → Настройки сервера | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#3 | EditTextPreference serverAddress | Server → Сервер | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#4 | EditTextPreference serverPort | Remote Port → Удаленный порт | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#5 | EditTextPreference serverUsername | User ID → Логин пользователя | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#6 | EditTextPreference serverPassword | Password → Пароль | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#7 | EditTextPreference serverALPN | Application-Layer Protocol Negotiation → Согласование протокола уровня приложений | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#8 | EditTextPreference serverCertificates | Certificates → Сертификаты | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#9 | moe.matsuri.nb4a.ui.SimpleMenuPreference serverUDPRelayMode | UDP Relay Mode → Режим UDP-ретранслятора | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#10 | moe.matsuri.nb4a.ui.SimpleMenuPreference serverCongestionController | Congestion Controller → Контроллер перегрузки | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#11 | SwitchPreference serverDisableSNI | Disable SNI → Отключить SNI | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#12 | EditTextPreference serverSNI | Server Name Indication → SNI (индикация имени сервера) | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#13 | SwitchPreference serverReduceRTT | Enable 0-RTT QUIC handshake → Включить 0-RTT QUIC-соединение | SOURCE_ONLY |
| app/src/main/res/xml/tuic_preferences.xml#14 | SwitchPreference serverAllowInsecure | Allow Insecure → Разрешить небезопасное подключение | SOURCE_ONLY |
| app/src/main/res/xml/wireguard_preferences.xml#0 | PreferenceScreen  |  → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/wireguard_preferences.xml#1 | EditTextPreference name | Profile Name → Имя профиля | SOURCE_ONLY |
| app/src/main/res/xml/wireguard_preferences.xml#2 | PreferenceCategory  | Server Settings → Настройки сервера | SOURCE_ONLY |
| app/src/main/res/xml/wireguard_preferences.xml#3 | EditTextPreference serverAddress | Server → Сервер | SOURCE_ONLY |
| app/src/main/res/xml/wireguard_preferences.xml#4 | EditTextPreference serverPort | Remote Port → Удаленный порт | SOURCE_ONLY |
| app/src/main/res/xml/wireguard_preferences.xml#5 | EditTextPreference localAddress | Local Address → Частный адрес | SOURCE_ONLY |
| app/src/main/res/xml/wireguard_preferences.xml#6 | EditTextPreference privateKey | Private Key → Закрытый ключ | SOURCE_ONLY |
| app/src/main/res/xml/wireguard_preferences.xml#7 | EditTextPreference peerPublicKey | Peer Public Key → Открытый ключ сервера | SOURCE_ONLY |
| app/src/main/res/xml/wireguard_preferences.xml#8 | EditTextPreference peerPreSharedKey | Peer Pre-Shared Key → Пароль сервера | SOURCE_ONLY |
| app/src/main/res/xml/wireguard_preferences.xml#9 | EditTextPreference mtu | MTU → (fallback / динамический) | SOURCE_ONLY |
| app/src/main/res/xml/wireguard_preferences.xml#10 | EditTextPreference reserved | Reserved → (fallback / динамический) | LITERAL_REQUIRES_LOCALIZATION_REVIEW |

## Программные элементы

Это места конструирования, не гарантированные уникальные элементы. Создаваемые в цикле карточки имеют переменную кратность.

- `app/src/main/java/io/nekohasekai/sagernet/ui/AboutFragment.kt:203` — `.setPositiveButton(R.string.project_contact_tunxbox) { _, _ -> contactSupport() }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/AboutFragment.kt:204` — `.setNegativeButton(android.R.string.cancel, null).show()`
- `app/src/main/java/io/nekohasekai/sagernet/ui/AboutFragment.kt:242` — `.setPositiveButton(android.R.string.ok, null).show().also { dialog ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/AboutFragment.kt:285` — `.setNeutralButton(R.string.about_copy_build_info) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/AboutFragment.kt:288` — `}.setPositiveButton(android.R.string.ok, null).show().also { dialog ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/AppLifecycleActions.kt:22` — `.setPositiveButton(title) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/AppLifecycleActions.kt:26` — `.setNegativeButton(android.R.string.cancel, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/AppManagerActivity.kt:340` — `.setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/AppManagerActivity.kt:371` — `.setNegativeButton(R.string.no, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/AppUpdateFragment.kt:94` — `.setPositiveButton(android.R.string.ok,null).show()`
- `app/src/main/java/io/nekohasekai/sagernet/ui/AssetsActivity.kt:216` — `MaterialAlertDialogBuilder(this@AssetsActivity).setTitle(file.name).setItems(entries.map { getString(it.first) }.toTypedArray()) { _, index -> entries[index].second() }.show()`
- `app/src/main/java/io/nekohasekai/sagernet/ui/BackupFragment.kt:70` — `.setNegativeButton(R.string.no, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/BackupFragment.kt:71` — `.setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/BackupFragment.kt:230` — `.setPositiveButton(R.string.backup_import) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/BackupFragment.kt:260` — `.setNegativeButton(android.R.string.cancel, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/BlankActivity.kt:19` — `.setPositiveButton(R.string.crash_recovery_retry) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/BlankActivity.kt:23` — `.setNegativeButton(R.string.crash_recovery_close) { _, _ -> finishAndRemoveTask() }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/BlankActivity.kt:24` — `.setNeutralButton(R.string.crash_recovery_share, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/ConfigurationFragment.kt:327` — `.setNegativeButton(android.R.string.cancel, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/ConfigurationFragment.kt:328` — `.setPositiveButton(R.string.yes) { _, _ -> remoteWork {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/ConfigurationFragment.kt:338` — `.setItems(actions.map { getString(it.first) }.toTypedArray()) { _, index ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/ConfigurationFragment.kt:340` — `}.setNegativeButton(android.R.string.cancel, null).show()`
- `app/src/main/java/io/nekohasekai/sagernet/ui/ConfigurationFragment.kt:565` — `.setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/ConfigurationFragment.kt:584` — `.setNegativeButton(R.string.no, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/ConfigurationFragment.kt:617` — `.setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/ConfigurationFragment.kt:636` — `.setNegativeButton(R.string.no, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/ConfigurationFragment.kt:657` — `.setPositiveButton(R.string.minimize) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/ConfigurationFragment.kt:660` — `.setNegativeButton(android.R.string.cancel) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/GroupFragment.kt:119` — `.setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/GroupFragment.kt:126` — `.setNegativeButton(R.string.no, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/GroupFragment.kt:343` — `.setPositiveButton(android.R.string.ok, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/GroupFragment.kt:344` — `if (record != null) builder.setNeutralButton(R.string.subscription_attempt_share) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/GroupSettingsActivity.kt:168` — `setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/GroupSettingsActivity.kt:173` — `setNegativeButton(R.string.no) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/GroupSettingsActivity.kt:176` — `setNeutralButton(android.R.string.cancel, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/GroupSettingsActivity.kt:185` — `setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/GroupSettingsActivity.kt:191` — `setNegativeButton(R.string.no, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt:175` — `.setPositiveButton(android.R.string.ok, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt:265` — `.setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt:270` — `.setNegativeButton(android.R.string.cancel, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt:295` — `.setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt:300` — `.setNegativeButton(android.R.string.cancel, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt:336` — `.setPositiveButton(R.string.action_download) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt:339` — `.setNeutralButton(android.R.string.cancel, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt:340` — `.setNeutralButton(R.string.action_learn_more) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt:365` — `.setItems(items.toTypedArray()) { _, which ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/ModeSelectionActivity.kt:29` — `val scroll = ScrollView(this).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/ModeSelectionActivity.kt:40` — `fun text(value: String, size: Float, color: Int) = TextView(this).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/ReleaseNotesActivity.kt:27` — `val scroll=ScrollView(this).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/ReleaseNotesActivity.kt:35` — `fun label(value: String,size: Float,idValue: Int)=TextView(this).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/RemoteRowActions.kt:45` — `.setPositiveButton(R.string.yes) { _, _ -> action() }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/RemoteRowActions.kt:46` — `.setNegativeButton(android.R.string.cancel, null).create()`
- `app/src/main/java/io/nekohasekai/sagernet/ui/RouteFragment.kt:112` — `.setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/RouteFragment.kt:119` — `.setNegativeButton(R.string.no, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/RouteFragment.kt:285` — `MaterialAlertDialogBuilder(requireContext()).setTitle(rule.displayName()).setItems(actions.map { getString(it.first) }.toTypedArray()) { _, index -> actions[index].second() }.show()`
- `app/src/main/java/io/nekohasekai/sagernet/ui/RouteSettingsActivity.kt:184` — `setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/RouteSettingsActivity.kt:189` — `setNegativeButton(R.string.no) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/RouteSettingsActivity.kt:192` — `setNeutralButton(android.R.string.cancel, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/RouteSettingsActivity.kt:201` — `setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/RouteSettingsActivity.kt:207` — `setNegativeButton(R.string.no, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/RouteSettingsActivity.kt:263` — `.setPositiveButton(android.R.string.ok, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/SettingsPreferenceFragment.kt:94` — `val view = EditText(context).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/SettingsPreferenceFragment.kt:103` — `.setPositiveButton(android.R.string.ok) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/SettingsPreferenceFragment.kt:108` — `.setNegativeButton(android.R.string.cancel, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/StunActivity.kt:48` — `.setPositiveButton(android.R.string.ok) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/WebviewFragment.kt:58` — `val view = EditText(context).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/WebviewFragment.kt:64` — `.setPositiveButton(android.R.string.ok) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/WebviewFragment.kt:68` — `.setNegativeButton(android.R.string.cancel, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/diagnostics/DiagnosticDialog.kt:32` — `val text=TextView(context).apply {id=R.id.diagnostic_report_text;text=arguments?.getString("report");textSize=16f;setPadding(dp(16),dp(12),dp(16),dp(12));isFocusable=true}`
- `app/src/main/java/io/nekohasekai/sagernet/ui/diagnostics/DiagnosticDialog.kt:33` — `val scroll=ScrollView(context).apply {addView(text);layoutParams=LinearLayout.LayoutParams(-1,dp((resources.configuration.screenHeightDp-220).coerceIn(120,500)))}`
- `app/src/main/java/io/nekohasekai/sagernet/ui/diagnostics/DiagnosticDialog.kt:36` — `if(arguments?.getBoolean("extended")==true)addView(Button(context).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/diagnostics/DiagnosticDialog.kt:42` — `.setNeutralButton(R.string.diagnostic_share) {_,_-> DiagnosticShareDialog.open(parentFragmentManager,arguments?.getString("report").orEmpty(),arguments?.getString("offline"))}`
- `app/src/main/java/io/nekohasekai/sagernet/ui/diagnostics/DiagnosticDialog.kt:43` — `.setPositiveButton(android.R.string.ok,null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/diagnostics/DiagnosticDialog.kt:44` — `if(arguments?.getBoolean("repeat")==true)builder.setNegativeButton(R.string.diagnostic_repeat) {_,_->parentFragmentManager.setFragmentResult("diagnostic_repeat",Bundle())}`
- `app/src/main/java/io/nekohasekai/sagernet/ui/diagnostics/DiagnosticDialog.kt:72` — `image=ImageView(context).apply {id=R.id.diagnostic_qr;layoutParams=LinearLayout.LayoutParams(size,size);contentDescription=getString(R.string.diagnostic_share)}`
- `app/src/main/java/io/nekohasekai/sagernet/ui/diagnostics/DiagnosticDialog.kt:73` — `status=TextView(context).apply {id=R.id.diagnostic_share_status;text=getString(R.string.diagnostic_share_preparing);textSize=16f;setPadding(dp(12),dp(8),dp(12),dp(8));isFocusable=true}`
- `app/src/main/java/io/nekohasekai/sagernet/ui/diagnostics/DiagnosticDialog.kt:74` — `val scroll=ScrollView(context).apply {addView(status)}`
- `app/src/main/java/io/nekohasekai/sagernet/ui/diagnostics/DiagnosticDialog.kt:81` — `builder.setTitle(R.string.diagnostic_share).setView(frame).setPositiveButton(R.string.diagnostic_share_close,null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/diagnostics/DiagnosticDialog.kt:82` — `if(!arguments?.getString("offline").isNullOrBlank()) builder.setNeutralButton(R.string.diagnostic_offline_button,null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/diagnostics/DiagnosticDialog.kt:101` — `.setItems(options.map {it.second.hostAddress}.toTypedArray()){_,index->if(continuation.isActive)continuation.resumeWith(Result.success(options[index]))}`
- `app/src/main/java/io/nekohasekai/sagernet/ui/diagnostics/DiagnosticDialog.kt:102` — `.setNegativeButton(android.R.string.cancel){_,_->if(continuation.isActive)continuation.resumeWith(Result.success(null))}`
- `app/src/main/java/io/nekohasekai/sagernet/ui/lan/LanDiscoveryFragment.kt:168` — `.setItems(candidates.map { "${it.host}:${it.port}" }.toTypedArray()) { _, i -> confirmProfile(fresh, candidates[i]) }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/lan/LanDiscoveryFragment.kt:169` — `.setNegativeButton(android.R.string.cancel, null).show()`
- `app/src/main/java/io/nekohasekai/sagernet/ui/lan/LanDiscoveryFragment.kt:185` — `.setPositiveButton(android.R.string.ok, null).show()`
- `app/src/main/java/io/nekohasekai/sagernet/ui/lan/LanDiscoveryFragment.kt:188` — `.setItems(networks.map { scopeDetails(it) }.toTypedArray()) { _, index -> select(networks[index]) }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/lan/LanDiscoveryFragment.kt:189` — `.setNegativeButton(android.R.string.cancel, null).show()`
- `app/src/main/java/io/nekohasekai/sagernet/ui/lan/LanDiscoveryFragment.kt:244` — `val input = EditText(context).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/lan/LanDiscoveryFragment.kt:251` — `panel.addView(TextView(context).apply { text = "${candidate.host}:${candidate.port}\n${if (gatewayQuick) getString(R.string.lan_gateway_unverified) else kindLabel(candidate.kind)}\n${getString(if (gatewayQuick) R.string.lan_gateway_explanation else R.string.lan_save_warning)}"; textSize = 18f })`
- `app/src/main/java/io/nekohasekai/sagernet/ui/lan/LanDiscoveryFragment.kt:253` — `panel.addView(TextView(context).apply { text = getString(R.string.lan_protocol_label); textSize = 18f })`
- `app/src/main/java/io/nekohasekai/sagernet/ui/lan/LanDiscoveryFragment.kt:254` — `val protocol = Spinner(context).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/lan/LanDiscoveryFragment.kt:266` — `val validation = TextView(context).apply { textSize = 18f; accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/lan/LanDiscoveryFragment.kt:269` — `panel.addView(TextView(context).apply { text = getString(R.string.lan_group) })`
- `app/src/main/java/io/nekohasekai/sagernet/ui/lan/LanDiscoveryFragment.kt:270` — `val group = Spinner(context).apply { minimumHeight = (48 * resources.displayMetrics.density).toInt()`
- `app/src/main/java/io/nekohasekai/sagernet/ui/lan/LanDiscoveryFragment.kt:273` — `val scroll = ScrollView(context).apply { addView(panel) }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/lan/LanDiscoveryFragment.kt:275` — `.setPositiveButton(R.string.lan_save, null).setNegativeButton(android.R.string.cancel, null).create()`
- `app/src/main/java/io/nekohasekai/sagernet/ui/profile/ChainSettingsActivity.kt:223` — `.setPositiveButton(android.R.string.ok, null).show()`
- `app/src/main/java/io/nekohasekai/sagernet/ui/profile/ChainSettingsActivity.kt:300` — `MaterialAlertDialogBuilder(this@ChainSettingsActivity).setTitle(proxyEntity.displayName()).setItems(actions.map { getString(it.first) }.toTypedArray()) { _, index -> actions[index].second() }.show()`
- `app/src/main/java/io/nekohasekai/sagernet/ui/profile/ConfigEditActivity.kt:41` — `setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/profile/ConfigEditActivity.kt:44` — `setNegativeButton(R.string.no) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/profile/ConfigEditActivity.kt:47` — `setNeutralButton(android.R.string.cancel, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/profile/ProfileSettingsActivity.kt:54` — `setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/profile/ProfileSettingsActivity.kt:59` — `setNegativeButton(R.string.no) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/profile/ProfileSettingsActivity.kt:62` — `setNeutralButton(android.R.string.cancel, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/profile/ProfileSettingsActivity.kt:71` — `setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/profile/ProfileSettingsActivity.kt:77` — `setNegativeButton(R.string.no, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/profile/ProfileSettingsActivity.kt:368` — `val scrollView = ScrollView(context).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/ActionPresenter.kt:30` — `val icon = ImageView(context).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/ActionPresenter.kt:39` — `val title = TextView(context).apply { id = R.id.action_title; textSize = 20f; setTextColor(Color.WHITE); minLines = 2; maxLines = 2; ellipsize = TextUtils.TruncateAt.END }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/ActionPresenter.kt:40` — `val subtitle = TextView(context).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/ConnectionCardPresenter.kt:20` — `fun line(size: Float,lines: Int)=TextView(context).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/GroupAndFunctionPresenters.kt:10` — `delegate.onBindViewHolder(holder,TvAction(group.id,group.name,context.getString(R.string.tv_group_row_count,group.count)+"\n"+`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/GroupAndFunctionPresenters.kt:23` — `delegate.onBindViewHolder(holder,TvAction(entry.id.toLong(),entry.title,holder.view.context.getString(R.string.tv_function_direct),R.drawable.ic_baseline_more_vert_24))`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:330` — `actionsAdapter.setItems(listOf(`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:331` — `TvAction(TOGGLE, getString(label), names.joinToString("\n"), if (command == TvVpnCommand.STOP) R.drawable.ic_service_active else R.drawable.ic_service_idle, command != TvVpnCommand.NONE),`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:332` — `TvAction(GROUPS, getString(R.string.tv_groups), group?.let { getString(R.string.tv_group_count, it.displayName(), profiles.size) } ?: "", R.drawable.ic_remote_groups),`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:333` — `TvAction(ADD_PROFILE, getString(R.string.add_profile), getString(R.string.tv_add_profile_hint), R.drawable.ic_action_note_add),`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:334` — `TvAction(APP_UPDATE, getString(R.string.menu_app_updates), getString(R.string.tv_quality_update_hint), R.drawable.ic_baseline_more_vert_24),`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:335` — `TvAction(PHONE_MODE, getString(R.string.tv_phone_mode), getString(R.string.tv_phone_mode_hint), R.drawable.ic_remote_phone)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:338` — `toolsAdapter.setItems(listOf(TvAction(FULL_TOOLS,getString(R.string.tv_all_functions),getString(R.string.tv_all_functions_hint),R.drawable.ic_baseline_more_vert_24))+`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:340` — `operationsAdapter.setItems(listOf(`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:341` — `TvAction(PROFILE_ACTIONS, getString(R.string.tv_actions), getString(R.string.tv_actions_hint), R.drawable.ic_image_edit, DataStore.selectedProxy > 0),`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:342` — `TvAction(UPDATE, getString(R.string.tv_update_group), getString(if (updating) R.string.tv_updating else if (group?.type != GroupType.SUBSCRIPTION) R.string.tv_not_subscription else R.string.tv_update_group), R.drawable.ic_social_share, group?.type == GroupType.SUBSCRIPTION && !updating),`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:343` — `TvAction(SUBSCRIPTION_DIAGNOSTICS, getString(R.string.subscription_attempt_title), getString(R.string.subscription_attempt_description), R.drawable.ic_social_share, group?.type == GroupType.SUBSCRIPTION),`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:344` — `TvAction(TESTS, getString(R.string.tv_tests_title), getString(R.string.tv_tests_hint), R.drawable.ic_remote_groups),`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:345` — `TvAction(HOME, getString(R.string.tv_home), getString(R.string.tv_home_hint), R.drawable.ic_baseline_more_vert_24),`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:346` — `TvAction(MORE, getString(R.string.tv_more_import), getString(R.string.tv_more_hint), R.drawable.ic_baseline_more_vert_24),`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:347` — `TvAction(RESTART_APP, getString(R.string.app_restart), getString(R.string.app_restart_hint), R.drawable.ic_baseline_refresh_24),`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:348` — `TvAction(CLOSE_APP, getString(R.string.app_close), getString(R.string.app_close_hint), R.drawable.ic_navigation_close)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:362` — `groupAdapter.setItems(cards,diff)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:367` — `rowAdapter.setItems(rows,object: DiffCallback<ListRow>() {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:389` — `TvQualityCard(DIAGNOSTICS,getString(R.string.diagnostic_title),short,`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:393` — `if (phase == TvVpnPhase.CONNECTED) statusCards.add(TvAction(YOUTUBE, getString(R.string.tv_youtube), getString(R.string.tv_youtube_hint), R.drawable.ic_baseline_more_vert_24))`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:394` — `connectionAdapter.setItems(statusCards, diff)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:449` — `.setPositiveButton(android.R.string.ok, null)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:450` — `if (record != null) builder.setNeutralButton(R.string.subscription_attempt_share) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:518` — `}.setNegativeButton(android.R.string.cancel, null))`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:544` — `.setPositiveButton(android.R.string.ok, null))`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:551` — `.setPositiveButton(R.string.yes) { _, _ -> if (continuation.isActive) continuation.resume(true) }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:552` — `.setNegativeButton(R.string.no) { _, _ -> updateDeclined = true; if (continuation.isActive) continuation.resume(false) }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:570` — `options.add(R.string.tv_details to { show(AlertDialog.Builder(requireContext()).setTitle(entity.displayName()).setMessage("${entity.displayType()}\n${entity.requireBean().serverAddress}:${entity.requireBean().serverPort}").setPositiveButton(android.R.string.ok, null)) })`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:585` — `.setNegativeButton(android.R.string.cancel, null).setPositiveButton(R.string.yes) { _, _ -> launchWork {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:592` — `show(AlertDialog.Builder(requireContext()).setTitle(entity.displayName()).setItems(options.map { getString(it.first) }.toTypedArray()) { _, index -> try { options[index].second() } catch (_: Exception) { toast(R.string.tv_operation_failed) } })`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:612` — `.setItems(options.map { getString(it) }.toTypedArray()) { _, index ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:624` — `}.setNegativeButton(android.R.string.cancel, null))`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:629` — `.setItems(entries.map { it.title }.toTypedArray()) { _, index ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:646` — `}.setNegativeButton(android.R.string.cancel, null))`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:653` — `val input = EditText(requireContext()).apply { hint = getString(R.string.tv_url_hint); textSize = 20f; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI; val density = resources.displayMetrics.density; setPadding((16 * density).toInt(), (12 * density).toInt(), (16 * density).toInt(), (12 * density).toInt()) }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:654` — `show(AlertDialog.Builder(requireContext()).setTitle(R.string.tv_url).setView(input).setPositiveButton(R.string.tv_import) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:656` — `if (text.isBlank()) { toast(R.string.tv_url_required); return@setPositiveButton }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:663` — `}.setNeutralButton(R.string.tv_import_phone) { _, _ -> showPhoneImport() }.setNegativeButton(android.R.string.cancel, null))`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:668` — `.setItems(entries.map { it.title }.toTypedArray()) { _, index ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:672` — `}.setNegativeButton(android.R.string.cancel, null))`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:676` — `.setPositiveButton(R.string.yes) { _, _ ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:680` — `}.setNegativeButton(android.R.string.cancel, null))`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:701` — `.setMessage(R.string.diagnostic_extended_scope).setNegativeButton(android.R.string.cancel){_,_->extendedJob?.cancel()})`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:736` — `checkProgress=android.widget.TextView(themed).apply {id=R.id.diagnostic_check_progress_text;text=checkingLabel();textSize=18f;setPadding(24,16,24,16)}`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:739` — `addView(android.widget.ProgressBar(themed));addView(checkProgress)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:742` — `.setNegativeButton(android.R.string.cancel) {_,_->healthJob?.cancel()})`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:817` — `.setItems(entries.map { it.title }.toTypedArray()) { _, index -> openFullScreen(entries[index].id) }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:818` — `.setNegativeButton(android.R.string.cancel, null))`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:822` — `show(AlertDialog.Builder(requireContext()).setTitle(R.string.tv_tests_title).setItems(options.map { getString(it) }.toTypedArray()) { _, index ->`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:832` — `}.setNegativeButton(android.R.string.cancel, null))`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:845` — `.setPositiveButton(android.R.string.ok,null))`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:852` — `.setMessage(R.string.tv_test_starting).setNegativeButton(android.R.string.cancel){_,_->cancelled.set(true);testJob?.cancel()})`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:946` — `.setMessage(report+"\n\n"+getString(R.string.tv_quality_scope)).setPositiveButton(android.R.string.ok,null))`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:959` — `.setMessage(R.string.tv_test_starting).setNegativeButton(android.R.string.cancel) { _, _ -> testJob?.cancel() })`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt:983` — `.setPositiveButton(android.R.string.ok, null))`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/QrCodeTransferFragment.kt:44` — `left.addView(TextView(context).apply { setText(if (outgoing) R.string.tv_send_qr_title else R.string.tv_qr_title); textSize = 24f; setTextColor(Color.WHITE); gravity = Gravity.CENTER; maxLines = 2 })`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/QrCodeTransferFragment.kt:45` — `qrImageView = ImageView(context).apply { layoutParams = LinearLayout.LayoutParams(qrSize, qrSize).apply { topMargin = dp(12) }; scaleType = ImageView.ScaleType.FIT_CENTER; importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/QrCodeTransferFragment.kt:47` — `ipText = TextView(context).apply { textSize = 16f; setTextColor(0xFF67E8F9.toInt()); gravity = Gravity.CENTER; maxLines = 2 }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/QrCodeTransferFragment.kt:50` — `statusText = TextView(context).apply { textSize = 18f; setTextColor(Color.WHITE); accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/QrCodeTransferFragment.kt:51` — `hintText = TextView(context).apply { textSize = 16f; setTextColor(0xFFCBD5E1.toInt()); setPadding(0, dp(12), 0, dp(16)) }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/QrCodeTransferFragment.kt:53` — `fun button(label: Int) = Button(context).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/QrCodeTransferFragment.kt:65` — `val controls = ScrollView(context).apply { addView(right); isFocusable = false }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/QrCodeTransferFragment.kt:123` — `.setPositiveButton(R.string.tv_receive_continue) { _, _ -> parentFragmentManager.popBackStack() }`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/QualityCardPresenter.kt:20` — `val lines=(0..3).map {i->TextView(parent.context).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvInteractionPolicy.kt:41` — `data class TvAction(val id: Long, val title: String, val subtitle: String, val icon: Int, val available: Boolean = true)`
- `app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvInteractionPolicy.kt:48` — `data class TvQualityCard(val id: Long,val title: String,val result: String,val age: String,val traffic: String)`
- `app/src/main/java/io/nekohasekai/sagernet/widget/QRCodeDialog.kt:84` — `addView(ImageView(context).apply {`
- `app/src/main/java/io/nekohasekai/sagernet/widget/QRCodeDialog.kt:98` — `addView(TextView(context).apply {`
- `app/src/main/java/moe/matsuri/nb4a/ui/ColorPickerPreference.kt:63` — `return ImageView(context).apply {`
- `app/src/main/java/moe/matsuri/nb4a/ui/ColorPickerPreference.kt:115` — `.setNegativeButton(android.R.string.cancel, null)`
- `app/src/main/java/moe/matsuri/nb4a/ui/Dialogs.kt:19` — `.setPositiveButton(android.R.string.ok) { _, _ ->`
- `app/src/main/java/moe/matsuri/nb4a/ui/MTUPreference.kt:29` — `val view = EditText(context).apply {`
- `app/src/main/java/moe/matsuri/nb4a/ui/MTUPreference.kt:36` — `.setPositiveButton(android.R.string.ok) { _, _ ->`
- `app/src/main/java/moe/matsuri/nb4a/ui/MTUPreference.kt:38` — `if (mtu < 1000 || mtu > 10000) return@setPositiveButton`
- `app/src/main/java/moe/matsuri/nb4a/ui/MTUPreference.kt:41` — `.setNegativeButton(android.R.string.cancel, null)`
