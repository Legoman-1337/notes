# -*- coding: utf-8 -*-
"""Генератор полезных плагинов «Удобных заметок» (v3.5)."""
import json, os, shutil, sys, subprocess

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import defs_a, defs_b, defs_c

REPO = r"C:\Users\artem\Desktop\notes_for_mom_android"
ANDROID_ASSETS = os.path.join(REPO, "app", "src", "main", "assets", "plugins_extra")
DESKTOP_RES = os.path.join(REPO, "desktop", "src", "main", "resources", "plugins_extra")
ANDROID_KT = os.path.join(REPO, "app", "src", "main", "java", "ru", "momnotes", "app", "ExtraCatalog.kt")
DESKTOP_KT = os.path.join(REPO, "desktop", "src", "main", "kotlin", "ru", "momnotes", "app", "ExtraCatalog.kt")

PACKAGES = [
    dict(id="home", name="Дом и семья", icon="🏠",
         desc="Дела, меню недели, полив цветов, питомец, аптечка и важные документы",
         plugins=["home-tasks", "menu-week", "dinner-roulette", "plants-water", "pet-tracker",
                  "first-aid-kit", "documents-dates", "gifts-ideas", "freezer-log"]),
    dict(id="money", name="Финансы и расчёты", icon="💰",
         desc="Траты дня, доходы, копилка мечты, подписки, скидки и деление счёта",
         plugins=["spend-diary", "income-log", "piggy-bank", "subscriptions-list", "discount-calc",
                  "bill-split", "salary-plan", "currency-memo"]),
    dict(id="health", name="Здоровье и сон", icon="💧",
         desc="Вода, приём лекарств, дневник сна, вес и дыхание 4-7-8",
         plugins=["water-tracker", "pills-check", "sleep-diary", "weight-diary", "breathe-calm"]),
    dict(id="study", name="Учёба, книги и досуг", icon="📚",
         desc="Помодоро, домашка, оценки, книги, фильмы и счётчик серий",
         plugins=["pomodoro", "homework-list", "grades-log", "books-list", "movies-list", "series-tracker"]),
    dict(id="tools", name="Инструменты и заметки", icon="⏱️",
         desc="Таймер, секундомер, даты, конвертер величин, пароли, стикер и Фея",
         plugins=["fairy", "timers-mini", "stopwatch", "date-diff", "age-calc", "unit-conv",
                  "password-gen", "wifi-memo", "packing-list", "quick-sticker", "mood-diary",
                  "thanks-jar", "ideas-box"]),
]


def main():
    plugins = defs_a.plugins() + defs_b.plugins() + defs_c.plugins()

    ids = [p["id"] for p in plugins]
    assert len(ids) == len(set(ids)), "дубликат id: " + str([i for i in ids if ids.count(i) > 1])
    pkg_ids = [i for p in PACKAGES for i in p["plugins"]]
    missing = [i for i in pkg_ids if i not in ids]
    assert not missing, "пакеты ссылаются на несуществующие плагины: " + str(missing)

    total = 0
    for base in (ANDROID_ASSETS, DESKTOP_RES):
        os.makedirs(base, exist_ok=True)
        for p in plugins:
            d = os.path.join(base, p["id"])
            os.makedirs(d, exist_ok=True)
            js = p["js"].replace("%NAME%", p["name"]).replace("%ID%", p["id"])
            manifest = {
                "id": p["id"], "name": p["name"], "version": "1.1.0",
                "author": "Legoman", "description": p["desc"],
                "main": "plugin.js", "icon": p["icon"],
            }
            with open(os.path.join(d, "manifest.json"), "w", encoding="utf-8", newline="\n") as f:
                json.dump(manifest, f, ensure_ascii=False, indent=2)
                f.write("\n")
            with open(os.path.join(d, "plugin.js"), "w", encoding="utf-8", newline="\n") as f:
                f.write(js)
            total += 1
        keep = {p["id"] for p in plugins}
        for name in os.listdir(base):
            if name not in keep:
                shutil.rmtree(os.path.join(base, name), ignore_errors=True)

    kt_ids = ",\n        ".join('"%s"' % i for i in ids)
    kt_pkgs = ",\n        ".join(
        'ExtraPackage("%s", "%s", "%s", "%s", listOf(%s))' % (
            p["id"], p["name"], p["icon"], p["desc"],
            ", ".join('"%s"' % i for i in p["plugins"]))
        for p in PACKAGES)
    kt = """package ru.momnotes.app

/**
 * Очищенный каталог полезных «Дополнительных» плагинов (без мусорных мини-игр)
 * и пакетов для вкладки «Пакеты».
 * Сгенерировано plugin-registry/extra/generate.py.
 */
internal object ExtraCatalog {

    val PLUGIN_IDS: List<String> = listOf(
        @IDS@
    )

    val PACKAGES: List<ExtraPackage> = listOf(
        @PKGS@
    )
}
""".replace("@IDS@", kt_ids).replace("@PKGS@", kt_pkgs)
    for path in (ANDROID_KT, DESKTOP_KT):
        if os.path.exists(os.path.dirname(path)) or path == ANDROID_KT:
            os.makedirs(os.path.dirname(path), exist_ok=True)
            with open(path, "w", encoding="utf-8", newline="\n") as f:
                f.write(kt)

    print("Плагинов: %d (уникальных id: %d)" % (total // 2, len(ids)))
    print("Пакетов: %d" % len(PACKAGES))
    for p in PACKAGES:
        print("  %s %s — %d плагинов" % (p["icon"], p["name"], len(p["plugins"])))
    print("OK")

    node = shutil.which("node")
    if node:
        bad = []
        for p in plugins:
            js_path = os.path.join(ANDROID_ASSETS, p["id"], "plugin.js")
            r = subprocess.run([node, "--check", js_path], capture_output=True, text=True)
            if r.returncode != 0:
                bad.append((p["id"], r.stderr.strip().splitlines()[-1] if r.stderr else "?"))
        if bad:
            print("СИНТАКС-ОШИБКИ:")
            for pid, err in bad:
                print("  %s: %s" % (pid, err))
            sys.exit(1)
        print("node --check: все plugin.js валидны")
    else:
        print("node не найден — синтакс-проверка пропущена")


if __name__ == "__main__":
    main()