# Плагины заметок — как это устроено

## Структура плагина

Каждый плагин — отдельная папка внутри `plugins/` в данных приложения:

```
files/
└── plugins/
    └── kinder-crm/
        ├── manifest.json   ← паспорт плагина
        └── plugin.js       ← код плагина
```

`manifest.json`:

```json
{
  "id": "kinder-crm",
  "name": "Журнал и Табель",
  "version": "1.0.0",
  "author": "Legoman",
  "description": "Учёт посещаемости, оплат и детских групп",
  "main": "plugin.js",
  "icon": "📋"
}
```

`plugin.js` — две функции:

- `activate(app)` — строит интерфейс плагина (HTML в `#root`). Вызывается при открытии экрана плагина.
- `deactivate()` — вызывается при закрытии экрана (можно дочистить/сохраниться).

Обе можно объявить как глобальные (`function activate(app)`) или как
`window.plugin = { activate, deactivate }`.

## Мост к приложению: объект `app`

| Метод              | Что делает                                          |
|--------------------|-----------------------------------------------------|
| `app.load()`       | Прочитать все данные плагина (JSON-строка, `{}` если пусто) |
| `app.save(json)`   | Сохранить данные плагина (файл `plugin_data/<id>.json`) |
| `app.toast(text)`  | Показать всплывающее сообщение                      |
| `app.close()`      | Закрыть экран плагина                               |

CSS-переменные темы приложения доступны плагину: `--bg --surface --surface2
--text --muted --accent --on-accent --border --danger`, плюс готовые классы
`.card .btn .chip .day .muted .row .grow table .money`.

## Каталог плагинов (магазин) без своего сервера

1. Создай публичный репозиторий GitHub, например `notes-plugins-registry`.
2. Положи в корень `index.json` (образец лежит в `plugin-registry/index.json`):

```json
[
  {
    "id": "kinder-crm",
    "name": "Журнал и Табель",
    "version": "1.0.0",
    "author": "Legoman",
    "description": "Учёт посещаемости и оплаты",
    "icon": "📋",
    "download_url": "https://raw.githubusercontent.com/<ты>/notes-plugins-registry/main/plugins/kinder-crm/kinder-crm.zip"
  }
]
```

3. Рядом положи `plugins/kinder-crm/kinder-crm.zip` — zip с `manifest.json` +
   `plugin.js` (или один `.js` файл в `download_url`).
4. Адрес реестра по умолчанию:
   `https://raw.githubusercontent.com/Legoman-1337/notes-plugins-registry/main/index.json`.
   Его можно поменять прямо в приложении: «+ Плагины» → иконка ⌂ справа сверху.

В магазине: поиск по `@id` или названию → «Скачать» → плагин распаковывается в
`plugins/<id>/` и включается сразу, без перезапуска приложения.

## Две версии сборки

- `momEdition=mom` (версия для мамы): плагин `kinder-crm` вшит в приложение
  (`app/src/main/assets/plugins/kinder-crm/`), при первом запуске копируется
  в `plugins/` и сразу включается.
- `momEdition=public` (публичная версия): вшитые плагины **не устанавливаются
  вообще** — приложение стартует пустым, плагины ставятся через «+ Плагины».

Сборка для мамы: `gradlew assembleDebug` (издание уже `mom`).
Публичная сборка: `gradlew assembleDebug -PmomEdition=public`.

## Облачное сохранение данных плагина

Данные плагина (`plugin_data/<id>.json`) автоматически дублируются в Appwrite
Storage (бакет заметок) после каждого `app.save()` и восстанавливаются при
открытии плагина. Работает под аккаунтом; в гостевом режиме — только локально.
Данные не удаляются ни при удалении плагина, ни при переустановке приложения.

